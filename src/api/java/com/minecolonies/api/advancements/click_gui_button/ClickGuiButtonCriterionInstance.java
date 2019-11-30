package com.minecolonies.api.advancements.click_gui_button;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.util.ResourceLocation;

public class ClickGuiButtonCriterionInstance extends AbstractCriterionInstance
{
    private String buttonId;
    private String windowResource;

    public ClickGuiButtonCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON));
    }

    public ClickGuiButtonCriterionInstance(final String buttonId)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON));

        this.buttonId = buttonId;
    }

    public ClickGuiButtonCriterionInstance(final String buttonId, final String windowResource)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON));

        this.windowResource = windowResource;
        this.buttonId = buttonId;
    }

    public boolean test(final String buttonId, final String windowResource)
    {
        if (this.buttonId != null && this.windowResource != null)
        {
            return this.buttonId.equalsIgnoreCase(buttonId) && this.windowResource.equalsIgnoreCase(windowResource);
        }
        else if (this.buttonId != null)
        {
            return this.buttonId.equalsIgnoreCase(buttonId);
        }

        return true;
    }
}
