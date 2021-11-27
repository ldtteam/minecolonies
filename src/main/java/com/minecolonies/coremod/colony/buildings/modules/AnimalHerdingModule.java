package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Currently this module is only used to provide some metadata for JEI, but
 * in the future it could be accessed by animal herding AI too.
 */
public class AnimalHerdingModule extends AbstractBuildingModule
{
    private final JobEntry jobEntry;
    private final EntityType<?> animalType;
    private final ItemStack breedingItem;

    public AnimalHerdingModule(@NotNull final JobEntry jobEntry,
                               @NotNull final EntityType<?> animalType,
                               @NotNull final ItemStack breedingItem)
    {
        this.jobEntry = jobEntry;
        this.animalType = animalType;
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
        return jobEntry.getHandlerProducer().apply(null);
    }

    /**
     * Gets the animal type managed by this herder.
     *
     * @return The animal entity type.
     */
    @NotNull
    public EntityType<?> getAnimalType()
    {
        return animalType;
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
     * The loot table for killing the animal.
     *
     * @return The resource location of the loot table.
     */
    @NotNull
    public ResourceLocation getDefaultLootTable()
    {
        return getAnimalType().getDefaultLootTable();
    }

    /**
     * Returns a list of expected loot from farming the animals.
     * Can be overridden if something other than just killing the animals happens.
     * This should *not* be used to actually generate loot; it's just informative.
     *
     * @return The list of expected loot.
     */
    @NotNull
    public List<LootTableAnalyzer.LootDrop> getExpectedLoot()
    {
        return CustomRecipeManager.getInstance().getLootDrops(getDefaultLootTable());
    }
}
