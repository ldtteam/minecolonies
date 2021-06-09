package com.minecolonies.api.advancements.colony_population;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ColonyPopulationTrigger extends AbstractCriterionTrigger<ColonyPopulationListeners, ColonyPopulationCriterionInstance>
{
    public ColonyPopulationTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_COLONY_POPULATION), ColonyPopulationListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     */
    public void trigger(final ServerPlayerEntity player, final int populationCount)
    {
        final ColonyPopulationListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(populationCount);
        }
    }

    @NotNull
    @Override
    public ColonyPopulationCriterionInstance createInstance(@NotNull final JsonObject jsonObject, @NotNull final ConditionArrayParser conditionArrayParser)
    {
        final int populationCount = JSONUtils.getAsInt(jsonObject, "population_count");
        return new ColonyPopulationCriterionInstance(populationCount);
    }
}
