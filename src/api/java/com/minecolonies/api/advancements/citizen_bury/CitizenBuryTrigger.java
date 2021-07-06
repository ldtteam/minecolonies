package com.minecolonies.api.advancements.citizen_bury;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.advancements.CriterionListeners;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * A Trigger that is triggered when a citizen is buried in a graveyard
 */
public class CitizenBuryTrigger extends AbstractCriterionTrigger<CriterionListeners<CitizenBuryCriterionInstance>, CitizenBuryCriterionInstance>
{
    private final static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CITIZEN_BURY);

    public CitizenBuryTrigger()
    {
        super(ID, CriterionListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     */
    public void trigger(final ServerPlayerEntity player)
    {
        final CriterionListeners<CitizenBuryCriterionInstance> listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger();
        }
    }

    @NotNull
    @Override
    public CitizenBuryCriterionInstance createInstance(@NotNull final JsonObject object, @NotNull final ConditionArrayParser conditions)
    {
        return new CitizenBuryCriterionInstance();
    }
}
