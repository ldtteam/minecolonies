package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ChunkLoadStorage;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.IColonyManagerCapability;
import com.minecolonies.coremod.network.messages.client.UpdateChunkCapabilityMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.*;
import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.coremod.MineColonies.*;

/**
 * Class to take care of chunk data helper.
 */
public final class ChunkDataHelper
{
    /**
     * If colony is farther away from a capability then this times the default colony distance it will delete the capability.
     */
    private static final int DISTANCE_TO_DELETE = MineColonies.getConfig().getCommon().maxColonySize.get() * BLOCKS_PER_CHUNK * 2 * 5;

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
    public static void loadChunk(final Chunk chunk, final World world)
    {
        final IChunkmanagerCapability chunkManager = world.getCapability(CHUNK_STORAGE_UPDATE_CAP, null).orElseGet(null);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT, new Exception());
            return;
        }

        if (!chunkManager.getAllChunkStorages().isEmpty())
        {
            final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).orElseGet(null);
            if (cap == null)
            {
                return;
            }

            final ChunkLoadStorage existingStorage = chunkManager.getChunkStorage(chunk.getPos().x, chunk.getPos().z);
            if (existingStorage != null)
            {
                addStorageToChunk(chunk, existingStorage);
            }
            else
            {
                if (MineColonies.getConfig().getCommon().fixOrphanedChunks.get())
                {
                    final IColonyTagCapability closeCap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                    if (closeCap != null)
                    {
                        boolean dirty = false;
                        for (final int colony : closeCap.getAllCloseColonies())
                        {
                            if (colony != 0 && (cap.getColony(colony) == null
                                                  ||  BlockPosUtil.getDistance2D(cap.getColony(colony).getCenter(), new BlockPos(chunk.getPos().x * BLOCKS_PER_CHUNK, 0, chunk.getPos().z * BLOCKS_PER_CHUNK)) > DISTANCE_TO_DELETE))
                            {
                                Log.getLogger().warn("Removing orphaned chunk at:  " + chunk.getPos().x * BLOCKS_PER_CHUNK + " 100 " + chunk.getPos().z * BLOCKS_PER_CHUNK);
                                closeCap.removeColony(colony, chunk);
                                dirty = true;
                            }
                        }
                        if (dirty)
                        {
                            Network.getNetwork().sendToEveryone(new UpdateChunkCapabilityMessage(closeCap, chunk.getPos().x, chunk.getPos().z));
                        }
                    }
                }
            }
        }

        final IColonyTagCapability closeCap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
        if (closeCap != null)
        {
            if (closeCap.getOwningColony() != 0)
            {
                final IColony colony = IColonyManager.getInstance().getColonyByDimension(closeCap.getOwningColony(), world.getDimension().getType().getId());
                if (colony != null)
                {
                    colony.addLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z));
                }
            }
        }
    }

    /**
     * Called when a chunk is unloaded
     * @param world the world it is unloading in.
     * @param chunk the chunk that is unloading.
     */
    public static void unloadChunk(final Chunk chunk, final World world)
    {
        final IColonyTagCapability closeCap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
        if (closeCap != null)
        {
            if (closeCap.getOwningColony() != 0)
            {
                final IColony colony = IColonyManager.getInstance().getColonyByDimension(closeCap.getOwningColony(), world.getDimension().getType().getId());
                if (colony != null)
                {
                    colony.removeLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z));
                }
            }
        }
    }

    /**
     * Add a chunk storage to a chunk.
     *
     * @param chunk   the chunk to add it to.
     * @param storage the said storage.
     */
    public static void addStorageToChunk(final Chunk chunk, final ChunkLoadStorage storage)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
        storage.applyToCap(cap, chunk);

        if (cap != null)
        {
            Network.getNetwork().sendToEveryone(new UpdateChunkCapabilityMessage(cap, chunk.getPos().x, chunk.getPos().z));
        }
    }

    /**
     * Load the chunk storages from the server into the world.
     *
     * @param world the world to load them to.
     */
    public static void loadChunkStorageToWorldCapability(final World world)
    {
        @NotNull final File chunkDir = new File(((ServerWorld) world).getSaveHandler().getWorldDirectory(), CHUNK_INFO_PATH);
        if (!chunkDir.exists())
        {
            return;
        }

        final IChunkmanagerCapability chunkManager = world.getCapability(CHUNK_STORAGE_UPDATE_CAP, null).orElseGet(null);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT, new Exception());
            return;
        }

        final File[] files = chunkDir.listFiles();
        if (files != null)
        {
            for (final File file : files)
            {
                @Nullable final CompoundNBT chunkData = BackUpHelper.loadNBTFromPath(file);
                if (chunkData != null)
                {
                    final ChunkLoadStorage storage = new ChunkLoadStorage(chunkData);
                    final int z = (int) (storage.getXz() >> 32);
                    final int x = (int) storage.getXz();

                    chunkManager.addChunkStorage(x, z, storage);
                    file.delete();
                }
            }
        }
    }

    /**
     * Notify all chunks in the range of the colony about the colony.
     *
     * @param world the world.
     * @param add if add or remove.
     * @param id the colony id.
     * @param center the center chunk.
     * @param dimension the dimension.
     */
    public static void claimColonyChunks(final World world, final boolean add, final int id, final BlockPos center, final int dimension)
    {
        final int range = getConfig().getCommon().initialColonySize.get();
        final int buffer = getConfig().getCommon().minColonyDistance.get();

        claimChunksInRange(id, dimension, add, center, range, buffer, world);
    }

    /**
     * Notify all chunks in the range of the colony about the colony.
     *
     * --- This is only for dynamic claiming ---
     *
     * @param colony the colony to claim for
     * @param add if add or remove.
     * @param center the center position of the colony.
     * @param range the range to claim.
     */
    public static void claimColonyChunks(final IColony colony, final boolean add, final BlockPos center, final int range)
    {
        claimChunksInRange(colony, add, range, center, false);
    }

    /**
     * Check if all chunks within a certain range can be claimed, if range is too big this might require to load chunks.
     * Use carefully.
     *
     * --- This is only for dynamic claiming ---
     *
     * @param w the world.
     * @param pos the center position.
     * @param range the range to check.
     * @return true if possible.
     */
    public static boolean canClaimChunksInRange(final World w, final BlockPos pos, final int range)
    {
        final IChunkmanagerCapability worldCapability = w.getCapability(CHUNK_STORAGE_UPDATE_CAP, null).orElseGet(null);
        if (worldCapability == null)
        {
            return true;
        }
        final Chunk centralChunk = w.getChunkAt(pos);
        final int chunkX = centralChunk.getPos().x;
        final int chunkZ = centralChunk.getPos().z;

        for (int i = chunkX - range; i <= chunkX + range; i++)
        {
            for (int j = chunkZ - range; j <= chunkZ + range; j++)
            {
                final Chunk chunk = w.getChunk(i, j);
                final IColonyTagCapability colonyCap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
                if (colonyCap == null)
                {
                    return false;
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
     * Claim a number of chunks in a certain range around a position.
     * Prevents the initial chunkradius from beeing unclaimed, unless forced.
     *
     * @param colony    the colony to claim for
     * @param add       if claim or unclaim.
     * @param range     the range.
     * @param center    the center position to be claimed.
     * @param force     whether to ignore restrictions.
     */
    public static void claimChunksInRange(
      final IColony colony,
      final boolean add,
      final int range,
      final BlockPos center,
      final boolean force)
    {
        final World world = colony.getWorld();
        final int colonyId = colony.getID();
        final int dimension = colony.getDimension();

        final IChunkmanagerCapability chunkManager = world.getCapability(CHUNK_STORAGE_UPDATE_CAP, null).orElseGet(null);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT, new Exception());
            return;
        }

        final int chunkColonyCenterX = colony.getCenter().getX() >> 4;
        final int chunkColonyCenterZ = colony.getCenter().getZ() >> 4;
        final BlockPos colonyCenterCompare = new BlockPos(colony.getCenter().getX(), 0, colony.getCenter().getZ());

        final int chunkX = center.getX() >> 4;
        final int chunkZ = center.getZ() >> 4;

        for (int i = chunkX - range; i <= chunkX + range; i++)
        {
            for (int j = chunkZ - range; j <= chunkZ + range; j++)
            {
                // Initial chunk unclaim not allowed for dynamic(building removal)
                if (!force && !add && (Math.abs(chunkColonyCenterX - i) <= getConfig().getCommon().initialColonySize.get()
                                         && Math.abs(chunkColonyCenterZ - j) <= getConfig().getCommon().initialColonySize
                                                                                  .get()))
                {
                    Log.getLogger().debug("Unclaim of initial chunk prevented");
                    continue;
                }

                final BlockPos pos = new BlockPos(i * BLOCKS_PER_CHUNK, 0, j * BLOCKS_PER_CHUNK);
                if (!force && getConfig().getCommon().maxColonySize.get() != 0
                      && pos.distanceSq(colonyCenterCompare) > Math.pow(getConfig().getCommon().maxColonySize.get() * BLOCKS_PER_CHUNK, 2))
                {
                    Log.getLogger()
                      .debug(
                        "Tried to claim chunk at pos X:" + pos.getX() + " Z:" + pos.getZ() + " too far away from the colony:" + colony.getID() + " center:" + colony.getCenter()
                              + " max is config workingRangeTownHall ^2");
                    continue;
                }

                if (loadChunkAndAddData(world, pos, add, colonyId, center, chunkManager))
                {
                    continue;
                }

                @NotNull final ChunkLoadStorage newStorage = new ChunkLoadStorage(colonyId, ChunkPos.asLong(i, j), dimension, center);
                chunkManager.addChunkStorage(i, j, newStorage);
            }
        }
    }

    /**
     * Claim a number of chunks in a certain range around a position.
     *
     * @param colonyId  the colony id.
     * @param dimension the dimension.
     * @param add       if claim or unclaim.
     * @param center    the center position to be claimed.
     * @param range     the range.
     * @param buffer    the buffer.
     * @param world     the world.
     */
    public static void claimChunksInRange(
      final int colonyId,
      final int dimension,
      final boolean add,
      final BlockPos center,
      final int range,
      final int buffer,
      final World world)
    {
        final IChunkmanagerCapability chunkManager = world.getCapability(CHUNK_STORAGE_UPDATE_CAP, null).orElseGet(null);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT, new Exception());
            return;
        }

        final Chunk centralChunk = world.getChunkAt(center);
        loadChunkAndAddData(world, center, add, colonyId, chunkManager);

        final int chunkX = centralChunk.getPos().x;
        final int chunkZ = centralChunk.getPos().z;

        final int maxRange = range * 2 + buffer;
        for (int i = chunkX - maxRange; i <= chunkX + maxRange; i++)
        {
            for (int j = chunkZ - maxRange; j <= chunkZ + maxRange; j++)
            {
                if (i == chunkX && j == chunkZ)
                {
                    continue;
                }

                if (i >= chunkX - DISTANCE_TO_LOAD_IMMEDIATELY && j >= chunkZ - DISTANCE_TO_LOAD_IMMEDIATELY && i <= chunkX + DISTANCE_TO_LOAD_IMMEDIATELY
                      && j <= chunkZ + DISTANCE_TO_LOAD_IMMEDIATELY
                      && loadChunkAndAddData(world, new BlockPos(i * BLOCKS_PER_CHUNK, 0, j * BLOCKS_PER_CHUNK), add, colonyId, chunkManager))
                {
                    continue;
                }

                final boolean owning = i >= chunkX - range && j >= chunkZ - range && i <= chunkX + range && j <= chunkZ + range;
                @NotNull final ChunkLoadStorage newStorage = new ChunkLoadStorage(colonyId, ChunkPos.asLong(i, j), add, dimension, owning);
                chunkManager.addChunkStorage(i, j, newStorage);
            }
        }
    }

    /**
     * This is a utility methods to detect chunks which are claimed in a certain range.
     *
     * @param chunkX the chunkX starter position.
     * @param chunkZ the chunkZ starter position.
     * @param range  the range.
     * @param buffer the buffer.
     * @param world  the world.
     */
    public static void debugChunksInRange(final int chunkX, final int chunkZ, final int range, final int buffer, final World world)
    {
        final IChunkmanagerCapability chunkManager = world.getCapability(CHUNK_STORAGE_UPDATE_CAP, null).orElseGet(null);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT, new Exception());
            return;
        }

        final int maxRange = range * 2 + buffer;
        for (int i = chunkX - maxRange; i <= chunkX + maxRange; i++)
        {
            for (int j = chunkZ - maxRange; j <= chunkZ + maxRange; j++)
            {
                final BlockPos pos = new BlockPos(i * BLOCKS_PER_CHUNK, 0, j * BLOCKS_PER_CHUNK);
                final Chunk chunk = world.getChunkAt(pos);
                if (chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null).getOwningColony() != 0)
                {
                    Log.getLogger().warn("Has owner: " + pos.toString());
                }
            }
        }
    }

    /**
     * Add the data to the chunk directly.
     *
     * @param world the world.
     * @param pos   the position.
     * @param add   if add or delete.
     * @param id    the id.
     * @param chunkManager the chunk manager capability.
     * @return true if successful.
     */
    public static boolean loadChunkAndAddData(final World world, final BlockPos pos, final boolean add, final int id, final IChunkmanagerCapability chunkManager)
    {
        if (!world.isBlockPresent(pos))
        {
            return false;
        }

        final Chunk chunk = (Chunk) world.getChunk(pos);

        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);

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

        if (cap.getOwningColony() == id && add)
        {
            return true;
        }

        if (add)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(id, world.getDimension().getType().getId());
            if (colony != null)
            {
                colony.addLoadedChunk(ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z));
            }
            cap.setOwningColony(id, chunk);
            cap.addColony(id, chunk);
        }
        else
        {
            cap.removeColony(id, chunk);
        }

        Network.getNetwork().sendToEveryone(new UpdateChunkCapabilityMessage(cap, chunk.getPos().x, chunk.getPos().z));
        return true;
    }

    /**
     * Add the data to the chunk directly for dynamic claiming.
     *
     * ----- Only for dynamic claiming -----
     *
     * @param world the world.
     * @param pos   the position.
     * @param add   if add or delete.
     * @param id    the id.
     * @param buildingPos the building pos.
     * @param chunkManager the chunk manager capability.
     * @return true if successful.
     */
    public static boolean loadChunkAndAddData(
      final World world,
      final BlockPos pos,
      final boolean add,
      final int id,
      final BlockPos buildingPos,
      final IChunkmanagerCapability chunkManager)
    {
        if (!world.isBlockPresent(pos))
        {
            return false;
        }

        final Chunk chunk = world.getChunkAt(pos);
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
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
            cap.addBuildingClaim(id, buildingPos, chunk);
        }
        else
        {
            cap.removeBuildingClaim(id, buildingPos, chunk);
        }

        Network.getNetwork().sendToEveryone(new UpdateChunkCapabilityMessage(cap, chunk.getPos().x, chunk.getPos().z));
        return true;
    }
}
