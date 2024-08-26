package com.minecolonies.api.items.component;

import com.minecolonies.api.util.BlockPosUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.function.UnaryOperator;

public record PatrolTarget(BlockPos pos)
{
    public static final PatrolTarget EMPTY = new PatrolTarget(BlockPosUtil.SAFE_ZERO);

    public static final Codec<PatrolTarget> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(BlockPos.CODEC.fieldOf("pos").forGetter(PatrolTarget::pos))
                   .apply(builder, PatrolTarget::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PatrolTarget> STREAM_CODEC =
      StreamCodec.composite(BlockPos.STREAM_CODEC,
        PatrolTarget::pos,
        PatrolTarget::new);

    public boolean hasPos()
    {
        return !pos.equals(EMPTY.pos);
    }

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.PATROL_TARGET, this);
    }

    public static PatrolTarget readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.PATROL_TARGET, PatrolTarget.EMPTY);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<PatrolTarget> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}
