package com.minecolonies.api.advancements.army_population;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The test instance to check the "population_count" for the "army_population" trigger
 */
public class ArmyPopulationCriterionInstance extends CriterionInstance
{
    private final int populationCount;

    /**
     * Registers a deserialized advancement trigger with this criterion condition
     * @param populationCount the current army size
     */
    public ArmyPopulationCriterionInstance(final int populationCount)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_ARMY_POPULATION), EntityPredicate.AndPredicate.ANY);

        this.populationCount = populationCount;
    }

    /**
     * Perform the check comparing the current population to the advancement condition
     * @param populationCount the current population
     * @return whether the check passed
     */
    public boolean test(final int populationCount)
    {
        // Less than sign used just in case a previous population increase was missed.
        return this.populationCount <= populationCount;
    }

    @NotNull
    public static ArmyPopulationCriterionInstance deserializeFromJson(@NotNull final JsonObject object,
                                                                      @NotNull final ConditionArrayParser conditions)
    {
        final int populationCount = JSONUtils.getAsInt(object, "population_count");
        return new ArmyPopulationCriterionInstance(populationCount);
    }

    @NotNull
    @Override
    public JsonObject serializeToJson(@NotNull final ConditionArraySerializer serializer)
    {
        final JsonObject json = super.serializeToJson(serializer);
        json.addProperty("population_count", this.populationCount);
        return json;
    }
}
