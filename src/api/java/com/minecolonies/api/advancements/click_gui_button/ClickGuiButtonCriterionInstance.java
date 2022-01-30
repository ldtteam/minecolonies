package com.minecolonies.api.advancements.click_gui_button;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The test instance to check the various conditions for the "click_gui_button" trigger
 */
public class ClickGuiButtonCriterionInstance extends CriterionInstance
{
    private String buttonId;
    private String windowResource;

    public ClickGuiButtonCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON), EntityPredicate.AndPredicate.ANY);
    }

    /**
     * Construct the check with a single button condition
     * @param buttonId the button to be clicked to succeed
     */
    public ClickGuiButtonCriterionInstance(final String buttonId)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON), EntityPredicate.AndPredicate.ANY);

        this.buttonId = buttonId;
    }

    /**
     * Construct the check more specifically
     * @param buttonId the button to be clicked to succeed
     * @param windowResource the window id of the button to be clicked
     */
    public ClickGuiButtonCriterionInstance(final String buttonId, final String windowResource)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_CLICK_GUI_BUTTON), EntityPredicate.AndPredicate.ANY);

        this.windowResource = windowResource;
        this.buttonId = buttonId;
    }

    /**
     * Performs the check for the conditions
     * @param buttonId the id of the button that was just clicked
     * @param windowResource the blockout window id to check
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

    @NotNull
    public static ClickGuiButtonCriterionInstance deserializeFromJson(@NotNull final JsonObject jsonObject,
                                                                      @NotNull final ConditionArrayParser conditions)
    {
        if (jsonObject.has("button_id"))
        {
            final String buttonId = JSONUtils.getAsString(jsonObject, "button_id");
            if (jsonObject.has("window_resource_location"))
            {
                final String windowResource = JSONUtils.getAsString(jsonObject, "window_resource_location");
                return new ClickGuiButtonCriterionInstance(buttonId, windowResource);
            }
            return new ClickGuiButtonCriterionInstance(buttonId);
        }
        return new ClickGuiButtonCriterionInstance();
    }

    @NotNull
    @Override
    public JsonObject serializeToJson(@NotNull final ConditionArraySerializer serializer)
    {
        final JsonObject json = super.serializeToJson(serializer);
        if (this.buttonId != null)
        {
            json.addProperty("button_id", this.buttonId);
            if (this.windowResource != null)
            {
                json.addProperty("window_resource_location", this.windowResource);
            }
        }
        return json;
    }
}
