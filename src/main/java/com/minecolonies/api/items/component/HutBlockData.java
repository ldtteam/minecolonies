package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public record HutBlockData(int level, boolean pastable)
{
    public static final Codec<HutBlockData> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(Codec.INT.fieldOf("level").forGetter(HutBlockData::level),
                     Codec.BOOL.fieldOf("pastable").forGetter(HutBlockData::pastable))
                   .apply(builder, HutBlockData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HutBlockData> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.VAR_INT,
        HutBlockData::level, ByteBufCodecs.BOOL, HutBlockData::pastable,
        HutBlockData::new);

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.HUT_COMPONENT, this);
    }

    @Nullable
    public static HutBlockData readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.get(ModDataComponents.HUT_COMPONENT);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<HutBlockData> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}