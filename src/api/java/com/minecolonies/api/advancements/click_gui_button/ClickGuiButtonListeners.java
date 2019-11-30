package com.minecolonies.api.advancements.click_gui_button;

import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.advancements.PlayerAdvancements;

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
