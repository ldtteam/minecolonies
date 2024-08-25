package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

public record Bool(boolean does)
{
    public static final Bool EMPTY = new Bool(false);

    public static final Codec<Bool> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(Codec.BOOL.fieldOf("bool").forGetter(Bool::does))
                   .apply(builder, Bool::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Bool> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.BOOL,
        Bool::does,
        Bool::new);

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.BOOL_COMPONENT, this);
    }

    public static Bool readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.BOOL_COMPONENT, Bool.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<Bool> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}