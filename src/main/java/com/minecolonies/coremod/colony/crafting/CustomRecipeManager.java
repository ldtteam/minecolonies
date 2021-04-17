package com.minecolonies.coremod.colony.crafting;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.coremod.Network;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Manager class for tracking Custom recipes during load and use
 * This class is a singleton
 */
public class CustomRecipeManager
{
    /**
     * The internal static instance of the singleton
     */
    final private static CustomRecipeManager instance = new CustomRecipeManager();

    /**
     * The map of loaded recipes by crafter. Only fully populated on the server or in local single-player.
     */
    final private HashMap<String, Map<ResourceLocation, CustomRecipe>> recipeMap = new HashMap<>();

    /**
     * The map of all loaded recipes by output. Only reliably populated on the client.
     */
    final private HashMap<Item, List<CustomRecipe>> recipeOutputMap = new HashMap<>();

    /**
     * The recipes that are marked for removal after loading all resource packs
     * This list will be processed on first access of the custom recipe list after load, and will be emptied.
     */
    final private List<ResourceLocation> removedRecipes = new ArrayList<>();

    private CustomRecipeManager()
    {
    }

    /**
     * Get the singleton instance of this class
     *
     * @return
     */
    public static CustomRecipeManager getInstance()
    {
        return instance;
    }

    /**
     * Add recipe to manager.
     * @param recipe the recipe to add
     */
    public void addRecipe(@NotNull final CustomRecipe recipe)
    {
        if(!recipeMap.containsKey(recipe.getCrafter()))
        {
            recipeMap.put(recipe.getCrafter(), new HashMap<>());
        }

        recipeMap.get(recipe.getCrafter()).put(recipe.getRecipeId(), recipe);
    }

    /**
     * Remove recipe
     * @param toRemove
     */
    public void removeRecipe(@NotNull final ResourceLocation toRemove)
    {
        if(!removedRecipes.contains(toRemove))
        {
            removedRecipes.add(toRemove);
        }
    }

    /**
     * Reset the entire recipe map.
     * Should be run on Data Pack Reloads, to avoid transferring settings from another world.
     */
    public void reset()
    {
        recipeMap.clear();
    }

    /**
     * Get all of the custom recipes that apply to a particular crafter
     * @param crafter
     * @return
     */
    public Set<CustomRecipe> getRecipes(@NotNull final String crafter)
    {
        removeRecipes();

        return Collections.unmodifiableSet(new HashSet<>(recipeMap.getOrDefault(crafter, new HashMap<>()).values()));
    }

    /**
     * The complete list of custom recipes, by crafter.
     */
    public Map<String, Map<ResourceLocation, CustomRecipe>> getAllRecipes()
    {
        return recipeMap;
    }

    /**
     * Get the custom recipes for an item, or null if no matching recipe exists.
     */
    public List<CustomRecipe> getRecipeByOutput(final Item item)
    {
        return recipeOutputMap.get(item);
    }

    private void removeRecipes()
    {
        if (!removedRecipes.isEmpty())
        {
            for (final ResourceLocation toRemove : removedRecipes)
            {
                recipeMap.values().stream()
                    .filter(recipes -> recipes.containsKey(toRemove))
                    .findFirst()
                    .ifPresent(crafterRecipeMap -> crafterRecipeMap.remove(toRemove));
            }

            removedRecipes.clear();
        }
    }

    /**
     * Sends relevant Custom Recipes loaded from the Custom Recipe Manager to the client.
     * @param player the player to send the new data to.
     */
    public void sendCustomRecipeManagerPackets(final ServerPlayerEntity player)
    {
        final PacketBuffer recipeMgrPacketBuffer = new PacketBuffer(Unpooled.buffer());
        serializeNetworkData(recipeMgrPacketBuffer);
        Network.getNetwork().sendToPlayer(new CustomRecipeManagerMessage(recipeMgrPacketBuffer), player);
    }

