package com.minecolonies.coremod.colony.crafting;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.Network;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final static CustomRecipeManager instance = new CustomRecipeManager();

    /**
     * The map of loaded recipes by crafter.
     */
    private final HashMap<String, Map<ResourceLocation, CustomRecipe>> recipeMap = new HashMap<>();

    /**
     * The map of all loaded recipes by output.
     */
    private final HashMap<Item, List<CustomRecipe>> recipeOutputMap = new HashMap<>();

    /**
     * The recipes that are marked for removal after loading all resource packs
     * This list will be processed on first access of the custom recipe list after load, and will be emptied.
     */
    private final List<ResourceLocation> removedRecipes = new ArrayList<>();

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

        if (!recipeOutputMap.containsKey(recipe.getPrimaryOutput().getItem()))
        {
            recipeOutputMap.put(recipe.getPrimaryOutput().getItem(), new ArrayList<>());
        }
        recipeOutputMap.get(recipe.getPrimaryOutput().getItem()).add(recipe);
        for (final ItemStack item : recipe.getAltOutputs())
        {
            if (!recipeOutputMap.containsKey(item.getItem()))
            {
                recipeOutputMap.put(item.getItem(), new ArrayList<>());
            }
            recipeOutputMap.get(item.getItem()).add(recipe);
        }
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
     * @param item     An individual item to search for recipes.
     * @return  A list of custom recipes with that output.
     */
    @Nullable
    public List<CustomRecipe> getRecipeByOutput(final Item item)
    {
        return recipeOutputMap.get(item);
    }

    /**
     * Gets the custom recipes for an ItemStack, including comparing count and tags, or null if no matching recipe exists.
     * @param itemStack An ItemStack to search for recipes.
     * @return  A list of custom recipes with that output.
     */
    @Nullable
    public List<CustomRecipe> getRecipeByOutput(final ItemStack itemStack)
    {
        List<CustomRecipe> returnList = new ArrayList<>();
        for(CustomRecipe recipe : recipeOutputMap.get(itemStack.getItem()))
        {
            // ItemStacks don't override equals, so have to use the static methods.
            if(ItemStack.areItemStacksEqual(recipe.getPrimaryOutput(), itemStack))
            {
                returnList.add(recipe);
            }
            for(ItemStack output : recipe.getAltOutputs())
            {
                if(ItemStack.areItemStacksEqual(output, itemStack))
                {
                    returnList.add(recipe);
                }
            }
        }
        if(returnList.size() > 0)
        {
            return returnList;
        }
        return null;
    }

    /**
     * Gets the custom recipes for an ItemStorage, optionally including comparing count, damage, and NBT, or null if no matching recipe exists.
     * @param itemStorage An ItemStorage to search for recipes.
     * @return  A list of custom recipes with that output.
     */
    @Nullable
    public List<CustomRecipe> getRecipeByOutput(final ItemStorage itemStorage)
    {
        List<CustomRecipe> returnList = new ArrayList<>();
        for(CustomRecipe recipe : recipeOutputMap.get(itemStorage.getItem()))
        {
            // ItemStorage#equals does the actual comparison work for us, here.
            if(recipe.getPrimaryOutput().equals(itemStorage) || recipe.getAltOutputs().contains(itemStorage))
            {
                returnList.add(recipe);
            }
        }
        if(returnList.size() > 0)
        {
            return returnList;
        }
        return null;
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
     * This version sends the full Custom Recipe Manager.
     * @param recipeMgrPacketBuffer packet buffer to encode the data into.
     */
    private void serializeNetworkData(final PacketBuffer recipeMgrPacketBuffer)
    {
        // Custom Recipe Manager packets can potentially get very large, and individual CompoundNBTs can not be parsed if they exceed 2MB.
        // For safety with arbitrary data packs (or sets of data packs), we can not wrap the entire CustomRecipeManager into single ListNBT.
        // Including all recipes in transfer results in total transfer size around ~670KB for just Minecolonies + Structurize recipes.
        // See CustomRecipeFactory.serialize for last tested numbers and more precise breakdown.
        recipeMgrPacketBuffer.writeVarInt(recipeMap.size());
        for (Map.Entry<String, Map<ResourceLocation, CustomRecipe>> crafter : recipeMap.entrySet())
        {
            recipeMgrPacketBuffer.writeVarInt(crafter.getValue().size());
            for (CustomRecipe recipe : crafter.getValue().values())
            {
                StandardFactoryController.getInstance().serialize(recipeMgrPacketBuffer, recipe);
                //// NBT-based serialization for debugging/diagnosis only, as total packet size can be /very/ large.
                //recipeMgrPacketBuffer.writeCompoundTag(StandardFactoryController.getInstance().serialize(recipe));
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
        recipeMap.clear();

        for (int crafterNum = buff.readVarInt(); crafterNum > 0; crafterNum--)
        {
            for (int recipeNum = buff.readVarInt(); recipeNum > 0; recipeNum--)
            {
                addRecipe(StandardFactoryController.getInstance().deserialize(buff));
                //// NBT-based serialization for debugging/diagnostics only, as total packet size can be very large.
                //addRecipe(StandardFactoryController.getInstance().deserialize(buff.readCompoundTag()));
            }
        }
    }
}
