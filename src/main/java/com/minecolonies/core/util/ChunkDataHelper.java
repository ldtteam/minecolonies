package com.minecolonies.core.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.capability.IChunkmanagerCapability;
import com.minecolonies.api.colony.capability.IColonyManagerCapability;
import com.minecolonies.api.colony.capability.IColonyTagCapability;
import com.minecolonies.api.util.*;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.network.messages.client.UpdateChunkCapabilityMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.UNABLE_TO_FIND_WORLD_CAP_TEXT;
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
    public static void loadChunk(final LevelChunk chunk, final Level world)
    {
        // If colony is farther away from a capability then this times the default colony distance it will delete the capability.
        final int distanceToDelete = MineColonies.getConfig().getServer().maxColonySize.get() * BLOCKS_PER_CHUNK * 2 * 5;

        final IChunkmanagerCapability chunkManager = IChunkmanagerCapability.getCapability(world);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT, new Exception());
            return;
        }

        if (!chunkManager.getAllChunkStorages().isEmpty())
        {
            final IColonyManagerCapability cap = IColonyManagerCapability.getCapability(world);
            if (cap == null)
            {
                return;
            }

            final ChunkLoadStorage existingStorage = chunkManager.getChunkStorage(chunk.getPos().x, chunk.getPos().z);
            if (existingStorage != null)
            {
                addStorageToChunk(chunk, existingStorage);
            }
        }

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
     * Add a chunk storage to a chunk.
     *
     * @param chunk   the chunk to add it to.
     * @param storage the said storage.
     */
    public static void addStorageToChunk(final LevelChunk chunk, final ChunkLoadStorage storage)
    {
        if (chunk.getPos().equals(ChunkPos.ZERO))
        {
            Log.getLogger().warn("Trying to claim zero chunk!", new Exception());
        }

        final IColonyTagCapability cap = IColonyTagCapability.getCapability(chunk);
        storage.applyToCap(cap, chunk);

        if (cap != null)
        {
            new UpdateChunkCapabilityMessage(cap, chunk.getPos().x, chunk.getPos().z).sendToAllClients();
        }
    }

    /**
     * Notify all chunks in the range of the colony about the colony.
     *
     * @param world  the world.
     * @param add    if add or remove.
     * @param id     the colony id.
     * @param center the center chunk.
     */
    public static void claimColonyChunks(final Level world, final boolean add, final int id, final BlockPos center)
    {
        final int range = MineColonies.getConfig().getServer().initialColonySize.get();
        staticClaimInRange(id, add, center, add ? range : range * 2, world, false);
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
      final IColony colony, final boolean add, final BlockPos center, final int range,
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
        final IChunkmanagerCapability worldCapability = IChunkmanagerCapability.getCapability(w);
        if (worldCapability == null)
        {
            return true;
        }
        final LevelChunk centralChunk = w.getChunkAt(pos);
        final int chunkX = centralChunk.getPos().x;
        final int chunkZ = centralChunk.getPos().z;

        for (int i = chunkX - range; i <= chunkX + range; i++)
        {
            for (int j = chunkZ - range; j <= chunkZ + range; j++)
            {
                final LevelChunk chunk = w.getChunk(i, j);
                final IColonyTagCapability colonyCap = IColonyTagCapability.getCapability(chunk);
                if (colonyCap == null)
                {
                    return true;
                }
                final ChunkLoadStorage storage = worldCapability.getChunkStorage(chunk.getPos().x, chunk.getPos().z);
                if (storage != null)
                {
                    storage.applyToCap(colonyCap, chunk);
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
      final IColony colony,
      final boolean add,
      final int range,
      final BlockPos center,
      final boolean force)
    {
        final Level world = colony.getWorld();
        final IChunkmanagerCapability chunkManager = IChunkmanagerCapability.getCapability(world);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT, new Exception());
            return;
        }

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

                if (tryClaimBuilding(world, pos, add, colony, center, chunkManager))
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
      final IColony colony,
      final BlockPos anchor,
      final boolean add,
      final Tuple<BlockPos, BlockPos> corners)
    {
        final Level world = colony.getWorld();
        final IChunkmanagerCapability chunkManager = IChunkmanagerCapability.getCapability(world);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT, new Exception());
            return;
        }

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

            tryClaimBuilding(world, pos, add, colony, anchor, chunkManager);
        }
    }

    /**
     * Claim a number of chunks in a certain range around a position.
     *
     * @param colonyId the colony id.
     * @param add      if claim or unclaim.
     * @param center   the center position to be claimed.
     * @param range    the range.
     * @param world    the world.
     */
    public static void staticClaimInRange(
      final int colonyId,
      final boolean add,
      final BlockPos center,
      final int range,
      final Level world,
      final boolean forceOwnerChange)
    {
        final IChunkmanagerCapability chunkManager = IChunkmanagerCapability.getCapability(world);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT, new Exception());
            return;
        }

        final LevelChunk centralChunk = world.getChunkAt(center);

        final int chunkXMax = centralChunk.getPos().x;
        final int chunkZMax = centralChunk.getPos().z;

        for (int chunkPosX = chunkXMax - range; chunkPosX <= chunkXMax + range; chunkPosX++)
        {
            for (int chunkPosZ = chunkZMax - range; chunkPosZ <= chunkZMax + range; chunkPosZ++)
            {
                tryClaim(world, new BlockPos(chunkPosX * BLOCKS_PER_CHUNK, 0, chunkPosZ * BLOCKS_PER_CHUNK), add, colonyId, chunkManager, forceOwnerChange);
            }
        }
    }

    /**
     * Add the data to the chunk directly.
     *
     * @param world         the world.
     * @param chunkBlockPos the position.
     * @param add           if add or delete.
     * @param id            the id.
     * @param chunkManager  the chunk manager capability.
     * @return true if successful.
     */
    public static boolean tryClaim(
      final Level world,
      final BlockPos chunkBlockPos,
      final boolean add,
      final int id,
      final IChunkmanagerCapability chunkManager,
      boolean forceOwnerChange)
    {
        if (!WorldUtil.isBlockLoaded(world, chunkBlockPos))
        {
            final ChunkLoadStorage newStorage = new ChunkLoadStorage(id, ChunkPos.asLong(chunkBlockPos), add, world.dimension().location(), forceOwnerChange);
            chunkManager.addChunkStorage(SectionPos.blockToSectionCoord(chunkBlockPos.getX()), SectionPos.blockToSectionCoord(chunkBlockPos.getZ()), newStorage);
            return false;
        }

        final LevelChunk chunk = (LevelChunk) world.getChunk(chunkBlockPos);
        final IColonyTagCapability cap = IColonyTagCapability.getCapability(chunk);
        if (cap == null)
        {
            return false;
        }

        // Before directly adding cap data, apply data from our cache.
        final ChunkLoadStorage chunkLoadStorage = chunkManager.getChunkStorage(chunk.getPos().x, chunk.getPos().z);
        if (chunkLoadStorage != null)
        {
            chunkLoadStorage.applyToCap(cap, chunk);
        }

        if (add)
        {
            cap.addColony(id, chunk);
            if (forceOwnerChange)
            {
                cap.setOwningColony(id, chunk);
                final IColony colony = IColonyManager.getInstance().getColonyByDimension(id, world.dimension());
                if (colony != null)
                {
                    colony.addLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z), chunk);
                }
            }
        }
        else
        {
            cap.removeColony(id, chunk);
        }

        new UpdateChunkCapabilityMessage(cap, chunk.getPos().x, chunk.getPos().z).sendToPlayersTrackingChunk(chunk);
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
     * @param chunkManager  the chunk manager capability.
     * @return true if successful.
     */
    public static boolean tryClaimBuilding(
      final Level world,
      final BlockPos chunkBlockPos,
      final boolean add,
      final IColony colony,
      final BlockPos buildingPos,
      final IChunkmanagerCapability chunkManager)
    {
        if (!WorldUtil.isBlockLoaded(world, chunkBlockPos))
        {
            final ChunkLoadStorage newStorage = new ChunkLoadStorage(colony.getID(), ChunkPos.asLong(chunkBlockPos), world.dimension().location(), buildingPos, add);
            chunkManager.addChunkStorage(SectionPos.blockToSectionCoord(chunkBlockPos.getX()), SectionPos.blockToSectionCoord(chunkBlockPos.getZ()), newStorage);
            return false;
        }

        final LevelChunk chunk = world.getChunkAt(chunkBlockPos);
        final IColonyTagCapability cap = IColonyTagCapability.getCapability(chunk);
        if (cap == null)
        {
            return false;
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

        // Before directly adding cap data, apply data from our cache.
        final ChunkLoadStorage chunkLoadStorage = chunkManager.getChunkStorage(chunk.getPos().x, chunk.getPos().z);
        if (chunkLoadStorage != null)
        {
            chunkLoadStorage.applyToCap(cap, chunk);
        }

        if (add)
        {
            cap.addBuildingClaim(colony.getID(), buildingPos, chunk);
        }
        else
        {
            cap.removeBuildingClaim(colony.getID(), buildingPos, chunk);
        }

        new UpdateChunkCapabilityMessage(cap, chunk.getPos().x, chunk.getPos().z).sendToPlayersTrackingChunk(chunk);
        return true;
    }
}
