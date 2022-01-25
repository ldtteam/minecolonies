package com.minecolonies.api.advancements.create_build_request;

import com.google.gson.JsonObject;
import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

/**
 * A Trigger for any building request that gets made
 */
public class CreateBuildRequestTrigger extends AbstractCriterionTrigger<CreateBuildRequestListeners, CreateBuildRequestCriterionInstance>
{
    public CreateBuildRequestTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CREATE_BUILD_REQUEST), CreateBuildRequestListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     * @param structureName the structure that is to be created
     * @param level the level that the request will complete
     */
    public void trigger(final ServerPlayerEntity player, final StructureName structureName, final int level)
    {
        if (player != null)
        {
            final CreateBuildRequestListeners listeners = this.getListeners(player.getAdvancements());
            if (listeners != null)
            {
                listeners.trigger(structureName, level);
            }
        }
    }

    @Override
    public CreateBuildRequestCriterionInstance createInstance(final JsonObject jsonObject, final ConditionArrayParser conditionArrayParser)
    {
        return CreateBuildRequestCriterionInstance.deserializeFromJson(jsonObject, conditionArrayParser);
    }
}
