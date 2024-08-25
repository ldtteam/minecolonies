package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

public record Pos(BlockPos pos)
{
    public static final Pos EMPTY = new Pos(BlockPos.ZERO);

    public static final Codec<Pos> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(BlockPos.CODEC.fieldOf("pos").forGetter(Pos::pos))
                   .apply(builder, Pos::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Pos> STREAM_CODEC =
      StreamCodec.composite(BlockPos.STREAM_CODEC,
        Pos::pos,
        Pos::new);

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.POS_COMPONENT, this);
    }

    public static Pos readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.POS_COMPONENT, Pos.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<Pos> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}