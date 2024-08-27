package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

/**
 * Custom usually tooltip component, like ItemLore, but simplier
 */
public record Desc(MutableComponent desc)
{
    public static final Desc EMPTY = new Desc(Component.empty());
    public static final Codec<Desc> CODEC = ComponentSerialization.FLAT_CODEC.xmap(Desc::new, Desc::desc);
    public static final StreamCodec<RegistryFriendlyByteBuf, Desc> STREAM_CODEC = ComponentSerialization.STREAM_CODEC.map(Desc::new, Desc::desc);

    public Desc(Component comp)
    {
        this((MutableComponent) comp);
    }

    public boolean isEmpty()
    {
        return desc.equals(EMPTY.desc);
    }

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
