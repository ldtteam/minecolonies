package com.minecolonies.api.advancements.place_supply;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class PlaceSupplyTrigger extends AbstractCriterionTrigger<PlaceSupplyListeners, PlaceSupplyCriterionInstance>
{
    public PlaceSupplyTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_SUPPLY_PLACED), PlaceSupplyListeners::new);
    }

    public void trigger(final EntityPlayerMP player)
    {
        final PlaceSupplyListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger();
        }
    }

    @NotNull
    @Override
    public PlaceSupplyCriterionInstance deserializeInstance(@NotNull final JsonObject jsonObject, @NotNull final JsonDeserializationContext jsonDeserializationContext)
    {
        return new PlaceSupplyCriterionInstance();
    }
}
