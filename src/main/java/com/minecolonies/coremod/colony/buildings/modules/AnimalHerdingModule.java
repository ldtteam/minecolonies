package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
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
     * Returns a list of expected loot from farming the animals.
     * Can be overridden if something other than just killing the animals happens.
     * This should *not* be used to actually generate loot; it's just informative.
     *
     * @param animal An example animal. (Don't use specific properties of this; it's only for checking type.)
     * @return The list of expected loot.
     */
    @NotNull
    public List<LootTableAnalyzer.LootDrop> getExpectedLoot(@NotNull final Animal animal)
    {
        return CustomRecipeManager.getInstance().getLootDrops(animal.getLootTable());
    }
}
