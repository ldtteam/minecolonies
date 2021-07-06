package com.minecolonies.api.advancements.max_fields;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.advancements.CriterionListeners;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Is triggered when the maximum number of fields has been allocated to a single farmer
 */
public class MaxFieldsTrigger extends AbstractCriterionTrigger<CriterionListeners<MaxFieldsCriterionInstance>, MaxFieldsCriterionInstance>
{
    private final static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_MAX_FIELDS);

    public MaxFieldsTrigger()
    {
        super(ID, CriterionListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     */
    public void trigger(final ServerPlayerEntity player)
    {
        final CriterionListeners<MaxFieldsCriterionInstance> listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger();
        }
    }

    @NotNull
    @Override
    public MaxFieldsCriterionInstance createInstance(@NotNull final JsonObject object, @NotNull final ConditionArrayParser conditions)
    {
        return new MaxFieldsCriterionInstance();
    }
}
