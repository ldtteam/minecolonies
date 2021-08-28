package com.minecolonies.api.advancements.click_gui_button;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;

/**
 * The test instance to check the various conditions for the "click_gui_button" trigger
 */
public class ClickGuiButtonCriterionInstance extends AbstractCriterionTriggerInstance
{
    private String buttonId;
    private String windowResource;

    public ClickGuiButtonCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON), EntityPredicate.Composite.ANY);
    }

    /**
     * Construct the check with a single button condition
     * @param buttonId the button to be clicked to succeed
     */
    public ClickGuiButtonCriterionInstance(final String buttonId)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON), EntityPredicate.Composite.ANY);

        this.buttonId = buttonId;
    }

    /**
     * Construct the check more specifically
     * @param buttonId the button to be clicked to succeed
     * @param windowResource the window id of the button to be clicked
     */
    public ClickGuiButtonCriterionInstance(final String buttonId, final String windowResource)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON), EntityPredicate.Composite.ANY);

        this.windowResource = windowResource;
        this.buttonId = buttonId;
    }

    /**
     * Performs the check for the conditions
     * @param buttonId the id of the button that was just clicked
     * @param windowResource the blockui window id to check
     * @return whether the check succeeded
     */
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
