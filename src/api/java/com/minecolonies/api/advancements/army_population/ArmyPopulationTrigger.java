package com.minecolonies.api.advancements.army_population;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
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
    public void trigger(final ServerPlayerEntity player, final int armySize)
    {
        final ArmyPopulationListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(armySize);
        }
    }

    @NotNull
    @Override
    public ArmyPopulationCriterionInstance createInstance(@NotNull final JsonObject object, @NotNull final ConditionArrayParser conditions)
    {
        return ArmyPopulationCriterionInstance.deserializeFromJson(object, conditions);
    }
}
