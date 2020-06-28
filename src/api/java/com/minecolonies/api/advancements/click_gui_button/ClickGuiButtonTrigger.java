package com.minecolonies.api.advancements.click_gui_button;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class ClickGuiButtonTrigger extends AbstractCriterionTrigger<ClickGuiButtonListeners, ClickGuiButtonCriterionInstance>
{
    public ClickGuiButtonTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON), ClickGuiButtonListeners::new);
    }

    public void trigger(final ServerPlayerEntity player, final String buttonId, final String windowResource)
    {
        final ClickGuiButtonListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(buttonId, windowResource);
        }
    }

    @NotNull
    @Override
    public ClickGuiButtonCriterionInstance deserializeInstance(@NotNull final JsonObject jsonObject, @NotNull final JsonDeserializationContext jsonDeserializationContext)
    {
        if (jsonObject.has("button_id"))
        {
            final String buttonId = JSONUtils.getString(jsonObject, "button_id");
            if (jsonObject.has("window_resource_location"))
            {
                final String windowResource = JSONUtils.getString(jsonObject, "window_resource_location");
                return new ClickGuiButtonCriterionInstance(buttonId, windowResource);
            }
            return new ClickGuiButtonCriterionInstance(buttonId);
        }
        return new ClickGuiButtonCriterionInstance();
    }
}
