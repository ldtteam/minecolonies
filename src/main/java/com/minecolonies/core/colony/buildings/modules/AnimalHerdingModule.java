package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Provides some basic definitions used by the animal herding AI (and JEI).
 */
public class AnimalHerdingModule extends AbstractBuildingModule
{
    private final JobEntry jobEntry;
    private final Predicate<Animal> animalPredicate;
    private final ItemStack breedingItem;

    public AnimalHerdingModule(@NotNull final JobEntry jobEntry,
                               @NotNull final Predicate<Animal> animalPredicate,
                               @NotNull final ItemStack breedingItem)
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
    public List<ItemStack> getBreedingItems()
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
    @NotNull
    public List<ResourceLocation> getLootTables(@NotNull final Animal animal)
    {
        return Collections.singletonList(animal.getLootTable());
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
        return Collections.singletonList(new GenericRecipe(ForgeRegistries.ENTITY_TYPES.getKey(animal.getType()),
                ItemStack.EMPTY,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(getBreedingItems()),
                0,
                Blocks.AIR,
                animal.getLootTable(),
                ModEquipmentTypes.axe.get(),
                animal.getType(),
                Collections.emptyList(),
                0));
    }
}