    /**
     * Serializes a partial assembly of Custom Recipes.
     * This version only includes recipes with showTooltip to be true.
     * @param recipeMgrPacketBuffer packet buffer to encode the data into.
     */
    private void serializeNetworkData(final PacketBuffer recipeMgrPacketBuffer)
    {
        // Custom Recipe Manager packets can potentially get very large, and individual CompoundNBTs can not be parsed if they exceed 2MB.
        // For safety with arbitrary data packs (or sets of data packs), we can not wrap the entire CustomRecipeManager into single ListNBT.
        // Filtering to only recipes with ShowTooltip or ShowJei enabled reduces total transfer size to <50KB.
        // See CustomRecipeFactory.serialize for last tested numbers and more precise breakdown.
        recipeMgrPacketBuffer.writeInt(recipeMap.size());
        for (Map.Entry<String, Map<ResourceLocation, CustomRecipe>> crafter : recipeMap.entrySet())
        {
            int count = 0;
            final int startIdx = recipeMgrPacketBuffer.writerIndex();
            recipeMgrPacketBuffer.writerIndex(startIdx + 4);
            for (CustomRecipe recipe : crafter.getValue().values())
            {
                if (recipe.getShowTooltip() || recipe.getShowJei())
                {
                    count++;
                    recipeMgrPacketBuffer.writeCompoundTag(StandardFactoryController.getInstance().serialize(recipe));
                }
            }
            final int endIdx = recipeMgrPacketBuffer.writerIndex();
            recipeMgrPacketBuffer.writerIndex(startIdx);
            recipeMgrPacketBuffer.writeInt(count);
            recipeMgrPacketBuffer.writerIndex(endIdx);
        }
    }

    /**
     * Serializes a partial assembly of Custom Recipes.
     * This version sends the full Custom Recipe Manager.
     * @param recipeMgrPacketBuffer packet buffer to encode the data into.
     */
    // Alternative approach, if it becomes necessary to send the entire CustomRecipeManager.
    private void serializeNetworkDataByRecipe(final PacketBuffer recipeMgrPacketBuffer)
    {
        // Custom Recipe Manager packets can potentially get very large, and individual CompoundNBTs can not be parsed if they exceed 2MB.
        // For safety with arbitrary data packs (or sets of data packs), we can not wrap the entire CustomRecipeManager into single ListNBT.
        // Including all recipes in transfer results in total transfer size around ~400KB for base recipes.
        // See CustomRecipeFactory.serialize for last tested numbers and more precise breakdown.
        recipeMgrPacketBuffer.writeInt(recipeMap.size());
        for (Map.Entry<String, Map<ResourceLocation, CustomRecipe>> crafter : recipeMap.entrySet())
        {
            recipeMgrPacketBuffer.writeInt(crafter.getValue().size());
            for (CustomRecipe recipe : crafter.getValue().values())
            {
                recipeMgrPacketBuffer.writeCompoundTag(StandardFactoryController.getInstance().serialize(recipe));
            }
        }
    }

    /**
     * Ingests the custom recipes packet, and applies it to the recipe manager.
     * @param buff packet buffer containing the received data.
     */
    public void handleCustomRecipeManagerMessage(final PacketBuffer buff)
    {
        recipeOutputMap.clear();

        for (int crafterNum = buff.readInt(); crafterNum > 0; crafterNum--)
        {
            for (int recipeNum = buff.readInt(); recipeNum > 0; recipeNum--)
            {
                CustomRecipe rec = StandardFactoryController.getInstance().deserialize(buff.readCompoundTag());
                if (!recipeOutputMap.containsKey(rec.getPrimaryOutput().getItem()))
                {
                    recipeOutputMap.put(rec.getPrimaryOutput().getItem(), new ArrayList<>());
                }
                recipeOutputMap.get(rec.getPrimaryOutput().getItem()).add(rec);
                for (final ItemStack item : rec.getAltOutputs())
                {
                    if (!recipeOutputMap.containsKey(item.getItem()))
                    {
                        recipeOutputMap.put(item.getItem(), new ArrayList<>());
                    }
                    recipeOutputMap.get(item.getItem()).add(rec);
                }
            }
        }
    }
}
