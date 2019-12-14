package com.minecolonies.api.advancements.complete_build_request;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class CompleteBuildRequestTrigger extends AbstractCriterionTrigger<CompleteBuildRequestListeners, CompleteBuildRequestCriterionInstance>
{
    public CompleteBuildRequestTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_COMPLETE_BUILD_REQUEST), CompleteBuildRequestListeners::new);
    }

    public void trigger(final EntityPlayerMP player, final StructureName structureName, final int level)
    {
        final CompleteBuildRequestListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(structureName, level);
        }
    }

    @NotNull
    @Override
    public CompleteBuildRequestCriterionInstance deserializeInstance(@NotNull final JsonObject jsonObject, @NotNull final JsonDeserializationContext jsonDeserializationContext)
    {
        if (jsonObject.has("hut_name"))
        {
            final String hutName = JsonUtils.getString(jsonObject, "hut_name");
            if (jsonObject.has("level"))
            {
                final int level = JsonUtils.getInt(jsonObject, "level");
                return new CompleteBuildRequestCriterionInstance(hutName, level);
            }
            return new CompleteBuildRequestCriterionInstance(hutName);
        }

        if (jsonObject.has("structure_name"))
        {
            final StructureName structureName = new StructureName(JsonUtils.getString(jsonObject, "structure_name"));
            if (jsonObject.has("structure_name"))
            {
                final int level = JsonUtils.getInt(jsonObject, "level");
                return new CompleteBuildRequestCriterionInstance(structureName, level);
            }
            return new CompleteBuildRequestCriterionInstance(structureName);
        }

        return new CompleteBuildRequestCriterionInstance();
    }
}
