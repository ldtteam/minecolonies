package com.minecolonies.coremod.util;

import com.ldtteam.structures.helpers.Structure;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Set;

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
     * @param wrapper    the structureWrapper.
     * @param rotation   the rotation.
     * @param isMirrored if its mirrored.
     * @return a tuple with the required corners.
     */
    public static Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> calculateCorners(
      final BlockPos pos,
      final World world,
      final Structure wrapper,
      final int rotation,
      final boolean isMirrored)
    {
        wrapper.rotate(BlockPosUtil.getRotationFromRotations(rotation), world, pos, isMirrored ? Mirror.FRONT_BACK : Mirror.NONE);
        wrapper.setPosition(pos);

        final int x1 = wrapper.getPosition().getX() - wrapper.getOffset().getX() - 1;
        final int z1 = wrapper.getPosition().getZ() - wrapper.getOffset().getZ() - 1;
        final int x2 = wrapper.getPosition().getX() + (wrapper.getWidth() - wrapper.getOffset().getX());
        final int z2 = wrapper.getPosition().getZ() + (wrapper.getLength() - wrapper.getOffset().getZ());

        return new Tuple<>(new Tuple<>(x1, x2), new Tuple<>(z1, z2));
    }

    /**
     * Sends a message to all given players.
     *
     * @param players the list of players
     * @param message the message to send
     */
    public static void sendToAll(Set<EntityPlayerMP> players, IMessage message)
    {
        for (final EntityPlayer player : players)
        {
            MineColonies.getNetwork().sendTo(message, (EntityPlayerMP) player);
        }
    }
}
