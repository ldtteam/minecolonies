package com.minecolonies.api.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class CodecUtil
{
    private CodecUtil()
    {
        throw new UnsupportedOperationException("utility class");
    }

    /**
     * Chunk pos codec
     */
    public static final Codec<ChunkPos> CHUNK_POS = Codec.INT_STREAM
        .comapFlatMap(stream -> Util.fixedSize(stream, 2).map(arr -> new ChunkPos(arr[0], arr[1])), pos -> IntStream.of(pos.x, pos.z))
        .stable();

    /**
     * @param codec type codec
     * @param logger logger for errors
     * @param instanceIfErrored will be returned from function if error occurs
     * @return decoding function
     */
    public static <T> Function<CompoundTag, T> nbtDecoder(final Codec<T> codec, final Logger logger, final Supplier<T> instanceIfErrored)
    {
        return nbt -> codec.decode(NbtOps.INSTANCE, nbt)
            .resultOrPartial(logger::error)
            .map(Pair::getFirst)
            .orElseGet(instanceIfErrored);
    }

    /**
     * @param codec type codec
     * @param logger logger for errors
     * @param instanceIfErrored will be returned from function if error occurs
     * @return encoding function
     */
    public static <T> Function<T, CompoundTag> nbtEncoder(final Codec<T> codec, final Logger logger, final Supplier<CompoundTag> instanceIfErrored)
    {
        return data -> codec.encodeStart(NbtOps.INSTANCE, data)
            .resultOrPartial(logger::error)
            .map(t -> t instanceof final CompoundTag c ? c : null)
            .orElseGet(instanceIfErrored);
    }

    /**
     * Set codec, backed by list codec
     */
    public static <T> Codec<Set<T>> set(final Codec<T> codec, final Function<Collection<T>, Set<T>> setFactory)
    {
        return codec.listOf().xmap(setFactory, List::copyOf);
    }

    /**
     * {@link ExtraCodecs#strictOptionalField(Codec, String, Object)} but with supplier and for collections
     */
    public static <T, A extends Collection<T>> MapCodec<A> strictOptionalField(final Codec<A> codec, final String fieldName, final Supplier<A> defaultSupplier)
    {
        return ExtraCodecs.strictOptionalField(codec, fieldName).xmap(opt -> opt.orElseGet(defaultSupplier), data -> data.isEmpty() ? Optional.empty() : Optional.of(data));
    }

    /**
     * {@link ExtraCodecs#strictOptionalField(Codec, String, Object)} but with supplier and for collections
     */
    public static <K, V, A extends Map<K, V>> MapCodec<A> strictOptionalFieldMap(final Codec<A> codec, final String fieldName, final Supplier<A> defaultSupplier)
    {
        return ExtraCodecs.strictOptionalField(codec, fieldName).xmap(opt -> opt.orElseGet(defaultSupplier), data -> data.isEmpty() ? Optional.empty() : Optional.of(data));
    }
}
