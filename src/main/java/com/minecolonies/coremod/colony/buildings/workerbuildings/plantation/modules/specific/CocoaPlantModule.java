package com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.specific;

import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.modules.generic.TreeSideFieldPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_JUNGLE;

/**
 * Planter module for growing {@link Blocks#COCOA}.
 */
public class CocoaPlantModule extends TreeSideFieldPlantModule
{
    /**
     * Default constructor.
     */
    public CocoaPlantModule()
    {
        super("cocoa_field", "cocoa", Blocks.COCOA);
    }

    @Override
    protected boolean isPlantMaxAge(final BlockState blockState)
    {
        Block block = blockState.getBlock();
        if (block instanceof CocoaBlock cocoa)
        {
            return !cocoa.isRandomlyTicking(blockState);
        }
        return false;
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_JUNGLE;
    }
}
