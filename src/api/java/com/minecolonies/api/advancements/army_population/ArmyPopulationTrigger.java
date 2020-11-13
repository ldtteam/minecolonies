package com.minecolonies.api.advancements.army_population;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ArmyPopulationTrigger extends AbstractCriterionTrigger<ArmyPopulationListeners, ArmyPopulationCriterionInstance>
{
    public ArmyPopulationTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_ARMY_POPULATION), ArmyPopulationListeners::new);
    }

    public void trigger(final ServerPlayerEntity player, final int populationCount)
    {
        final ArmyPopulationListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(populationCount);
        }
    }

    @NotNull
    @Override
    public ArmyPopulationCriterionInstance deserializeInstance(@NotNull final JsonObject jsonObject, @NotNull final JsonDeserializationContext jsonDeserializationContext)
    {
        final int populationCount = JSONUtils.getInt(jsonObject, "population_count");
        return new ArmyPopulationCriterionInstance(populationCount);
    }
}
