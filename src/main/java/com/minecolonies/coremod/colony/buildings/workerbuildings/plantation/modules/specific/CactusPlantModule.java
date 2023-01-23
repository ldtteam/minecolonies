package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import net.minecraft.world.level.block.Blocks;

/**
 * Planter module for growing {@link Blocks#CACTUS}.
 */
public class CactusPlantModule extends UpwardsGrowingPlantModule
{
    /**
     * Default constructor.
     */
    public CactusPlantModule()
    {
        super("cactus_field", "cactus", Blocks.CACTUS);
    }
}
