package com.minecolonies.api.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A loot condition that checks if the entity producing loot is in a biome with a particular tag. */
public class EntityInBiomeTag implements LootItemCondition
{
    public static final EntityInBiomeTag           ANY   = new EntityInBiomeTag(null);
    public static final MapCodec<EntityInBiomeTag> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
        .group(TagKey.hashedCodec(Registries.BIOME).optionalFieldOf("tag", null).forGetter(t -> t.tag))
        .apply(builder, EntityInBiomeTag::ofNullable));

    @Nullable
    private final TagKey<Biome> tag;

    private EntityInBiomeTag(@Nullable final TagKey<Biome> tag)
    {
        this.tag = tag;
    }

    public static LootItemCondition.Builder any()
    {
        return () -> ANY;
    }

    public static LootItemCondition.Builder of(@NotNull final TagKey<Biome> category)
    {
        return () -> new EntityInBiomeTag(category);
    }

    private static EntityInBiomeTag ofNullable(final TagKey<Biome> tag)
    {
        return tag == null ? ANY : new EntityInBiomeTag(tag);
    }

    @NotNull
    @Override
    public LootItemConditionType getType()
    {
        return ModLootConditions.entityInBiomeTag.get();
    }

    @Override
    public boolean test(@NotNull final LootContext lootContext)
    {
        if (tag == null)
        {
            return true;
        }

        final Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity != null && entity.level() != null)
        {
            return entity.level().getBiome(entity.blockPosition()).is(tag);
        }

        return false;
    }
}
