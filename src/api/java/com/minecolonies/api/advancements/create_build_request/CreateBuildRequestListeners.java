package com.minecolonies.api.advancements.create_build_request;

import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.advancements.PlayerAdvancements;

public class CreateBuildRequestListeners extends CriterionListeners<CreateBuildRequestCriterionInstance>
{
    public CreateBuildRequestListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger(final StructureName structureName, final int level)
    {
        trigger(instance -> instance.test(structureName, level));
    }
}
