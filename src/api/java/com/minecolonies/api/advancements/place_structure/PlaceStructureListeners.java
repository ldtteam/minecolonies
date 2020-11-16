package com.minecolonies.api.advancements.place_structure;

import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.advancements.PlayerAdvancements;

/**
 * The listener instantiated for every advancement that listens to the associated criterion.
 * A basic class to trigger with the correct arguments
 */
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
