package com.minecolonies.core.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.claim.IChunkClaimData;
import com.minecolonies.api.util.*;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.Colony;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.api.util.constant.TranslationConstants.COLONY_SIZE_CHANGE;

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
     * @param world the worldg to.
     */
    public static void loadChunk(final LevelChunk chunk, final ServerLevel world)
    {
        final int closeColony = ColonyUtils.getOwningColony(chunk);
        if (closeColony != 0)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(closeColony, world.dimension());
            if (colony != null)
            {
                colony.addLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z), chunk);
            }
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
        final int closeColony = ColonyUtils.getOwningColony(chunk);
        if (closeColony != 0)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(closeColony, world.dimension());
            if (colony != null)
            {
                colony.removeLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z));
            }
        }
    }

    /**
     * Notify all chunks in the range of the colony about the colony.
     *
     * @param world  the world.
     * @param add    if add or remove.
     * @param colony the colony.
     * @param center the center chunk.
     */
    public static void claimColonyChunks(final ServerLevel world, final boolean add, final Colony colony, final BlockPos center)
    {
        final int range = MineColonies.getConfig().getServer().initialColonySize.get();
        staticClaimInRange(colony, add, center, add ? range : range * 2, world, false);
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
      final Colony colony, final boolean add, final BlockPos center, final int range,
      @Nullable final Tuple<BlockPos, BlockPos> corners)
    {
        buildingClaimInRange(colony, add, range, center, false);

        if (corners != null)
        {
            buildingClaimBox(colony, center, add, corners);
        }
    }

    /**
     * Check if all chunks within a certain range can be claimed, if range is too big this might require to load chunks. Use carefully.
     * <p>
     * --- This is only for dynamic claiming ---
     *
     * @param w     the world.
     * @param pos   the center position.
     * @param range the range to check.
     * @return true if possible.
     */
    public static boolean canClaimChunksInRange(final Level w, final BlockPos pos, final int range)
    {
        final LevelChunk centralChunk = w.getChunkAt(pos);
        final int chunkX = centralChunk.getPos().x;
        final int chunkZ = centralChunk.getPos().z;

        for (int i = chunkX - range; i <= chunkX + range; i++)
        {
            for (int j = chunkZ - range; j <= chunkZ + range; j++)
            {
                final LevelChunk chunk = w.getChunk(i, j);
                final IChunkClaimData colonyCap = IColonyManager.getInstance().getClaimData(w.dimension(), chunk.getPos());
                if (colonyCap == null)
                {
                    return true;
                }
                if (colonyCap.getOwningColony() != 0)
                {
                    return false;
                }
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
    private static void buildingClaimInRange(
      final Colony colony,
      final boolean add,
      final int range,
      final BlockPos center,
      final boolean force)
    {
        final ServerLevel world = (ServerLevel) colony.getWorld();
        final BlockPos colonyCenterCompare = new BlockPos(colony.getCenter().getX(), 0, colony.getCenter().getZ());

        final int chunkX = center.getX() >> 4;
        final int chunkZ = center.getZ() >> 4;

        final int maxColonySize = MineColonies.getConfig().getServer().maxColonySize.get();

        for (int i = chunkX - range; i <= chunkX + range; i++)
        {
            for (int j = chunkZ - range; j <= chunkZ + range; j++)
            {
                final BlockPos pos = new BlockPos(i * BLOCKS_PER_CHUNK, 0, j * BLOCKS_PER_CHUNK);
                if (!force && maxColonySize != 0 && pos.distSqr(colonyCenterCompare) > Math.pow(maxColonySize * BLOCKS_PER_CHUNK, 2))
                {
                    Log.getLogger()
                      .debug(
                        "Tried to claim chunk at pos X:" + pos.getX() + " Z:" + pos.getZ() + " too far away from the colony:" + colony.getID() + " center:" + colony.getCenter()
                          + " max is config workingRangeTownHall ^2");
                    continue;
                }

                if (tryClaimBuilding(world, pos, add, colony, center))
                {
                    continue;
                }
            }
        }

        if (add && range > 0)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(center);
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
      final Colony colony,
      final BlockPos anchor,
      final boolean add,
      final Tuple<BlockPos, BlockPos> corners)
    {
        final ServerLevel world = (ServerLevel) colony.getWorld();
        final int maxColonySize = MineColonies.getConfig().getServer().maxColonySize.get();
        final BlockPos colonyCenterCompare = new BlockPos(colony.getCenter().getX(), 0, colony.getCenter().getZ());

        for (final ChunkPos chunk : ChunkPos.rangeClosed(new ChunkPos(corners.getA()), new ChunkPos(corners.getB())).toList())
        {
            final BlockPos pos = chunk.getWorldPosition();
            if (maxColonySize != 0 && pos.distSqr(colonyCenterCompare) > Math.pow(maxColonySize * BLOCKS_PER_CHUNK, 2))
            {
                Log.getLogger()
                  .debug(
                    "Tried to claim chunk at pos X:" + pos.getX() + " Z:" + pos.getZ() + " too far away from the colony:" + colony.getID() + " center:" + colony.getCenter()
                      + " max is config workingRangeTownHall ^2");
                continue;
            }

            tryClaimBuilding(world, pos, add, colony, anchor);
        }
    }

    /**
     * Claim a number of chunks in a certain range around a position.
     *
     * @param colony   the colony.
     * @param add      if claim or unclaim.
     * @param center   the center position to be claimed.
     * @param range    the range.
     * @param world    the world.
     */
    public static void staticClaimInRange(
      final Colony colony,
      final boolean add,
      final BlockPos center,
      final int range,
      final ServerLevel world,
      final boolean forceOwnerChange)
    {
        final LevelChunk centralChunk = world.getChunkAt(center);

        final int chunkXMax = centralChunk.getPos().x;
        final int chunkZMax = centralChunk.getPos().z;

        for (int chunkPosX = chunkXMax - range; chunkPosX <= chunkXMax + range; chunkPosX++)
        {
            for (int chunkPosZ = chunkZMax - range; chunkPosZ <= chunkZMax + range; chunkPosZ++)
            {
                tryClaim(world, new BlockPos(chunkPosX * BLOCKS_PER_CHUNK, 0, chunkPosZ * BLOCKS_PER_CHUNK), add, colony, forceOwnerChange);
            }
        }
    }

    /**
     * Add the data to the chunk directly.
     *
     * @param world         the world.
     * @param chunkBlockPos the position.
     * @param add           if add or delete.
     * @param colony        the colony.
     * @return true if successful.
     */
    public static boolean tryClaim(
      final ServerLevel world,
      final BlockPos chunkBlockPos,
      final boolean add,
      final Colony colony,
      boolean forceOwnerChange)
    {
        final LevelChunk chunk = (LevelChunk) world.getChunk(chunkBlockPos);
        IChunkClaimData chunkClaimData = IColonyManager.getInstance().getClaimData(world.dimension(), chunk.getPos());
        final int id = colony.getID();
        if (chunkClaimData == null)
        {
            if (add)
            {
                chunkClaimData = colony.claimNewChunk(chunk.getPos());
            }
            else
            {
                return true;
            }
        }

        if (add)
        {
            chunkClaimData.addColony(id, chunk);
            if (forceOwnerChange)
            {
                chunkClaimData.setOwningColony(id, chunk);
                colony.addLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z), chunk);
            }
        }
        else
        {
            chunkClaimData.removeColony(id, chunk);
        }
        return true;
    }

    /**
     * Add the data to the chunk directly for dynamic claiming.
     * <p>
     * ----- Only for dynamic claiming -----
     *
     * @param world         the world.
     * @param chunkBlockPos the position.
     * @param add           if add or delete.
     * @param colony        the colony.
     * @param buildingPos   the building pos.
     * @return true if successful.
     */
    public static boolean tryClaimBuilding(
      final ServerLevel world,
      final BlockPos chunkBlockPos,
      final boolean add,
      final Colony colony,
      final BlockPos buildingPos)
    {
        final LevelChunk chunk = world.getChunkAt(chunkBlockPos);
        IChunkClaimData chunkClaimData = IColonyManager.getInstance().getClaimData(world.dimension(), chunk.getPos());;
        if (chunkClaimData == null)
        {
            if (add)
            {
                chunkClaimData = colony.claimNewChunk(chunk.getPos());
            }
            else
            {
                return false;
            }
        }

        if (chunk.getPos().equals(ChunkPos.ZERO))
        {
            if (chunk.getPos().equals(ChunkPos.ZERO))
            {
                if (colony == null || BlockPosUtil.getDistance2D(colony.getCenter(), BlockPos.ZERO) > 200)
                {
                    Log.getLogger().warn("Trying to claim at zero chunk pos!:", new Exception());
                }
            }
        }

        if (add)
        {
            chunkClaimData.addBuildingClaim(colony.getID(), buildingPos, chunk);
        }
        else
        {
            chunkClaimData.removeBuildingClaim(colony.getID(), buildingPos, chunk);
        }

        return true;
    }
}
