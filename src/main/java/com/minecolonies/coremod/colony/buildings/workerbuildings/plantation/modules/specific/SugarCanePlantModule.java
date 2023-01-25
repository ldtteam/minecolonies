package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Planter module for growing {@link Items#SUGAR_CANE}.
 */
public class SugarCanePlantModule extends UpwardsGrowingPlantModule
{
    /**
     * Default constructor.
     */
    public SugarCanePlantModule()
    {
        super("sugar_field", "sugar", Items.SUGAR_CANE);
    }

    @Override
    protected boolean isValidBlock(final Block block)
    {
        return block == Blocks.SUGAR_CANE;
    }
}
