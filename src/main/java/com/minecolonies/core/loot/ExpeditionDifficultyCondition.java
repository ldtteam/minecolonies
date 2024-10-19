package com.minecolonies.core.loot;

import com.google.gson.*;
import com.minecolonies.api.loot.ModLootConditions;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.loot.ModLootConditions.EXPEDITION_DIFFICULTY_PARAM;

/**
 * A loot condition that depends on the expedition difficulty.
 */
public class ExpeditionDifficultyCondition implements LootItemCondition
{
    /**
     * The required difficulty.
     */
    @NotNull
    private final List<ColonyExpeditionTypeDifficulty> difficulties;

    /**
     * Internal constructor.
     *
     * @param difficulties the required difficulties.
     */
    private ExpeditionDifficultyCondition(@NotNull final List<ColonyExpeditionTypeDifficulty> difficulties)
    {
        this.difficulties = difficulties;
    }

    /**
     * Generate a condition for a given expedition difficulty.
     *
     * @param difficulties the required difficulties.
     * @return the loot condition builder.
     */
    public static Builder forDifficulty(@NotNull final ColonyExpeditionTypeDifficulty... difficulties)
    {
        return () -> new ExpeditionDifficultyCondition(List.of(difficulties));
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
        if (difficulties.isEmpty())
        {
            return true;
        }

        final ColonyExpeditionTypeDifficulty actualDifficulty = lootContext.getParamOrNull(EXPEDITION_DIFFICULTY_PARAM);
        if (actualDifficulty != null)
        {
            return difficulties.contains(actualDifficulty);
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
            if (!condition.difficulties.isEmpty())
            {
                final JsonArray array = new JsonArray();
                for (final ColonyExpeditionTypeDifficulty difficulty : condition.difficulties)
                {
                    array.add(difficulty.getKey());
                }
                json.add("difficulties", array);
            }
        }

        @NotNull
        @Override
        public ExpeditionDifficultyCondition deserialize(@NotNull final JsonObject json, @NotNull final JsonDeserializationContext context)
        {
            if (json.has("difficulties"))
            {
                final List<ColonyExpeditionTypeDifficulty> difficulties = new ArrayList<>();
                final JsonArray array = GsonHelper.getAsJsonArray(json, "difficulties");
                for (final JsonElement element : array)
                {
                    if (GsonHelper.isStringValue(element))
                    {
                        final ColonyExpeditionTypeDifficulty difficulty = ColonyExpeditionTypeDifficulty.fromKey(element.getAsString());
                        if (difficulty != null)
                        {
                            difficulties.add(difficulty);
                        }
                    }
                }
                return new ExpeditionDifficultyCondition(difficulties);
            }
            return new ExpeditionDifficultyCondition(List.of());
        }
    }
}
