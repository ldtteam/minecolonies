package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.DownwardsGrowingPlantModule;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

/**
 * Planter module for growing {@link Items#GLOW_BERRIES}.
 */
public class GlowBerriesPlantModule extends DownwardsGrowingPlantModule
{
    /**
     * Default constructor.
     */
    public GlowBerriesPlantModule()
    {
        super("glowb_field", "berry", Items.GLOW_BERRIES);
    }

    @Override
    protected @NotNull Block getExpectedBlock()
    {
        return Blocks.CAVE_VINES;
    }
}
