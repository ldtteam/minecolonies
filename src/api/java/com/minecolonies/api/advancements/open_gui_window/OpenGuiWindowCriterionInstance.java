package com.minecolonies.api.advancements.open_gui_window;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.util.ResourceLocation;

public class OpenGuiWindowCriterionInstance extends CriterionInstance
{
    private String windowResource;

    public OpenGuiWindowCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_OPEN_GUI_WINDOW));
    }

    public OpenGuiWindowCriterionInstance(final String windowResource)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_OPEN_GUI_WINDOW));

        this.windowResource = windowResource;
    }

    public boolean test(final String windowResource)
    {
        if (this.windowResource != null)
        {
            return this.windowResource.equalsIgnoreCase(windowResource);
        }
        return true;
    }
}
