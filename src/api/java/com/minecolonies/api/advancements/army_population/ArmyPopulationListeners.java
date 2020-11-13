package com.minecolonies.api.advancements.army_population;

import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.advancements.PlayerAdvancements;

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
