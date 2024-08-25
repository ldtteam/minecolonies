package com.minecolonies.api.items.component;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public record ColonyId(int id, ResourceKey<Level> dimension)
{
    public static final ColonyId EMPTY = new ColonyId(-1, Level.OVERWORLD);

    public static final Codec<ColonyId> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(Codec.INT.fieldOf("colony_id").forGetter(ColonyId::id),
                     Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(ColonyId::dimension))
                   .apply(builder, ColonyId::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ColonyId> STREAM_CODEC =
      StreamCodec.composite(ByteBufCodecs.VAR_INT,
        ColonyId::id, ResourceKey.streamCodec(Registries.DIMENSION), ColonyId::dimension,
        ColonyId::new);

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.COLONY_ID_COMPONENT, this);
    }

    public static ColonyId readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.getOrDefault(ModDataComponents.COLONY_ID_COMPONENT, ColonyId.EMPTY);
    }

    @Nullable
    public static IColony readColonyFromItemStack(final ItemStack itemStack)
    {
        final ColonyId colonyId = readFromItemStack(itemStack);
        return colonyId == EMPTY ? null : IColonyManager.getInstance().getColonyByDimension(colonyId.id(), colonyId.dimension());
    }

    @Nullable
    public static IColonyView readColonyViewFromItemStack(final ItemStack itemStack)
    {
        final ColonyId colonyId = readFromItemStack(itemStack);
        return colonyId == EMPTY ? null : IColonyManager.getInstance().getColonyView(colonyId.id(), colonyId.dimension());
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<ColonyId> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}