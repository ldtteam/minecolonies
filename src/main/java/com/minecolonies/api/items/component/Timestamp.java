package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

public record Timestamp(long time)
{
    public static final Timestamp EMPTY = new Timestamp(0);

    public static final Codec<Timestamp> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(Codec.LONG.fieldOf("timestamp").forGetter(Timestamp::time))
                   .apply(builder, Timestamp::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Timestamp> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.VAR_LONG,
        Timestamp::time,
        Timestamp::new);

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.TIME_COMPONENT, this);
    }

    public static Timestamp readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.TIME_COMPONENT, Timestamp.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<Timestamp> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}