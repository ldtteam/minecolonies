package com.minecolonies.core.generation;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This is a HolderLookup.Provider that's populated on-demand during datagen, so that we
 * can look up loot tables for {@link com.minecolonies.core.colony.crafting.LootTableAnalyzer}.
 */
public class DatagenLootTableManager implements HolderLookup.Provider
{
    private final HolderLookup.Provider baseProvider;
    private final ResourceManager       dataPack;
    private final Registry<LootTable>   registry = new DynamicLoadingRegistry<>(Registries.LOOT_TABLE, Lifecycle.stable(), false, LootTable.DIRECT_CODEC);
    private final HolderLookup.Provider delegate;

    public DatagenLootTableManager(@NotNull final HolderLookup.Provider baseProvider,
                                   @NotNull final ResourceManager dataPack)
    {
        this.baseProvider = baseProvider;
        this.dataPack = dataPack;
        this.delegate = HolderLookup.Provider.create(Stream.concat(
            baseProvider.listRegistries().filter(lookup -> !lookup.key().equals(Registries.LOOT_TABLE)),
            Stream.of(registry)));
    }

    @NotNull
    @Override
    public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys()
    {
        return delegate.listRegistryKeys();
    }

    @NotNull
    @Override
    public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(@NotNull final ResourceKey<? extends Registry<? extends T>> registryId)
    {
        if (registryId.equals(Registries.LOOT_TABLE))
        {
            return delegate.lookup(registryId);
        }

        return baseProvider.lookup(registryId);
    }

    @NotNull
    @Override
    public <V> RegistryOps<V> createSerializationContext(@NotNull final DynamicOps<V> ops)
    {
        return baseProvider.createSerializationContext(ops);
    }

    /**
     * This is a {@link Registry} that will try to dynamically load the corresponding JSON file if not already found.
     * It's intended for use during datagen for registries that are not populated by default.
     * It does not implement everything needed for a registry; just the minimum required for purpose.
     * @param <T> The registry object type.
     */
    private class DynamicLoadingRegistry<T> extends MappedRegistry<T>
    {
        private final Codec<T> codec;

        public DynamicLoadingRegistry(@NotNull final ResourceKey<? extends Registry<T>> registryId,
                                      @NotNull final Lifecycle lifecycle,
                                      final boolean intrusive,
                                      @NotNull final Codec<T> codec)
        {
            super(registryId, lifecycle, intrusive);
            this.codec = codec;
        }

        @NotNull
        public Optional<Holder.Reference<T>> get(@NotNull final ResourceKey<T> id)
        {
            if (super.containsKey(id))
            {
                return super.get(id);
            }

            final Optional<T> table = dynamicLoad(id);
            return table.map(lt -> this.register(id, lt, RegistrationInfo.BUILT_IN));
        }

        private Optional<T> dynamicLoad(@NotNull final ResourceKey<T> id)
        {
            try
            {
                final Resource resource = dataPack.getResourceOrThrow(id.identifier());
                final DynamicOps<JsonElement> ops = createSerializationContext(JsonOps.INSTANCE);
                try (final var reader = resource.openAsReader())
                {
                    final JsonElement json = JsonParser.parseReader(reader);
                    return Optional.of(codec.parse(ops, json).getOrThrow());
                }
            }
            catch (IOException | IllegalArgumentException e)
            {
                return Optional.empty();
            }
        }
    }
}
