package com.minecolonies.api.advancements.place_structure;

import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.advancements.PlayerAdvancements;

public class PlaceStructureListeners extends CriterionListeners<PlaceStructureCriterionInstance>
{
    public PlaceStructureListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger(final StructureName structureName)
    {
        trigger(instance -> instance.test(structureName));
    }
}
