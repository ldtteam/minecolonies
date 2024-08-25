package com.minecolonies.api.items.component;

import com.ldtteam.structurize.api.constants.Constants;
import com.minecolonies.core.items.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents
{
    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Constants.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Timestamp>> TIME_COMPONENT =
      savedSynced("timestamp", Timestamp.CODEC, Timestamp.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SupplyData>> SUPPLY_COMPONENT =
      savedSynced("supplies", SupplyData.CODEC, SupplyData.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LastPos>> LAST_POS_COMPONENT =
      savedSynced("last_pos", LastPos.CODEC, LastPos.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WarehouseSnapshot>> WAREHOUSE_SNAPSHOT_COMPONENT =
      savedSynced("warehouse_snapshot", WarehouseSnapshot.CODEC, WarehouseSnapshot.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RallyData>> RALLY_COMPONENT =
      savedSynced("rally", RallyData.CODEC, RallyData.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AdventureData>> ADVENTURE_COMPONENT =
      savedSynced("adventure", AdventureData.CODEC, AdventureData.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ColonyId>> COLONY_ID_COMPONENT =
      savedSynced("colonyid", ColonyId.CODEC, ColonyId.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Pos>> POS_COMPONENT =
      savedSynced("pos", Pos.CODEC, Pos.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Desc>> DESC_COMPONENT =
      savedSynced("desc", Desc.CODEC, Desc.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HutBlockData>> HUT_COMPONENT =
      savedSynced("hut", HutBlockData.CODEC, HutBlockData.STREAM_CODEC);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Bool>> BOOL_COMPONENT =
      savedSynced("bool", Bool.CODEC, Bool.STREAM_CODEC);

    private static <D> DeferredHolder<DataComponentType<?>, DataComponentType<D>> savedSynced(final String name,
      final Codec<D> codec,
      final StreamCodec<RegistryFriendlyByteBuf, D> streamCodec)
    {
        return REGISTRY.register(name,
          () -> DataComponentType.<D>builder().persistent(codec).networkSynchronized(streamCodec).build());
    }
}
