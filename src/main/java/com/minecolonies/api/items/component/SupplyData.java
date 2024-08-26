package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

import static com.minecolonies.api.util.constant.Constants.PLACEMENT_NBT;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RANDOM_KEY;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_SAW_STORY;

public record SupplyData(boolean sawStory, boolean instantPlacement, long randomKey)
{
    public static final SupplyData EMPTY = new SupplyData(false, false, -1);

    public static final Codec<SupplyData> CODEC = RecordCodecBuilder.create(
      builder -> builder.group(
          Codec.BOOL.fieldOf(TAG_SAW_STORY).forGetter(SupplyData::sawStory),
          Codec.BOOL.fieldOf(PLACEMENT_NBT).forGetter(SupplyData::instantPlacement),
          Codec.LONG.fieldOf(TAG_RANDOM_KEY).forGetter(SupplyData::randomKey))
                   .apply(builder, SupplyData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SupplyData> STREAM_CODEC =
      StreamCodec.composite(
        ByteBufCodecs.BOOL, SupplyData::sawStory,
        ByteBufCodecs.BOOL, SupplyData::instantPlacement,
        ByteBufCodecs.VAR_LONG, SupplyData::randomKey,
        SupplyData::new);

    public SupplyData withSawStory(final boolean sawStory)
    {
        return new SupplyData(sawStory, instantPlacement, randomKey);
    }

    public SupplyData withRandomKey(final long randomKey)
    {
        return new SupplyData(sawStory, instantPlacement, randomKey);
    }

    public boolean hasRandomKey()
    {
        return randomKey != EMPTY.randomKey;
    }

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.SUPPLY_COMPONENT, this);
    }

    public static SupplyData readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.SUPPLY_COMPONENT, SupplyData.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<SupplyData> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}
