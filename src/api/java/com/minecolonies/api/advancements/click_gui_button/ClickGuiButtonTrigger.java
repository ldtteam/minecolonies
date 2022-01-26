package com.minecolonies.api.advancements.click_gui_button;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
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
     * @param buttonId the id of the button in blockout
     * @param windowResource the blockout window id to refer to
     */
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
    public ClickGuiButtonCriterionInstance createInstance(@NotNull final JsonObject jsonObject, @NotNull final ConditionArrayParser conditionArrayParser)
    {
        return ClickGuiButtonCriterionInstance.deserializeFromJson(jsonObject, conditionArrayParser);
    }
}
