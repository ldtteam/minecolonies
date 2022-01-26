package com.minecolonies.api.advancements.open_gui_window;

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

    @NotNull
    public static OpenGuiWindowCriterionInstance deserializeFromJson(@NotNull final JsonObject jsonObject,
                                                                     @NotNull final ConditionArrayParser conditions)
    {
        if (jsonObject.has("window_resource_location"))
        {
            final String windowResource = JSONUtils.getAsString(jsonObject, "window_resource_location");
            return new OpenGuiWindowCriterionInstance(windowResource);
        }
        return new OpenGuiWindowCriterionInstance();
    }

    @NotNull
    @Override
    public JsonObject serializeToJson(@NotNull final ConditionArraySerializer serializer)
    {
        final JsonObject json = super.serializeToJson(serializer);
        if (this.windowResource != null)
        {
            json.addProperty("window_resource_location", this.windowResource);
        }
        return json;
    }
}
