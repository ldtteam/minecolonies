package com.minecolonies.api.advancements.click_gui_button;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ClickGuiButtonTrigger extends AbstractCriterionTrigger<ClickGuiButtonListeners, ClickGuiButtonCriterionInstance>
{
    public ClickGuiButtonTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON), ClickGuiButtonListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     * @param buttonId the id of the button in blockui
     * @param windowResource the blockui window id to refer to
     */
    public void trigger(final ServerPlayer player, final String buttonId, final String windowResource)
    {
        final ClickGuiButtonListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(buttonId, windowResource);
        }
    }

    @NotNull
    @Override
    public ClickGuiButtonCriterionInstance createInstance(@NotNull final JsonObject jsonObject, @NotNull final DeserializationContext conditionArrayParser)
    {
        return ClickGuiButtonCriterionInstance.deserializeFromJson(jsonObject, conditionArrayParser);
    }
}
