package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IHasRequiredItemsModule;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingHospital;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Provides some basic definitions used by the animal herding AI (and JEI).
 */
public class AnimalHerdingModule extends AbstractBuildingModule implements IHasRequiredItemsModule
{
    private final JobEntry jobEntry;
    private final Predicate<Animal> animalPredicate;
    private final ItemStorage breedingItem;

    public AnimalHerdingModule(@NotNull final JobEntry jobEntry,
                               @NotNull final Predicate<Animal> animalPredicate,
                               @NotNull final ItemStorage breedingItem)
    {
        this.jobEntry = jobEntry;
        this.animalPredicate = animalPredicate;
        this.breedingItem = breedingItem;
    }

    /**
     * Gets the herding job associated with this module.
     *
     * @return The job.
     */
    @NotNull
    public IJob<?> getHerdingJob()
    {
        return jobEntry.produceJob(null);
    }

    /**
     * Check if this module handles the particular animal.
     *
     * @param animal the animal to check.
     * @return true if so.
     */
    public boolean isCompatible(@NotNull final Animal animal)
    {
        return animalPredicate.test(animal);
    }

    /**
     * Gets the item required to breed the animal.
     *
     * @return The animal's preferred breeding item (as a list of alternatives).
     */
    @NotNull
    public List<ItemStorage> getBreedingItems()
    {
        return Collections.singletonList(breedingItem);
    }

    /**
     * Gets a list of loot tables that should be available for drop
     * analysis.  This is not intended for actually generating loot,
     * just for display purposes such as in JEI (e.g. via {@link #getRecipesForDisplayPurposesOnly}).
     *
     * @param animal An example animal. (Don't use specific properties of this; it's only for checking type.)
     * @return The list of loot table ids
     */
    public List<ResourceKey<LootTable>> getLootTables(@NotNull final Animal animal)
    {
        return Collections.singletonList(animal.getLootTable());
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> map = new HashMap<>();
        map.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.axe.get(), TOOL_LEVEL_WOOD_OR_GOLD, building.getMaxEquipmentLevel()), new Tuple<>(1, true));
        map.put(stack -> getBreedingItems().contains(new ItemStorage(stack)), new Tuple<>(1, true));
        return map;
    }

    @Override
    public Map<ItemStorage, Integer> reservedStacksExcluding(final @Nullable IRequest<? extends IDeliverable> excluded)
    {
        return Collections.emptyMap();
    }

    /**
     * Get a list of "recipes" for items obtainable by herding the given animal.  This can include loot drops
     * for killing the animal as well as anything else acquired through other means.
     *
     * These are purely for JEI display purposes and don't have to represent actual crafting recipes.
     *
     * @param animal An example animal. (Don't use specific properties of this; it's only for checking type.)
     * @return the list of additional display recipes.
     */
    @NotNull
    public List<IGenericRecipe> getRecipesForDisplayPurposesOnly(@NotNull final Animal animal)
    {
        return List.of(GenericRecipe.builder()
                .withRecipeId(BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()))
            .withInputs(List.of(getBreedingItems().stream().map(ItemStorage::getItemStack).toList()))
            .withLootTable(animal.getLootTable())
            .withRequiredTool(ModEquipmentTypes.axe.get())
            .withRequiredEntity(animal.getType())
            .build());
    }
}
