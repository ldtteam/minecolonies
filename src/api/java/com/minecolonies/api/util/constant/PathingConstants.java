package com.minecolonies.api.util.constant;

import net.minecraft.util.math.BlockPos;

/**
 * Pathing constants class.
 */
public final class PathingConstants
{
    //  Debug Output
    public static final int      DEBUG_VERBOSITY_NONE  = 0;
    public static final int      DEBUG_VERBOSITY_FULL  = 2;
    public static final Object   debugNodeMonitor      = new Object();
    public static final   BlockPos BLOCKPOS_IDENTITY     = new BlockPos(0, 0, 0);
    public static final   BlockPos BLOCKPOS_UP           = new BlockPos(0, 1, 0);
    public static final   BlockPos BLOCKPOS_DOWN         = new BlockPos(0, -1, 0);
    public static final   BlockPos BLOCKPOS_NORTH        = new BlockPos(0, 0, -1);
    public static final   BlockPos BLOCKPOS_SOUTH        = new BlockPos(0, 0, 1);
    public static final   BlockPos BLOCKPOS_EAST         = new BlockPos(1, 0, 0);
    public static final   BlockPos BLOCKPOS_WEST         = new BlockPos(-1, 0, 0);
    public static final   int      MAX_Y                 = 256;
    public static final   int      MIN_Y                 = 0;

    /**
     * Additional cost of jumping and dropping - base 1.
     */
    public static final double JUMP_DROP_COST = 2.5D;

    /**
     * Cost improvement of paths - base 1.
     */
    public static final double ON_PATH_COST = 0.5D;

    /**
     * Additional cost of swimming - base 1.
     */
    public static final double SWIM_COST = 10D;

    /**
     * Distance which is considered to be too close to a fence.
     */
    public static final double TOO_CLOSE_TO_FENCE = 0.1D;

    /**
     * Distance which is considered to be too far from a fence.
     */
    public static final double TOO_FAR_FROM_FENCE = 0.9D;

    /**
     * Shift x by this value to calculate the node key..
     */
    public static final int SHIFT_X_BY = 20;

    /**
     * Shift the y value by this to calculate the node key..
     */
    public static final int SHIFT_Y_BY      = 12;

    /**
     * Max jump height.
     */
    public static final double MAX_JUMP_HEIGHT = 1.3;

    /**
     * Half a block.
     */
    public static final double HALF_A_BLOCK    = 0.5;

    /**
     * Private constructor to hide implicit one.
     */
    private PathingConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}