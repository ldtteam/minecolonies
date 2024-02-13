package com.minecolonies.api.colony.capability;

import com.minecolonies.api.util.CodecUtil;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.util.function.Function;
import java.util.function.Supplier;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * SavedData per chunk wrapper
 */
public class PerChunkSavedData<T> extends SavedData
{
    /**
     * Actual live data
     */
    private final Long2ObjectMap<T> chunkCapabilities = new Long2ObjectOpenHashMap<>();
    
    // type functions
    private final Function<CompoundTag, @Nullable T> decoder;
    private final Function<T, @Nullable CompoundTag> encoder;
    private final Supplier<T> factory;

    /**
     * @param constructor fresh type constructor - used when querying data for unseen chunk
     * @param codec type codec
     * @param logger logger for encoding/decoding errors
     */
    public static <T> Factory<PerChunkSavedData<T>> factory(final Supplier<T> constructor, final Codec<T> codec, final Logger logger)
    {
        final Function<CompoundTag, T> decoder = CodecUtil.nbtDecoder(codec, logger, () -> null);
        final Function<T, CompoundTag> encoder = CodecUtil.nbtEncoder(codec, logger, () -> null);
        return new Factory<>(() -> new PerChunkSavedData<>(decoder, encoder, constructor), tag -> new PerChunkSavedData<>(decoder, encoder, constructor).load(tag));
    }

    private PerChunkSavedData(final Function<CompoundTag, T> decoder,
        final Function<T, CompoundTag> encoder,
        final Supplier<T> factory)
    {
        this.decoder = decoder;
        this.encoder = encoder;
        this.factory = factory;
    }

    private PerChunkSavedData<T> load(final CompoundTag tag)
    {
        for (final long chunkPos : tag.getLongArray(TAG_POS))
        {
            final T decoded = decoder.apply(tag.getCompound(Long.toString(chunkPos)));
            if (decoded != null)
            {
                chunkCapabilities.put(chunkPos, decoded);
            }
        }
        return this;
    }

    @Override
    public CompoundTag save(final CompoundTag tag)
    {
        // write poses as array so we have typed parsing
        final long[] chunkPoses = chunkCapabilities.keySet().toLongArray();
        tag.putLongArray(TAG_POS, chunkPoses);
        for (final long chunkPos : chunkPoses)
        {
            final CompoundTag encoded = encoder.apply(chunkCapabilities.get(chunkPos));
            if (encoded != null)
            {
                tag.put(Long.toString(chunkPos), encoded);
            }
        }
        return tag;
    }

    public T get(final LevelChunk chunk)
    {
        return get(chunk.getPos());
    }

    public T get(final ChunkPos chunkPos)
    {
        return chunkCapabilities.computeIfAbsent(chunkPos.toLong(), key -> factory.get());
    }
}
