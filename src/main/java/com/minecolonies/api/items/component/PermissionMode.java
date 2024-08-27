package com.minecolonies.api.items.component;

import com.ldtteam.common.codec.Codecs;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

public enum PermissionMode
{
    LOCATION, BLOCK;

    public static final PermissionMode EMPTY = LOCATION;
    public static final Codec<PermissionMode> CODEC = Codecs.forEnum(PermissionMode.class);
    public static final StreamCodec<ByteBuf, PermissionMode> STREAM_CODEC = ByteBufCodecs.idMapper(ord -> PermissionMode.values()[ord], PermissionMode::ordinal);

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.PERMISSION_MODE, this);
    }

    public static PermissionMode readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.PERMISSION_MODE, PermissionMode.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<PermissionMode> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}
