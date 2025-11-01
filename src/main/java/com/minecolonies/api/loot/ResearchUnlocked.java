package com.minecolonies.api.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Loot condition that checks whether the local colony has unlocked the specified research
 */
public class ResearchUnlocked implements LootItemCondition
{
    private final ResourceLocation effectId;
    private final double minStrength;
    private final double maxStrength;

    private ResearchUnlocked(@NotNull final ResourceLocation effectId, final double minStrength, final double maxStrength)
    {
        this.effectId = effectId;
        this.minStrength = minStrength;
        this.maxStrength = maxStrength;
    }

    /**
     * Creates a loot condition that is true when the given research effect is unlocked.
     * @param effectId the research effect id
     * @return the condition
     */
    public static LootItemCondition.Builder effect(@NotNull final ResourceLocation effectId)
    {
        return effect(effectId, Double.MIN_VALUE);
    }

    /**
     * Creates a loot condition that is true when the given research effect is unlocked at the given strength or higher.
     * @param effectId the research effect id
     * @param strength the minimum required strength
     * @return the condition
     */
    public static LootItemCondition.Builder effect(@NotNull final ResourceLocation effectId, final double strength)
    {
        return effect(effectId, strength, Double.MAX_VALUE);
    }

    /**
     * Creates a loot condition that is true when the given research effect is unlocked and within the given strength range.
     * @param effectId the research effect id
     * @param minStrength the minimum (inclusive) required strength
     * @param maxStrength the maximum (exclusive) required strength
     * @return the condition
     */
    public static LootItemCondition.Builder effect(@NotNull final ResourceLocation effectId, final double minStrength, final double maxStrength)
    {
        return () -> new ResearchUnlocked(effectId, minStrength, maxStrength);
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
        return test(lootContext, lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY))
                .or(() -> test(lootContext, lootContext.getParamOrNull(LootContextParams.THIS_ENTITY)))
                .or(() -> test(lootContext, lootContext.getParamOrNull(LootContextParams.ORIGIN)))
                .orElse(false);
    }

    private Optional<Boolean> test(@NotNull final LootContext lootContext, @Nullable Entity entity)
    {
        return Optional.ofNullable(entity)
                .map(e -> e instanceof AbstractEntityCitizen citizen ? citizen : null)
                .flatMap(c -> test(lootContext, c.getCitizenColonyHandler().getColonyOrRegister()));
    }

    private Optional<Boolean> test(@NotNull final LootContext lootContext, @Nullable Vec3 origin)
    {
        return Optional.ofNullable(origin)
                .map(BlockPos::containing)
                .flatMap(pos -> test(lootContext, IColonyManager.getInstance().getIColony(lootContext.getLevel(), pos)));
    }

    private Optional<Boolean> test(@NotNull final LootContext lootContext, @Nullable IColony colony)
    {
        return Optional.ofNullable(colony)
                .map(c -> c.getResearchManager().getResearchEffects().getEffectStrength(this.effectId))
                .map(s -> s >= this.minStrength && s < this.maxStrength);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ResearchUnlocked>
    {
        @Override
        public void serialize(@NotNull final JsonObject json,
                              @NotNull final ResearchUnlocked condition,
                              @NotNull final JsonSerializationContext context)
        {
            json.addProperty("id", condition.effectId.toString());

            if (condition.minStrength != Double.MIN_VALUE)
            {
                json.addProperty("minStrength", condition.minStrength);
            }

            if (condition.maxStrength != Double.MAX_VALUE)
            {
                json.addProperty("maxStrength", condition.maxStrength);
            }
        }

        @NotNull
        @Override
        public ResearchUnlocked deserialize(@NotNull final JsonObject json,
                                                 @NotNull final JsonDeserializationContext context)
        {
            final ResourceLocation researchId = new ResourceLocation(GsonHelper.getAsString(json, "id", ""));
            final double minStrength = GsonHelper.getAsDouble(json, "minStrength", Double.MIN_VALUE);
            final double maxStrength = GsonHelper.getAsDouble(json, "maxStrength", Double.MAX_VALUE);
            return new ResearchUnlocked(researchId, minStrength, maxStrength);
        }
    }
}
