package com.minecolonies.api.advancements.colony_population;

import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.advancements.PlayerAdvancements;

public class ColonyPopulationListeners extends CriterionListeners<ColonyPopulationCriterionInstance>
{
    public ColonyPopulationListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger(final int populationCount)
    {
        trigger(instance -> instance.test(populationCount));
    }
}
