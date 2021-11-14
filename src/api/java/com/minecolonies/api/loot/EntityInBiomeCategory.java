package com.minecolonies.api.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A loot condition that checks if the entity producing loot is in a biome with a particular category. */
public class EntityInBiomeCategory implements LootItemCondition
{
    @Nullable
    private final Biome.BiomeCategory category;

    private EntityInBiomeCategory(@Nullable final Biome.BiomeCategory category)
    {
        this.category = category;
    }

    public static LootItemCondition.Builder any()
    {
        return () -> new EntityInBiomeCategory(null);
    }

    public static LootItemCondition.Builder of(@NotNull final Biome.BiomeCategory category)
    {
        return () -> new EntityInBiomeCategory(category);
    }

    @NotNull
    @Override
    public LootItemConditionType getType()
    {
        return ModLootConditions.entityInBiomeCategory;
    }

    @Override
    public boolean test(@NotNull final LootContext lootContext)
    {
        if (category == null)
        {
            return true;
        }

        final Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity != null && entity.level != null)
        {
            final Biome biome = entity.level.getBiome(entity.blockPosition());
            return biome.getBiomeCategory().equals(category);
        }

        return false;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityInBiomeCategory>
    {
        @Override
        public void serialize(@NotNull final JsonObject json,
                              @NotNull final EntityInBiomeCategory condition,
                              @NotNull final JsonSerializationContext context)
        {
            if (condition.category != null)
            {
                json.addProperty("category", condition.category.getSerializedName());
            }
        }

        @NotNull
        @Override
        public EntityInBiomeCategory deserialize(@NotNull final JsonObject json,
                                                 @NotNull final JsonDeserializationContext context)
        {
            final String categoryId = GsonHelper.getAsString(json, "category", "");
            final Biome.BiomeCategory category = Biome.BiomeCategory.byName(categoryId);
            return new EntityInBiomeCategory(category);
        }
    }
}
