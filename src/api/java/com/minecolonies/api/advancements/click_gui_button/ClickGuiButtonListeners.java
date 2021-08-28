package com.minecolonies.api.advancements.click_gui_button;

import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.server.PlayerAdvancements;

/**
 * The listener instantiated for every advancement that listens to the associated criterion.
 * A basic class to trigger with the correct arguments
 */
public class ClickGuiButtonListeners extends CriterionListeners<ClickGuiButtonCriterionInstance>
{
    public ClickGuiButtonListeners(final PlayerAdvancements playerAdvancements)
    {
        super(playerAdvancements);
    }

    public void trigger(final String buttonId, final String windowResource)
    {
        trigger(instance -> instance.test(buttonId, windowResource));
    }
}
