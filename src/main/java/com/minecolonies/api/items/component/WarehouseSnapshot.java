package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Container class for warehouse snapshot data.
 *
 * @param snapshot the snapshot data.
 * @param hash     the work order hash for comparison between work orders.
 */
public record WarehouseSnapshot(Map<String, Integer> snapshot, String hash)
{
    public static final WarehouseSnapshot EMPTY = new WarehouseSnapshot(Map.of(), "");

    public static final Codec<WarehouseSnapshot> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("snapshot").forGetter(WarehouseSnapshot::snapshot),
                     Codec.STRING.fieldOf("hash").forGetter(WarehouseSnapshot::hash))
                   .apply(builder, WarehouseSnapshot::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WarehouseSnapshot> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
        WarehouseSnapshot::snapshot, ByteBufCodecs.STRING_UTF8, WarehouseSnapshot::hash,
        WarehouseSnapshot::new);

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.WAREHOUSE_SNAPSHOT_COMPONENT, this);
    }

    public static WarehouseSnapshot readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.WAREHOUSE_SNAPSHOT_COMPONENT, WarehouseSnapshot.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<WarehouseSnapshot> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}
