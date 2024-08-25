package com.minecolonies.api.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public record AdventureData(EntityType<?> entityType, float damage, int xp)
{
    public static final Codec<AdventureData> CODEC = RecordCodecBuilder.create(
      builder -> builder
                   .group(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(AdventureData::entityType),
                     Codec.FLOAT.fieldOf("damage").forGetter(AdventureData::damage),
                     Codec.INT.fieldOf("xp").forGetter(AdventureData::xp))
                   .apply(builder, AdventureData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AdventureData> STREAM_CODEC =
      StreamCodec.composite(
        ByteBufCodecs.registry(Registries.ENTITY_TYPE), AdventureData::entityType,
        ByteBufCodecs.FLOAT, AdventureData::damage,
        ByteBufCodecs.VAR_INT, AdventureData::xp,
        AdventureData::new);

    public void writeToItemStack(final ItemStack itemStack)
    {
        itemStack.set(ModDataComponents.ADVENTURE_COMPONENT, this);
    }

    @Nullable
    public static AdventureData readFromItemStack(final ItemStack itemStack)
    {
        return itemStack.get(ModDataComponents.ADVENTURE_COMPONENT);
    }

    public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<AdventureData> updater)
    {
        updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
    }
}