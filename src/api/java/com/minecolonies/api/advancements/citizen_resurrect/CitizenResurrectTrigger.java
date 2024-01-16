package com.minecolonies.api.advancements.citizen_resurrect;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.advancements.CriterionListeners;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A Trigger that is triggered when the miner reaches a certain depth
 */
public class CitizenResurrectTrigger extends AbstractCriterionTrigger<CriterionListeners<CitizenResurrectCriterionInstance>, CitizenResurrectCriterionInstance>
{
    private final static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_RESURRECT);

    public CitizenResurrectTrigger()
    {
        super(ID, CriterionListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     */
    public void trigger(final ServerPlayer player)
    {
        final CriterionListeners<CitizenResurrectCriterionInstance> listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger();
        }
    }

    @NotNull
    @Override
    public CitizenResurrectCriterionInstance createInstance(@NotNull final JsonObject object, @NotNull final DeserializationContext conditions)
    {
        return new CitizenResurrectCriterionInstance();
    }
}
