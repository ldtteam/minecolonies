package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.UpwardsGrowingPlantModule;
import net.minecraft.world.level.block.Blocks;

/**
 * Planter module for growing {@link Blocks#SUGAR_CANE}.
 */
public class SugarCanePlantModule extends UpwardsGrowingPlantModule
{
    /**
     * Default constructor.
     */
    public SugarCanePlantModule()
    {
        super("sugar_field", "sugar", Blocks.SUGAR_CANE);
    }
}
