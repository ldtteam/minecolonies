package com.minecolonies.api.advancements.open_gui_window;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.util.ResourceLocation;

/**
 * The test instance to check the "window_resource_location" for the "open_gui_window" trigger
 */
public class OpenGuiWindowCriterionInstance extends CriterionInstance
{
    private String windowResource;

    public OpenGuiWindowCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_OPEN_GUI_WINDOW), EntityPredicate.AndPredicate.ANY);
    }

    /**
     * Construct the check with a single condition
     * @param windowResource the window that has to be opened to succeed
     */
    public OpenGuiWindowCriterionInstance(final String windowResource)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_OPEN_GUI_WINDOW), EntityPredicate.AndPredicate.ANY);

        this.windowResource = windowResource;
    }

    /**
     * Performs the check for the conditions
     * @param windowResource the blockout window id that was just opened
     * @return whether the check succeeded
     */
    public boolean test(final String windowResource)
    {
        if (this.windowResource != null)
        {
            return this.windowResource.equalsIgnoreCase(windowResource);
        }
        return true;
    }
}
