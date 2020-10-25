package com.minecolonies.api.advancements.open_gui_window;

import com.minecolonies.api.advancements.CriterionListeners;
import net.minecraft.advancements.PlayerAdvancements;

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
