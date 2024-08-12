package com.minecolonies.core.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A loot condition that depends on the expedition difficulty.
 */
public class ExpeditionDifficultyCondition implements LootItemCondition
{
    /**
     * The parameter for this condition.
     */
    public static final ResourceLocation                                 PARAM_EXPEDITION_DIFFICULTY_ID = new ResourceLocation(Constants.MOD_ID, "expedition_difficulty");
    public static final LootContextParam<ColonyExpeditionTypeDifficulty> PARAM_EXPEDITION_DIFFICULTY    = new LootContextParam<>(PARAM_EXPEDITION_DIFFICULTY_ID);

    /**
     * The required difficulty.
     */
    @Nullable
    private final ColonyExpeditionTypeDifficulty difficulty;

    /**
     * Internal constructor.
     *
     * @param difficulty the required difficulty.
     */
    private ExpeditionDifficultyCondition(@Nullable final ColonyExpeditionTypeDifficulty difficulty)
    {
        this.difficulty = difficulty;
    }

    /**
     * Generate a condition for a given expedition difficulty.
     *
     * @param difficulty the required difficulty.
     * @return the loot condition builder.
     */
    public static Builder forDifficulty(@NotNull final ColonyExpeditionTypeDifficulty difficulty)
    {
        return () -> new ExpeditionDifficultyCondition(difficulty);
    }

    @NotNull
    @Override
    public LootItemConditionType getType()
    {
        return ModLootConditions.expeditionDifficulty.get();
    }

    @Override
    public boolean test(@NotNull final LootContext lootContext)
    {
        if (difficulty == null)
        {
            return true;
        }

        final ColonyExpeditionTypeDifficulty actualDifficulty = lootContext.getParamOrNull(PARAM_EXPEDITION_DIFFICULTY);
        if (actualDifficulty != null)
        {
            return actualDifficulty.getKey().equals(difficulty.getKey());
        }

        return false;
    }

    /**
     * Serializer for {@link ExpeditionDifficultyCondition}.
     */
    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ExpeditionDifficultyCondition>
    {
        @Override
        public void serialize(@NotNull final JsonObject json, @NotNull final ExpeditionDifficultyCondition condition, @NotNull final JsonSerializationContext context)
        {
            if (condition.difficulty != null)
            {
                json.addProperty("difficulty", condition.difficulty.getKey());
            }
        }

        @NotNull
        @Override
        public ExpeditionDifficultyCondition deserialize(@NotNull final JsonObject json, @NotNull final JsonDeserializationContext context)
        {
            final String difficultyKey = GsonHelper.getAsString(json, "difficulty", "");
            final ColonyExpeditionTypeDifficulty difficulty = difficultyKey.isEmpty() ? null : ColonyExpeditionTypeDifficulty.fromKey(difficultyKey);
            return new ExpeditionDifficultyCondition(difficulty);
        }
    }
}
