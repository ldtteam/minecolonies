package com.minecolonies.api.advancements.place_supply;

import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.advancements.PlayerAdvancements;

public class PlaceSupplyListeners extends CriterionListeners<PlaceSupplyCriterionInstance>
{
    public PlaceSupplyListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger()
    {
        trigger(instance -> true);
    }
}
