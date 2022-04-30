package com.minecolonies.coremod.colony.buildings.modules;

import com.google.common.collect.Sets;
import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.coremod.util.ChunkDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.minecolonies.api.util.constant.ColonyConstants.KEEP_LOADED_TYPE;
import static com.minecolonies.coremod.MineColonies.CHUNK_STORAGE_UPDATE_CAP;

public class RemoteAreaChunkLoadingModule extends AbstractBuildingModule implements IPersistentModule, ITickingModule
{

    private static final String TAG_CHUNKS_TO_KEEP_LOADED = "chunkToKeepLoaded";
    private final Set<ChunkPos> chunksToKeepLoaded     = Sets.newHashSet();

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        chunksToKeepLoaded.clear();
        final long[] chunkPositionsTag = compound.getLongArray(TAG_CHUNKS_TO_KEEP_LOADED);
        for (final long chunkPos : chunkPositionsTag) {
            chunksToKeepLoaded.add(new ChunkPos(chunkPos));
        }
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        compound.putLongArray(TAG_CHUNKS_TO_KEEP_LOADED, chunksToKeepLoaded.stream().mapToLong(ChunkPos::toLong).toArray());
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        for (final ChunkPos chunkPos : chunksToKeepLoaded)
        {
            ((ServerChunkCache) colony.getWorld().getChunkSource()).addRegionTicket(KEEP_LOADED_TYPE, chunkPos, 2, chunkPos);
        }
    }

    public void addChunkToClaim(final ChunkPos chunkPos) {
        this.chunksToKeepLoaded.add(chunkPos);

        final IChunkmanagerCapability chunkManager = building.getColony().getWorld().getCapability(CHUNK_STORAGE_UPDATE_CAP, null).resolve().orElse(null);
        ((ServerChunkCache) building.getColony().getWorld().getChunkSource()).addRegionTicket(KEEP_LOADED_TYPE, chunkPos, 2, chunkPos);
        ChunkDataHelper.loadChunkAndAddData(building.getColony().getWorld(), chunkPos.getBlockAt(0,0,0), true, building.getColony().getID(), chunkManager);
    }

    public void removeChunkToClaim(final ChunkPos chunkPos) {
        this.chunksToKeepLoaded.remove(chunkPos);

        final IChunkmanagerCapability chunkManager = building.getColony().getWorld().getCapability(CHUNK_STORAGE_UPDATE_CAP, null).resolve().orElse(null);
        ((ServerChunkCache) building.getColony().getWorld().getChunkSource()).removeRegionTicket(KEEP_LOADED_TYPE, chunkPos, 2, chunkPos);
        ChunkDataHelper.loadChunkAndAddData(building.getColony().getWorld(), chunkPos.getBlockAt(0,0,0), false, building.getColony().getID(), chunkManager);
    }
}
