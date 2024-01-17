package com.minecolonies.api.advancements.all_towers;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.advancements.CriterionListeners;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Triggered when all barracks towers have been fully upgraded on any one barracks
 */
public class AllTowersTrigger extends AbstractCriterionTrigger<CriterionListeners<AllTowersCriterionInstance>, AllTowersCriterionInstance>
{
    /** How this trigger is referenced in JSON and identified in the registry */
    private final static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_ALL_TOWERS);

    /** Construct the trigger instance with the right ID */
    public AllTowersTrigger()
    {
        super(ID, CriterionListeners::new);
    }

    /**
     * Triggers the listener checks if there is any listening in
     * @param player the player the check regards
     */
    public void trigger(final ServerPlayer player)
    {
        final CriterionListeners<AllTowersCriterionInstance> listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger();
        }
    }

    @NotNull
    @Override
    public AllTowersCriterionInstance createInstance(@NotNull final JsonObject object, @NotNull final DeserializationContext conditions)
    {
        return new AllTowersCriterionInstance();
    }
}
