package com.minecolonies.api.util;

import com.minecolonies.api.util.constant.ColonyConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class to search for fisher ponds.
 */
public final class Pond
{
    /**
     * The minimum pond requirements.
     */
    public static final int WATER_POOL_WIDTH_REQUIREMENT  = 7;
    public static final int WATER_DEPTH_REQUIREMENT       = 2;

    /**
     * Checks if on position "water" really is water, if the water is connected to land and if the pond is big enough (bigger then 20).
     *
     * @param world The world the player is in.
     * @param water The coordinate to check.
     * @return true if water.
     */
    public static boolean checkPond(@NotNull final BlockGetter world, @NotNull final BlockPos water, @Nullable final MutableBlockPos problematicPosition)
    {
        for (final MutableBlockPos tempPos : BlockPos.spiralAround(water, (WATER_POOL_WIDTH_REQUIREMENT - 1) / 2, Direction.SOUTH, Direction.EAST))
        {
            if (!isWaterForFishing(world, tempPos))
            {
                if (problematicPosition != null)
                {
                    problematicPosition.set(tempPos);
                }
                return false;
            }

            for (int y = 1; y < WATER_DEPTH_REQUIREMENT; y++)
            {
                // 70% chance to check, to on avg prefer cleared areas
                if (ColonyConstants.rand.nextInt(100) < 30)
                {
                    break;
                }

                if (!isWaterForFishing(world, tempPos.setY(tempPos.getY() - y)))
                {
                    if (problematicPosition != null)
                    {
                        problematicPosition.set(tempPos);
                    }
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if the water is fine for fishing, see vanilla FishingHook checks
     *
     * @param world
     * @param pos
     * @return
     */
    public static boolean isWaterForFishing(final BlockGetter world, final BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        if (!state.isAir() && !state.is(Blocks.LILY_PAD))
        {
            FluidState fluidstate = state.getFluidState();
            return fluidstate.is(FluidTags.WATER) && fluidstate.isSource() && state.getCollisionShape(world, pos).isEmpty();
        }

        return false;
    }
}
