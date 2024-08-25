package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

public record LastPos(BlockPos pos)
{
    public static final LastPos EMPTY = new LastPos(BlockPos.ZERO);

    public static final Codec<LastPos> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(BlockPos.CODEC.fieldOf("pos").forGetter(LastPos::pos))
                   .apply(builder, LastPos::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LastPos> STREAM_CODEC =
      StreamCodec.composite(BlockPos.STREAM_CODEC,
        LastPos::pos,
        LastPos::new);

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.LAST_POS_COMPONENT, this);
    }

    public static LastPos readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.LAST_POS_COMPONENT, LastPos.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<LastPos> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}