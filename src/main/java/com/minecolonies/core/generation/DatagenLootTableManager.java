package com.minecolonies.core.generation;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * This is a HolderLookup.Provider that's populated on-demand during datagen, so that we
 * can look up loot tables for {@link com.minecolonies.core.colony.crafting.LootTableAnalyzer}.
 */
public class DatagenLootTableManager implements HolderLookup.Provider
{
    private final HolderLookup.Provider baseProvider;
    private final ExistingFileHelper    existingFileHelper;
    private final Registry<LootTable>   registry = new DynamicLoadingRegistry<>(Registries.LOOT_TABLE, Lifecycle.stable(), false);

    public DatagenLootTableManager(@NotNull final HolderLookup.Provider baseProvider,
                                   @NotNull final ExistingFileHelper existingFileHelper)
    {
        this.baseProvider = baseProvider;
        this.existingFileHelper = existingFileHelper;
    }

    @NotNull
    @Override
    public Stream<ResourceKey<? extends Registry<?>>> listRegistries()
    {
        return baseProvider.listRegistries();
    }

    @NotNull
    @Override
    public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(@NotNull final ResourceKey<? extends Registry<? extends T>> registryId)
    {
        if (registryId.equals(Registries.LOOT_TABLE))
        {
            return Optional.of((HolderLookup.RegistryLookup<T>) registry.asLookup());
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
        public DynamicLoadingRegistry(@NotNull final ResourceKey<? extends Registry<T>> registryId,
                                      @NotNull final Lifecycle lifecycle,
                                      final boolean intrusive)
        {
            super(registryId, lifecycle, intrusive);
        }

        @NotNull
        @Override
        public Optional<Holder.Reference<T>> getHolder(@NotNull final ResourceKey<T> id)
        {
            if (super.containsKey(id))
            {
                return super.getHolder(id);
            }

            final Optional<T> table = dynamicLoad(id);
            return table.map(lt -> this.register(id, lt, RegistrationInfo.BUILT_IN));
        }

        private Optional<T> dynamicLoad(@NotNull final ResourceKey<T> id)
        {
            try
            {
                final Resource resource = existingFileHelper.getResource(id.location(), PackType.SERVER_DATA, ".json", Registries.elementsDirPath(key()));
                final DynamicOps<JsonElement> ops = createSerializationContext(JsonOps.INSTANCE);
                try (final var reader = resource.openAsReader())
                {
                    final JsonElement json = JsonParser.parseReader(reader);
                    return Optional.of(byNameCodec().decode(ops, json).getOrThrow().getFirst());
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                return Optional.empty();
            }
        }
    }
}
