package com.minecolonies.api.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Utility methods for dealing with voxel shapes
 */
public class ShapeUtil
{
    /**
     * Fast shape max for normal blocks
     *
     * @param shape
     * @param axis
     * @return
     */
    public static double max(final VoxelShape shape, final Direction.Axis axis)
    {
        if (shape == Shapes.block())
        {
            return 1.0;
        }

        // Note: in vanilla this is -infinity
        if (shape == Shapes.empty())
        {
            return 0;
        }

        return shape.max(axis);
    }

    /**
     * Fast shape min for normal Blocks
     *
     * @param shape
     * @param axis
     * @return
     */
    public static double min(final VoxelShape shape, final Direction.Axis axis)
    {
        if (shape == Shapes.block())
        {
            return 0.0;
        }

        // Note: in vanilla this is +infinity
        if (shape == Shapes.empty())
        {
            return 0;
        }

        return shape.min(axis);
    }

    /**
     * Check if a shape is empty
     *
     * @param shape
     * @return
     */
    public static boolean isEmpty(final VoxelShape shape)
    {
        if (shape == Shapes.block())
        {
            return false;
        }

        if (shape == Shapes.empty())
        {
            return true;
        }

        return shape.isEmpty();
    }

    /**
     * Get the start y of a voxelshape.
     *
     * @param bb  the voxelshape.
     * @param def the default if empty.
     * @return the start y.
     */
    public static double getStartY(final VoxelShape bb, final double def)
    {
        return isEmpty(bb) ? def : min(bb, Direction.Axis.Y);
    }

    /**
     * Get the end y of a voxelshape.
     *
     * @param bb  the voxelshape.
     * @param def the default if empty.
     * @return the end y.
     */
    public static double getEndY(final VoxelShape bb, final double def)
    {
        return isEmpty(bb) ? def : max(bb, Direction.Axis.Y);
    }
}
