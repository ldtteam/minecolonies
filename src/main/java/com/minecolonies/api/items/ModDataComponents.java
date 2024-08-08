package com.minecolonies.api.items;

import com.ldtteam.structurize.api.constants.Constants;
import com.minecolonies.core.items.ItemScanAnalyzer;
import com.minecolonies.core.items.ItemSupplyChestDeployer;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents
{
    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Constants.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemScanAnalyzer.Timestamp>> TIME_COMPONENT =
      savedSynced("timestamp", ItemScanAnalyzer.Timestamp.CODEC, ItemScanAnalyzer.Timestamp.STREAM_CODEC);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemSupplyChestDeployer.SupplyData>> SUPPLY_COMPONENT =
      savedSynced("supplies", ItemSupplyChestDeployer.SupplyData.CODEC, ItemSupplyChestDeployer.SupplyData.STREAM_CODEC);

    static
    {
        ItemScanAnalyzer.Timestamp.TYPE = TIME_COMPONENT;
        ItemSupplyChestDeployer.SupplyData.TYPE = SUPPLY_COMPONENT;
    }

    private static <D> DeferredHolder<DataComponentType<?>, DataComponentType<D>> savedSynced(final String name,
      final Codec<D> codec,
      final StreamCodec<RegistryFriendlyByteBuf, D> streamCodec)
    {
        return REGISTRY.register(name,
          () -> DataComponentType.<D>builder().persistent(codec).networkSynchronized(streamCodec).build());
    }
}
