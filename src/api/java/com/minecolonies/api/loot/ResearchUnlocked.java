package com.minecolonies.api.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ResearchUnlocked implements ILootCondition
{
    private final ResourceLocation effectId;

    private ResearchUnlocked(@NotNull final ResourceLocation effectId)
    {
        this.effectId = effectId;
    }

    public static ILootCondition.IBuilder effect(@NotNull final ResourceLocation effectId)
    {
        return () -> new ResearchUnlocked(effectId);
    }

    @NotNull
    @Override
    public LootConditionType getType()
    {
        return ModLootConditions.researchUnlocked;
    }

    @Override
    public boolean test(@NotNull final LootContext lootContext)
    {
        final Entity entity = lootContext.getParamOrNull(LootParameters.KILLER_ENTITY);
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

    public static class Serializer implements ILootSerializer<ResearchUnlocked>
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
            final ResourceLocation researchId = new ResourceLocation(JSONUtils.getAsString(json, "id", ""));
            return new ResearchUnlocked(researchId);
        }
    }
}
