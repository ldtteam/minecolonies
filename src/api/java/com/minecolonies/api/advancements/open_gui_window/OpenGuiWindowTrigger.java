package com.minecolonies.api.advancements.open_gui_window;

import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Triggered when a blockout window is opened
 */
public class OpenGuiWindowTrigger extends AbstractCriterionTrigger<OpenGuiWindowListeners, OpenGuiWindowCriterionInstance>
{
    public OpenGuiWindowTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_OPEN_GUI_WINDOW), OpenGuiWindowListeners::new);
    }

    /**
     * Triggers the listener checks if there are any listening in
     * @param player the player the check regards
     * @param windowResource the window id that was just opened
     */
    public void trigger(final ServerPlayerEntity player, final String windowResource)
    {
        final OpenGuiWindowListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(windowResource);
        }
    }

    @NotNull
    @Override
    public OpenGuiWindowCriterionInstance createInstance(@NotNull final JsonObject jsonObject, @NotNull final ConditionArrayParser conditionArrayParser)
    {
        if (jsonObject.has("window_resource_location"))
        {
            final String windowResource = JSONUtils.getAsString(jsonObject, "window_resource_location");
            return new OpenGuiWindowCriterionInstance(windowResource);
        }
        return new OpenGuiWindowCriterionInstance();
    }
}
