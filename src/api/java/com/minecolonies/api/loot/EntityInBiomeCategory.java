package com.minecolonies.api.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.entity.Entity;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A loot condition that checks if the entity producing loot is in a biome with a particular category. */
public class EntityInBiomeCategory implements ILootCondition
{
    @Nullable
    private final Biome.Category category;

    private EntityInBiomeCategory(@Nullable final Biome.Category category)
    {
        this.category = category;
    }

    public static ILootCondition.IBuilder any()
    {
        return () -> new EntityInBiomeCategory(null);
    }

    public static ILootCondition.IBuilder of(@NotNull final Biome.Category category)
    {
        return () -> new EntityInBiomeCategory(category);
    }

    @NotNull
    @Override
    public LootConditionType getType()
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

        final Entity entity = lootContext.getParamOrNull(LootParameters.THIS_ENTITY);
        if (entity != null && entity.level != null)
        {
            final Biome biome = entity.level.getBiome(entity.getEntity().blockPosition());
            return biome.getBiomeCategory().equals(category);
        }

        return false;
    }

    public static class Serializer implements ILootSerializer<EntityInBiomeCategory>
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
            final String categoryId = JSONUtils.getAsString(json, "category", "");
            final Biome.Category category = Biome.Category.byName(categoryId);
            return new EntityInBiomeCategory(category);
        }
    }
}
