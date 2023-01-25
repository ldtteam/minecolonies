package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Planter module for growing {@link Items#CACTUS}.
 */
public class CactusPlantModule extends UpwardsGrowingPlantModule
{
    /**
     * Default constructor.
     */
    public CactusPlantModule()
    {
        super("cactus_field", "cactus", Items.CACTUS);
    }

    @Override
    protected boolean isValidBlock(final Block block)
    {
        return block == Blocks.CACTUS;
    }
}
