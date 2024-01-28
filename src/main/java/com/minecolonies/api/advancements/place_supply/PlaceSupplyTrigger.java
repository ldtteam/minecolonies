package com.minecolonies.api.advancements.place_supply;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Triggered when a supply camp or supply ship has been placed
 */
public class PlaceSupplyTrigger extends AbstractCriterionTrigger<PlaceSupplyListeners, PlaceSupplyCriterionInstance>
{
    public PlaceSupplyTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_SUPPLY_PLACED), PlaceSupplyListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     */
    public void trigger(final ServerPlayer player)
    {
        final PlaceSupplyListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger();
        }
    }

    @NotNull
    @Override
    public PlaceSupplyCriterionInstance createInstance(@NotNull final JsonObject jsonObject, @NotNull final DeserializationContext conditionArrayParser)
    {
        return new PlaceSupplyCriterionInstance();
    }
}
