package com.minecolonies.api.advancements.complete_build_request;

import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.advancements.PlayerAdvancements;

/**
 * The listener instantiated for every advancement that listens to the associated criterion.
 * A basic class to trigger with the correct arguments
 */
public class CompleteBuildRequestListeners extends CriterionListeners<CompleteBuildRequestCriterionInstance>
{
    public CompleteBuildRequestListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger(final StructureName structureName, final int level)
    {
        trigger(instance -> instance.test(structureName, level));
    }
}
