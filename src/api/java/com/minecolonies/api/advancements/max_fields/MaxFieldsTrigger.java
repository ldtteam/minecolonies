package com.minecolonies.api.advancements.max_fields;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.advancements.CriterionListeners;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MaxFieldsTrigger extends AbstractCriterionTrigger<CriterionListeners<CriterionInstance>, CriterionInstance>
{
    private final static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_MAX_FIELDS);

    public MaxFieldsTrigger()
    {
        super(ID, CriterionListeners::new);
    }

    public void trigger(final ServerPlayerEntity player)
    {
        final CriterionListeners<CriterionInstance> listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger();
        }
    }

    @NotNull
    @Override
    public CriterionInstance deserializeInstance(@NotNull final JsonObject jsonObject, @NotNull final JsonDeserializationContext jsonDeserializationContext)
    {
        return new CriterionInstance(ID);
    }
}
