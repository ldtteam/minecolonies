package com.minecolonies.api.advancements.open_gui_window;

import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.server.PlayerAdvancements;

/**
 * The listener instantiated for every advancement that listens to the associated criterion.
 * A basic class to trigger with the correct arguments
 */
public class OpenGuiWindowListeners extends CriterionListeners<OpenGuiWindowCriterionInstance>
{
    public OpenGuiWindowListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger(final String windowResource)
    {
        trigger(instance -> instance.test(windowResource));
    }
}
