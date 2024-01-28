package com.minecolonies.api.advancements.colony_population;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

/**
 * The test instance to check the "population_count" for the "colony_population" trigger
 */
public class ColonyPopulationCriterionInstance extends AbstractCriterionTriggerInstance
{
    private int populationCount;

    /**
     * Constructs a check with a single condition
     * @param populationCount the population that has to be reached to succeed
     */
    public ColonyPopulationCriterionInstance(final int populationCount)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_COLONY_POPULATION), ContextAwarePredicate.ANY);

        this.populationCount = populationCount;
    }

    /**
     * Performs the check for the conditions
     * @param populationCount the current population
     * @return whether the check succeeded
     */
    public boolean test(final int populationCount)
    {
        //Less than sign used just in case a previous population increase was missed.
        return this.populationCount <= populationCount;
    }

    @NotNull
    public static ColonyPopulationCriterionInstance deserializeFromJson(@NotNull final JsonObject jsonObject,
                                                                        @NotNull final DeserializationContext context)
    {
        final int populationCount = GsonHelper.getAsInt(jsonObject, "population_count");
        return new ColonyPopulationCriterionInstance(populationCount);
    }

    @NotNull
    @Override
    public JsonObject serializeToJson(@NotNull final SerializationContext context)
    {
        final JsonObject json = super.serializeToJson(context);
        json.addProperty("population_count", this.populationCount);
        return json;
    }
}
