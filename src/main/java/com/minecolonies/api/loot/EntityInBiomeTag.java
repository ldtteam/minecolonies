package com.minecolonies.api.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A loot condition that checks if the entity producing loot is in a biome with a particular tag. */
public class EntityInBiomeTag implements LootItemCondition
{
    @Nullable
    private final TagKey<Biome> tag;

    private EntityInBiomeTag(@Nullable final TagKey<Biome> tag)
    {
        this.tag = tag;
    }

    public static LootItemCondition.Builder any()
    {
        return () -> new EntityInBiomeTag(null);
    }

    public static LootItemCondition.Builder of(@NotNull final TagKey<Biome> category)
    {
        return () -> new EntityInBiomeTag(category);
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

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityInBiomeTag>
    {
        @Override
        public void serialize(@NotNull final JsonObject json,
                              @NotNull final EntityInBiomeTag condition,
                              @NotNull final JsonSerializationContext context)
        {
            if (condition.tag != null)
            {
                json.addProperty("tag", condition.tag.location().toString());
            }
        }

        @NotNull
        @Override
        public EntityInBiomeTag deserialize(@NotNull final JsonObject json,
                                            @NotNull final JsonDeserializationContext context)
        {
            final String tagId = GsonHelper.getAsString(json, "tag", "");
            final TagKey<Biome> tag = tagId.isEmpty() ? null : ForgeRegistries.BIOMES.tags().createTagKey(new ResourceLocation(tagId));
            return new EntityInBiomeTag(tag);
        }
    }
}
