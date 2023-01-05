package com.minecolonies.api.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

public class ResearchUnlocked implements LootItemCondition
{
    private final ResourceLocation effectId;

    private ResearchUnlocked(@NotNull final ResourceLocation effectId)
    {
        this.effectId = effectId;
    }

    public static LootItemCondition.Builder effect(@NotNull final ResourceLocation effectId)
    {
        return () -> new ResearchUnlocked(effectId);
    }

    @NotNull
    @Override
    public LootItemConditionType getType()
    {
        return ModLootConditions.researchUnlocked.get();
    }

    @Override
    public boolean test(@NotNull final LootContext lootContext)
    {
        final Entity entity = lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
        if (entity instanceof AbstractEntityCitizen)
        {
            final AbstractEntityCitizen citizen = (AbstractEntityCitizen) entity;
            final IColony colony = citizen.getCitizenColonyHandler().getColony();
            if (colony != null)
            {
                return colony.getResearchManager().getResearchEffects().getEffectStrength(this.effectId) > 0;
            }
        }

        return false;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ResearchUnlocked>
    {
        @Override
        public void serialize(@NotNull final JsonObject json,
                              @NotNull final ResearchUnlocked condition,
                              @NotNull final JsonSerializationContext context)
        {
            json.addProperty("id", condition.effectId.toString());
        }

        @NotNull
        @Override
        public ResearchUnlocked deserialize(@NotNull final JsonObject json,
                                                 @NotNull final JsonDeserializationContext context)
        {
            final ResourceLocation researchId = new ResourceLocation(GsonHelper.getAsString(json, "id", ""));
            return new ResearchUnlocked(researchId);
        }
    }
}
