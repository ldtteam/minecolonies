package com.minecolonies.coremod.util;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
     * @param blueprint    the structureWrapper.
     * @param rotation   the rotation.
     * @param isMirrored if its mirrored.
     * @return a tuple with the required corners.
     */
    public static Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> calculateCorners(
      final BlockPos pos,
      final World world,
      final Blueprint blueprint,
      final int rotation,
      final boolean isMirrored)
    {
        blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(rotation), isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, world);
        final BlockPos zeroPos = pos.subtract(blueprint.getPrimaryBlockOffset());

        final int x1 = zeroPos.getX() - 1;
        final int z1 = zeroPos.getZ() - 1;
        final int x2 = zeroPos.getX() + blueprint.getSizeX();
        final int z2 = zeroPos.getZ() + blueprint.getSizeZ();

        return new Tuple<>(new Tuple<>(x1, x2), new Tuple<>(z1, z2));
    }
}
