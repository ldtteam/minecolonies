package com.minecolonies.api.util;

import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.minecolonies.api.colony.capability.IColonyTagCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.data.models.blockstates.VariantProperties.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;
import static com.minecolonies.api.util.constant.ColonyManagerConstants.NO_COLONY_ID;

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
     * @param rotMir     the rotation and mirror.
     * @return a tuple with the required corners.
     */
    public static Tuple<BlockPos, BlockPos> calculateCorners(
      final BlockPos pos,
      final Level world,
      final Blueprint blueprint,
      final RotationMirror rotMir)
    {
        if (blueprint == null)
        {
            return new Tuple<>(pos, pos);
        }

        blueprint.setRotationMirror(rotMir, world);
        final BlockPos zeroPos = pos.subtract(blueprint.getPrimaryBlockOffset());

        final BlockPos pos1 = new BlockPos(zeroPos.getX(), zeroPos.getY(), zeroPos.getZ());
        final BlockPos pos2 = new BlockPos(zeroPos.getX() + blueprint.getSizeX() - 1, zeroPos.getY() + blueprint.getSizeY() - 1, zeroPos.getZ() + blueprint.getSizeZ() - 1);

        return new Tuple<>(pos1, pos2);
    }

    /**
     * Get the owning colony from a chunk.
     * @param chunk the chunk to check.
     * @return the colony id.
     */
    public static int getOwningColony(final LevelChunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        return cap == null ? NO_COLONY_ID : cap.getOwningColony();
    }

    /**
     * Get all claiming buildings from the chunk.
     * @param chunk the chunk they are at.
     * @return the map from colony to building claims.
     */
    public static Map<Integer, Set<BlockPos>> getAllClaimingBuildings(final LevelChunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        return cap == null ? new HashMap<>() : cap.getAllClaimingBuildings();
    }

    /**
     * Get all static claims from a chunk.
     * @param chunk the chunk to get it from.
     * @return the list.
     */
    public static List<Integer> getStaticClaims(final LevelChunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        return cap == null ? new ArrayList<>() : cap.getStaticClaimColonies();
    }

    /**
     * Get comprehensive chunk ownership data.
     * @param chunk the chunk to get it from.
     * @return the ownership data, or null.
     */
    @Nullable
    public static ChunkCapData getChunkCapData(final LevelChunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        return cap == null ? new ChunkCapData(chunk.getPos().x, chunk.getPos().z) : new ChunkCapData(chunk.getPos().x, chunk.getPos().z, cap.getOwningColony(), cap.getStaticClaimColonies(), cap.getAllClaimingBuildings());
    }
}
