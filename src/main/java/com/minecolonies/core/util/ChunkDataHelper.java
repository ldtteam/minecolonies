package com.minecolonies.core.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.claims.ClaimReason;
import com.minecolonies.api.colony.claims.UnclaimReason;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.api.util.constant.TranslationConstants.COLONY_SIZE_CHANGE;
import static com.minecolonies.core.MineColonies.getConfig;

/**
 * Class to take care of chunk data helper.
 */
public final class ChunkDataHelper
{
    /**
     * Private constructor to hide implicit one.
     */
    private ChunkDataHelper()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Load the colony info for a certain chunk.
     *
     * @param chunk the chunk.
     * @param world the world.
     */
    public static void loadChunk(final LevelChunk chunk, final Level world)
    {
        final IColony colony = IColonyManager.getInstance().getOwningColony(world, chunk);
        if (colony != null)
        {
            colony.addLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z), chunk);
        }
    }

    /**
     * Called when a chunk is unloaded
     *
     * @param world the world it is unloading in.
     * @param chunk the chunk that is unloading.
     */
    public static void unloadChunk(final LevelChunk chunk, final Level world)
    {
        final IColony colony = IColonyManager.getInstance().getOwningColony(world, chunk);
        if (colony != null)
        {
            colony.removeLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z));
        }
    }

    /**
     * Notify all chunks in the range of the colony about the colony.
     * <p>
     * --- This is only for dynamic claiming ---
     *
     * @param colony  the colony to claim for
     * @param add     if add or remove.
     * @param center  the center position of the colony.
     * @param range   the range to claim.
     * @param corners also (un)claim all chunks intersecting this box (if not null)
     */
    public static void claimBuildingChunks(
        final IColony colony,
        final boolean add,
        final BlockPos center,
        final int range,
        @Nullable final Tuple<BlockPos, BlockPos> corners)
    {
        buildingClaimInRange(colony, add, range, center, false);

        if (corners != null)
        {
            buildingClaimBox(colony, center, add, corners);
        }
    }

    /**
     * Check if all chunks within a certain range are currently unclaimed.
     *
     * @param w     the world.
     * @param pos   the center position.
     * @param range the range to check.
     * @return true if none of the chunks in range are claimed.
     */
    public static boolean canClaimChunksInRange(final Level w, final BlockPos pos, final int range)
    {
        for (final long chunkPos : getChunksInRange(pos, range))
        {
            if (IColonyManager.getInstance().getOwningColony(w, chunkPos) != null)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Claim a number of chunks in a certain range around a position. Prevents the initial chunkradius from beeing unclaimed, unless forced.
     *
     * @param colony the colony to claim for
     * @param add    if claim or unclaim.
     * @param range  the range.
     * @param center the center position to be claimed.
     * @param force  whether to ignore restrictions.
     */
    private static void buildingClaimInRange(final IColony colony, final boolean add, final int range, final BlockPos center, final boolean force)
    {
        final BlockPos colonyCenterCompare = new BlockPos(colony.getCenter().getX(), 0, colony.getCenter().getZ());
        final ChunkPos colonyCenterChunk = new ChunkPos(colonyCenterCompare);
        final int maxColonySize = getConfig().getServer().maxColonySize.get();

        for (final long chunkPosLong : getChunksInRange(center, range))
        {
            final ChunkPos chunkPos = new ChunkPos(chunkPosLong);

            if (!force && maxColonySize != 0 && BlockPosUtil.chunkDistanceSquared(colonyCenterChunk, chunkPos) > maxColonySize * maxColonySize)
            {
                Log.getLogger()
                    .debug("Tried to claim chunk at pos X:" + chunkPos.x + " Z:" + chunkPos.z + " too far away from the colony:" + colony.getID() + " center:" + colony.getCenter()
                        + " max is config workingRangeTownHall ^2");
                continue;
            }

            if (add)
            {
                IColonyManager.getInstance().tryClaimChunkForColony(colony.getWorld(), chunkPosLong, colony, ClaimReason.building(center));
            }
            else
            {
                IColonyManager.getInstance().unclaimChunkForColony(colony.getWorld(), chunkPosLong, colony, UnclaimReason.building(center));
            }
        }

        if (add && range > 0)
        {
            final IBuilding building = colony.getServerBuildingManager().getBuilding(center);
            MessageUtils.format(COLONY_SIZE_CHANGE, range, building.getSchematicName()).sendTo(colony).forManagers();
        }
    }

    /**
     * (Un)Claim all chunks within the given box for a specific building.
     *
     * @param colony  the colony to claim for
     * @param anchor  the building anchor to claim for
     * @param add     if claim or unclaim.
     * @param corners the box.
     */
    private static void buildingClaimBox(
      final IColony colony,
      final BlockPos anchor,
      final boolean add,
      final Tuple<BlockPos, BlockPos> corners)
    {
        final int maxColonySize = getConfig().getServer().maxColonySize.get();
        final BlockPos colonyCenterCompare = new BlockPos(colony.getCenter().getX(), 0, colony.getCenter().getZ());

        for (final long chunkPosLong : getChunksInBox(corners))
        {
            final ChunkPos chunkPos = new ChunkPos(chunkPosLong);
            final BlockPos pos = chunkPos.getWorldPosition();
            if (maxColonySize != 0 && pos.distSqr(colonyCenterCompare) > Math.pow(maxColonySize * BLOCKS_PER_CHUNK, 2))
            {
                Log.getLogger()
                  .debug(
                    "Tried to claim chunk at pos X:" + pos.getX() + " Z:" + pos.getZ() + " too far away from the colony:" + colony.getID() + " center:" + colony.getCenter()
                      + " max is config workingRangeTownHall ^2");
                continue;
            }

            if (add)
            {
                IColonyManager.getInstance().tryClaimChunkForColony(colony.getWorld(), chunkPosLong, colony, ClaimReason.building(anchor));
            }
            else
            {
                IColonyManager.getInstance().unclaimChunkForColony(colony.getWorld(), chunkPosLong, colony, UnclaimReason.building(anchor));
            }
        }
    }

    /**
     * Computes the set of chunk positions within the given range (in chunks) of the given block position. Just does the math,
     * doesn't need to look anything up in the world.
     *
     * @param center the center position.
     * @param range  the range, in chunks.
     * @return the chunk positions, as {@code ChunkPos.asLong(x, z)}.
     */
    public static Set<Long> getChunksInRange(final BlockPos center, final int range)
    {
        final Set<Long> result = new HashSet<>();
        final int chunkX = center.getX() >> 4;
        final int chunkZ = center.getZ() >> 4;

        for (int i = chunkX - range; i <= chunkX + range; i++)
        {
            for (int j = chunkZ - range; j <= chunkZ + range; j++)
            {
                result.add(ChunkPos.asLong(i, j));
            }
        }
        return result;
    }

    /**
     * Computes the set of chunk positions intersecting the given block-position box. Just does the math, doesn't need to look
     * anything up in the world.
     *
     * @param corners the box.
     * @return the chunk positions, as {@code ChunkPos.asLong(x, z)}.
     */
    public static Set<Long> getChunksInBox(final Tuple<BlockPos, BlockPos> corners)
    {
        final Set<Long> result = new HashSet<>();
        for (final ChunkPos chunk : ChunkPos.rangeClosed(new ChunkPos(corners.getA()), new ChunkPos(corners.getB())).toList())
        {
            result.add(chunk.toLong());
        }
        return result;
    }
}
