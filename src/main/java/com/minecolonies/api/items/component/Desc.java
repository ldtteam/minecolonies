package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

public record Desc(String desc)
{
    public static final Desc EMPTY = new Desc("");

    public static final Codec<Desc> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(Codec.STRING.fieldOf("desc").forGetter(Desc::desc))
                   .apply(builder, Desc::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Desc> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.STRING_UTF8,
        Desc::desc,
        Desc::new);

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.DESC_COMPONENT, this);
    }

    public static Desc readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.DESC_COMPONENT, Desc.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<Desc> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}