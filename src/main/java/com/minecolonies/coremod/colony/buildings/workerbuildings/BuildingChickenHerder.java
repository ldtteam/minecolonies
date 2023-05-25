package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a new building for the Chicken Herder.
 */
public class BuildingChickenHerder extends AbstractBuilding
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String JOB = "chickenherder";

    /**
     * The hut name, used for the lang string in the GUI
     */
    private static final String HUT_NAME = "chickenherderhut";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingChickenHerder(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return JOB;
    }

    /**
     * Chicken herding module
     */
    public static class HerdingModule extends AnimalHerdingModule
    {
        public HerdingModule()
        {
            super(ModJobs.chickenHerder.get(), EntityType.CHICKEN, Chicken.class, new ItemStack(Items.WHEAT_SEEDS, 2));
        }

        @Override
        public @NotNull List<LootTableAnalyzer.LootDrop> getExpectedLoot()
        {
            final List<LootTableAnalyzer.LootDrop> drops = new ArrayList<>(super.getExpectedLoot());
            drops.add(new LootTableAnalyzer.LootDrop(Collections.singletonList(new ItemStack(Items.EGG)), 1, 0, false));
            return drops;
        }
    }
}
