package com.minecolonies.coremod.placementhandlers;

import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for all kinds of placements.
 */
public interface IPlacementHandler
{
    enum ActionProcessingResult
    {
        ACCEPT,
        DENY,
        IGNORE,
        REQUEST
    }
    /**
     * Method used to handle the processing of a Placement of a block.
     * @param world receives the world.
     * @param pos the position.
     * @param blockState the blockState.
     * @param placer the placer of the block.
     * @return ACCEPT, DENY or IGNORE.
     */
    Object handle(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState blockState, @Nullable AbstractEntityAIStructure<?> placer);
}
