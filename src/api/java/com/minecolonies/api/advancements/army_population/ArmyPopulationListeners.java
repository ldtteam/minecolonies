package com.minecolonies.api.advancements.army_population;

import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.server.PlayerAdvancements;

/**
 * The listener instantiated for every advancement that listens to the associated criterion.
 * A basic class to trigger with the correct arguments
 */
public class ArmyPopulationListeners extends CriterionListeners<ArmyPopulationCriterionInstance>
{
    public ArmyPopulationListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger(final int populationCount)
    {
        trigger(instance -> instance.test(populationCount));
    }
}
