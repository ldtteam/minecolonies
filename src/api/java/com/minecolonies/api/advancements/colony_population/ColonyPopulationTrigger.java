package com.minecolonies.api.advancements.colony_population;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
    public void trigger(final ServerPlayer player, final int populationCount)
    {
        final ColonyPopulationListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(populationCount);
        }
    }

    @NotNull
    @Override
    public ColonyPopulationCriterionInstance createInstance(@NotNull final JsonObject jsonObject, @NotNull final DeserializationContext conditionArrayParser)
    {
        return ColonyPopulationCriterionInstance.deserializeFromJson(jsonObject, conditionArrayParser);
    }
}
