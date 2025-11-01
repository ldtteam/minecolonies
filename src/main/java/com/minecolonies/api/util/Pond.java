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
    /*
     * Possible pond states, with SUBOPTIMAL introduced to recognize "flowing water" ponds
     * where it appears valid, but the water blocks below the surface are not source blocks.
     */
    public enum PondState {
        INVALID,
        SUBOPTIMAL,
        VALID
    }

    /**
     * The minimum pond requirements.
     */
    public static final int WATER_POOL_WIDTH_REQUIREMENT  = 5;
    public static final int WATER_DEPTH_REQUIREMENT       = 2;

    /**
     * Checks if on position "water" really is water, if the water is connected to land and if the pond is big enough (bigger then 20).
     *
     * @param world The world the player is in.
     * @param water The coordinate to check.
     * @param problematicPosition Will contain position of problematic block (if not null && pond was not found).
     * @return true if water.
     */
    public static PondState checkPond(@NotNull final BlockGetter world, @NotNull final BlockPos water, @Nullable final MutableBlockPos problematicPosition)
    {
        PondState worstPondState = PondState.VALID;

        for (final MutableBlockPos tempPos : BlockPos.spiralAround(water, (WATER_POOL_WIDTH_REQUIREMENT - 1) / 2, Direction.SOUTH, Direction.EAST))
        {
            PondState pondState = PondState.VALID;

            for (int y = 0; y < WATER_DEPTH_REQUIREMENT; y++)
            {
                pondState = checkWaterForFishing(world, tempPos.setY(tempPos.getY() - y));

                if (pondState == PondState.INVALID)
                {
                    if (problematicPosition != null)
                    {
                        problematicPosition.set(tempPos);
                    }
                    return PondState.INVALID;
                }
                else if (pondState == PondState.SUBOPTIMAL)
                {
                    worstPondState = PondState.SUBOPTIMAL;
                }

                // 70% chance to check, to on avg prefer cleared areas
                if (ColonyConstants.rand.nextInt(100) < 30)
                {
                    break;
                }
            }
        }

        return worstPondState;
    }

    /**
     * Checks if the water is fine for fishing, see vanilla FishingHook checks
     *
     * @param world
     * @param pos
     * @return
     */
    public static PondState checkWaterForFishing(final BlockGetter world, final BlockPos pos)
    {
        PondState pondState = PondState.INVALID;

        final BlockState state = world.getBlockState(pos);
        if (!state.isAir() && !state.is(Blocks.LILY_PAD))
        {
            FluidState fluidstate = state.getFluidState();

            if  (fluidstate.is(FluidTags.WATER) && state.getCollisionShape(world, pos).isEmpty()) 
            {
                if (fluidstate.isSource())
                {
                    pondState = PondState.VALID;
                }
                else
                {
                    pondState = PondState.SUBOPTIMAL;
                }
            };
        }

        return pondState;
    }
}
