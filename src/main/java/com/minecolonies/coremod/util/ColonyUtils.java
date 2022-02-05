package com.minecolonies.coremod.util;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

/**
 * Contains colony specific utility.
 */
public final class ColonyUtils
{
    /**
     * Private constructor to hide implicit one.
     */
    private ColonyUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Calculated the corner of a building.
     *
     * @param pos        the central position.
     * @param world      the world.
     * @param blueprint  the structureWrapper.
     * @param settings   the placement settings.
     * @return a tuple with the required corners.
     */
    public static Tuple<BlockPos, BlockPos> calculateCorners(
            final BlockPos pos,
            final Level world,
            Blueprint blueprint,
            final PlacementSettings settings)
    {
        if (blueprint == null)
        {
            return new Tuple<>(pos, pos);
        }

        blueprint.rotateWithMirror(settings.getRotation(), settings.getMirror(), world);
        blueprint = BlueprintUtil.createWall(blueprint, settings.getWallExtents());

        final BlockPos zeroPos = pos.subtract(blueprint.getPrimaryBlockOffset());

        final BlockPos pos1 = new BlockPos(zeroPos.getX(), zeroPos.getY(), zeroPos.getZ());
        final BlockPos pos2 = new BlockPos(zeroPos.getX() + blueprint.getSizeX() - 1, zeroPos.getY() + blueprint.getSizeY() - 1, zeroPos.getZ() + blueprint.getSizeZ() - 1);

        return new Tuple<>(pos1, pos2);
    }
}
