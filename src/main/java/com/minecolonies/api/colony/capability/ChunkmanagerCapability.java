package com.minecolonies.api.colony.capability;

import com.minecolonies.api.util.ChunkLoadStorage;
import com.minecolonies.api.util.CodecUtil;
import com.minecolonies.api.util.constant.Constants;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CHUNK_STORAGE;

/**
 * The implementation of the colonyTagCapability.
 */
public class ChunkmanagerCapability extends SavedData implements IChunkmanagerCapability
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<ChunkmanagerCapability> CODEC = RecordCodecBuilder.create(builder -> builder
        .group(CodecUtil.strictOptionalFieldMap(Codec.unboundedMap(CodecUtil.CHUNK_POS, ChunkLoadStorage.CODEC), TAG_CHUNK_STORAGE, HashMap::new).forGetter(cap -> cap.chunkStorages))
        .apply(builder, ChunkmanagerCapability::new));
    
    public static final String NAME = new ResourceLocation(Constants.MOD_ID, "chunk_manager").toDebugFileName();
    public static final Factory<ChunkmanagerCapability> FACTORY = new Factory<>(ChunkmanagerCapability::new, CodecUtil.nbtDecoder(CODEC, LOGGER, ChunkmanagerCapability::new));

    /**
     * Map of chunkPos to chunkLoadStorage.
     */
    private final Map<ChunkPos, ChunkLoadStorage> chunkStorages;

    private ChunkmanagerCapability()
    {
        this(new HashMap<>());
    }

    private ChunkmanagerCapability(final Map<ChunkPos, ChunkLoadStorage> chunkStorages)
    {
        this.chunkStorages = chunkStorages;
    }

    @Override
    public CompoundTag save(final CompoundTag tag)
    {
        return CodecUtil.nbtEncoder(CODEC, LOGGER, () -> tag).apply(this);
    }

    @Nullable
    @Override
    public ChunkLoadStorage getChunkStorage(final int chunkX, final int chunkZ)
    {
        return chunkStorages.remove(new ChunkPos(chunkX, chunkZ));
    }

    @Override
    public boolean addChunkStorage(final int chunkX, final int chunkZ, final ChunkLoadStorage storage)
    {
        final ChunkLoadStorage existingStorage = chunkStorages.get(new ChunkPos(chunkX, chunkZ));
        if (existingStorage == null)
        {
            chunkStorages.put(new ChunkPos(chunkX, chunkZ), storage);
            return false;
        }
        else
        {
            existingStorage.merge(storage);
            return true;
        }
    }

    @Override
    public Map<ChunkPos, ChunkLoadStorage> getAllChunkStorages()
    {
        return chunkStorages;
    }
}
