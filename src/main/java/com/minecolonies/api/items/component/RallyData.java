package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public record RallyData(List<BlockPos> towers, boolean active)
{
    public static final RallyData EMPTY = new RallyData(List.of(), false);

    public static final Codec<RallyData> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(Codec.list(BlockPos.CODEC).fieldOf("pos").forGetter(RallyData::towers),
                     Codec.BOOL.fieldOf("active").forGetter(RallyData::active))
                   .apply(builder, RallyData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RallyData> STREAM_CODEC =
      StreamCodec.composite(BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
        RallyData::towers, ByteBufCodecs.BOOL, RallyData::active,
        RallyData::new);

    public RallyData withActive(final boolean active)
    {
        return new RallyData(towers, active);
    }

    public RallyData withPosRemoval(final BlockPos posToRemove)
    {
        final ArrayList<BlockPos> copy = new ArrayList<>(towers);
        return copy.remove(posToRemove) ? new RallyData(Collections.unmodifiableList(copy), active) : this;
    }

    public RallyData withPosAddition(final BlockPos posToAdd)
    {
        final ArrayList<BlockPos> copy = new ArrayList<>(towers);
        copy.add(posToAdd);
        return new RallyData(Collections.unmodifiableList(copy), active);
    }

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.RALLY_COMPONENT, this);
    }

    public static RallyData readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.RALLY_COMPONENT, RallyData.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<RallyData> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}
