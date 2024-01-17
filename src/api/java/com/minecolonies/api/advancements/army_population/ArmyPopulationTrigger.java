package com.minecolonies.api.advancements.army_population;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Triggered on allocation of new soldiers
 */
public class ArmyPopulationTrigger extends AbstractCriterionTrigger<ArmyPopulationListeners, ArmyPopulationCriterionInstance>
{
    public ArmyPopulationTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_ARMY_POPULATION), ArmyPopulationListeners::new);
    }

    /**
     * Triggers the listener checks if there is any listening in
     * @param player the player the check regards
     * @param armySize the related colony's current army size
     */
    public void trigger(final ServerPlayer player, final int armySize)
    {
        final ArmyPopulationListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(armySize);
        }
    }

    @NotNull
    @Override
    public ArmyPopulationCriterionInstance createInstance(@NotNull final JsonObject object, @NotNull final DeserializationContext context)
    {
        return ArmyPopulationCriterionInstance.deserializeFromJson(object, context);
    }
}
