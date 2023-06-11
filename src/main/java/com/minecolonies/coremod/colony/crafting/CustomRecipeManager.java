package com.minecolonies.coremod.colony.crafting;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.modules.AnimalHerdingModule;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    private final Map<String, Map<ResourceLocation, CustomRecipe>> recipeMap = new HashMap<>();

    /**
     * The map of all loaded recipes by output.
     */
    private final Map<Item, List<CustomRecipe>> recipeOutputMap = new HashMap<>();

    /**
     * The recipes that are marked for removal after loading all resource packs
     * This list will be processed on first access of the custom recipe list after load, and will be emptied.
     */
    private final List<ResourceLocation> removedRecipes = new ArrayList<>();

    /**
     * The collection of related loot table drops (for informational purposes, not loot gen).
     */
    private final Map<ResourceLocation, List<LootTableAnalyzer.LootDrop>> lootTables = new HashMap<>();

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
        recipeOutputMap.clear();
        recipeMap.clear();
        lootTables.clear();
        removedRecipes.clear();
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
     * Get the custom recipes for an item, or an empty list if no matching recipe exists.
     * @param item     An individual item to search for recipes.
     * @return  A list of custom recipes with that output.
     */
    public List<CustomRecipe> getRecipeByOutput(final Item item)
    {
        if(recipeOutputMap.containsKey(item))
        {
            return recipeOutputMap.get(item);
        }
        return Collections.emptyList();
    }

    /**
     * Gets the custom recipes for an ItemStack, including comparing count and tags, or an empty list if no matching recipe exists.
     * @param itemStack An ItemStack to search for recipes.
     * @return  A list of custom recipes with that output.
     */
    public List<CustomRecipe> getRecipeByOutput(final ItemStack itemStack)
    {
        List<CustomRecipe> returnList = new ArrayList<>();
        for (CustomRecipe recipe : recipeOutputMap.get(itemStack.getItem()))
        {
            // ItemStacks don't override equals, so have to use the static methods.
            if (ItemStack.matches(recipe.getPrimaryOutput(), itemStack))
            {
                returnList.add(recipe);
            }
            for (ItemStack output : recipe.getAltOutputs())
            {
                if (ItemStack.matches(output, itemStack))
                {
                    returnList.add(recipe);
                }
            }
        }
        return returnList;
    }

    /**
     * Gets the custom recipes for an ItemStorage, optionally including comparing count, damage, and NBT, or an empty list if no matching recipe exists.
     * @param itemStorage An ItemStorage to search for recipes.
     * @return  A list of custom recipes with that output.
     */
    public List<CustomRecipe> getRecipeByOutput(final ItemStorage itemStorage)
    {
        List<CustomRecipe> returnList = new ArrayList<>();
        for (CustomRecipe recipe : recipeOutputMap.get(itemStorage.getItem()))
        {
            // ItemStorage#equals does the actual comparison work for us, here.
            if (recipe.getPrimaryOutput().equals(itemStorage) || recipe.getAltOutputs().contains(itemStorage))
            {
                returnList.add(recipe);
            }
        }
        return returnList;
    }

    /**
     * Gets the loot drops (if any) associated with a particular recipe.  These are just
     * informational and shouldn't be used to actually generate loot.
     * @param lootTableId The loot table id of the recipe.
     * @return The loot drops.
     */
    @NotNull
    public List<LootTableAnalyzer.LootDrop> getLootDrops(@Nullable final ResourceLocation lootTableId)
    {
        if (lootTableId == null) return Collections.emptyList();

        return lootTables.getOrDefault(lootTableId, Collections.emptyList());
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
                    .ifPresent(crafterRecipeMap ->
                            {
                                final List<CustomRecipe> emptyList = new ArrayList<>();
                                final CustomRecipe recipe = crafterRecipeMap.remove(toRemove);
                                if (recipe != null)
                                {
                                    recipeOutputMap.getOrDefault(recipe.getPrimaryOutput().getItem(), emptyList).remove(recipe);
                                    for (final ItemStack item : recipe.getAltOutputs())
                                    {
                                        recipeOutputMap.getOrDefault(item.getItem(), emptyList).remove(recipe);
                                    }
                                }
                            });
            }

            removedRecipes.clear();
        }
    }

    /**
     * Analyses and builds an approximate list of possible loot drops from registered recipes.
     * @param lootTableManager the loot table manager
     */
    public void buildLootData(@NotNull final LootDataManager lootTableManager,
                              @NotNull final Level level)
    {
        final List<Animal> animals = RecipeAnalyzer.createAnimals(level);

        final List<ResourceLocation> lootIds = new ArrayList<>();
        for (final Map<ResourceLocation, CustomRecipe> recipes : recipeMap.values())
        {
            for (final CustomRecipe recipe : recipes.values())
            {
                final ResourceLocation lootTable = recipe.getLootTable();
                if (lootTable != null)
                {
                    lootIds.add(lootTable);
                }
            }
        }

        for (final BuildingEntry building : IMinecoloniesAPI.getInstance().getBuildingRegistry())
        {
            building.getModuleProducers().stream().map(Supplier::get).forEach(module ->
            {
                if (module instanceof AnimalHerdingModule herding)
                {
                    for (final Animal animal : animals)
                    {
                        if (herding.isCompatible(animal))
                        {
                            lootIds.addAll(herding.getLootTables(animal));
                        }
                    }
                }
                else if (module instanceof ICraftingBuildingModule crafting)
                {
                    lootIds.addAll(crafting.getAdditionalLootTables());
                }
            });
        }

        lootIds.add(ModLootTables.FISHING);
        lootIds.addAll(ModLootTables.FISHERMAN_BONUS.values());

        lootTables.clear();
        lootTables.putAll(lootIds.stream()
                .filter(Objects::nonNull)   // just in case
                .distinct()
                .collect(Collectors.toConcurrentMap(Function.identity(),
                        id -> LootTableAnalyzer.toDrops(lootTableManager, id))));
    }

    /**
     * Sends relevant Custom Recipes loaded from the Custom Recipe Manager to the client.
     * @param player the player to send the new data to.
     */
    public void sendCustomRecipeManagerPackets(final ServerPlayer player)
    {
        final FriendlyByteBuf recipeMgrFriendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        serializeNetworkData(recipeMgrFriendlyByteBuf);
        Network.getNetwork().sendToPlayer(new CustomRecipeManagerMessage(recipeMgrFriendlyByteBuf), player);
    }

    /**
     * Serializes a partial assembly of Custom Recipes.
     * This version sends the full Custom Recipe Manager.
     * @param recipeMgrFriendlyByteBuf packet buffer to encode the data into.
     */
    private void serializeNetworkData(final FriendlyByteBuf recipeMgrFriendlyByteBuf)
    {
        // Custom Recipe Manager packets can potentially get very large, and individual CompoundTags can not be parsed if they exceed 2MB.
        // For safety with arbitrary data packs (or sets of data packs), we can not wrap the entire CustomRecipeManager into single ListNBT.
        // Including all recipes in transfer results in total transfer size around ~670KB for just Minecolonies + Structurize recipes.
        // See CustomRecipeFactory.serialize for last tested numbers and more precise breakdown.
        recipeMgrFriendlyByteBuf.writeVarInt(recipeMap.size());
        for (Map.Entry<String, Map<ResourceLocation, CustomRecipe>> crafter : recipeMap.entrySet())
        {
            recipeMgrFriendlyByteBuf.writeVarInt(crafter.getValue().size());
            for (CustomRecipe recipe : crafter.getValue().values())
            {
                StandardFactoryController.getInstance().serialize(recipeMgrFriendlyByteBuf, recipe);
                //// NBT-based serialization for debugging/diagnosis only, as total packet size can be /very/ large.
                //recipeMgrFriendlyByteBuf.writeCompoundTag(StandardFactoryController.getInstance().serialize(recipe));
            }
        }

        recipeMgrFriendlyByteBuf.writeVarInt(lootTables.size());
        for (final Map.Entry<ResourceLocation, List<LootTableAnalyzer.LootDrop>> lootEntry : lootTables.entrySet())
        {
            recipeMgrFriendlyByteBuf.writeResourceLocation(lootEntry.getKey());
            recipeMgrFriendlyByteBuf.writeVarInt(lootEntry.getValue().size());
            for (final LootTableAnalyzer.LootDrop drop : lootEntry.getValue())
            {
                drop.serialize(recipeMgrFriendlyByteBuf);
            }
        }
    }

    /**
     * Ingests the custom recipes packet, and applies it to the recipe manager.
     * @param buff packet buffer containing the received data.
     */
    public void handleCustomRecipeManagerMessage(final FriendlyByteBuf buff)
    {
        reset();

        for (int crafterNum = buff.readVarInt(); crafterNum > 0; crafterNum--)
        {
            for (int recipeNum = buff.readVarInt(); recipeNum > 0; recipeNum--)
            {
                addRecipe(StandardFactoryController.getInstance().deserialize(buff));
                //// NBT-based serialization for debugging/diagnostics only, as total packet size can be very large.
                //addRecipe(StandardFactoryController.getInstance().deserialize(buff.readCompoundTag()));
            }
        }

        for (int lootNum = buff.readVarInt(); lootNum > 0; --lootNum)
        {
            final ResourceLocation id = buff.readResourceLocation();
            int count = buff.readVarInt();
            final List<LootTableAnalyzer.LootDrop> drops = new ArrayList<>(count);
            for (; count > 0; --count)
            {
                drops.add(LootTableAnalyzer.LootDrop.deserialize(buff));
            }
            lootTables.put(id, drops);
        }

        MinecraftForge.EVENT_BUS.post(new CustomRecipesReloadedEvent());
    }
}
