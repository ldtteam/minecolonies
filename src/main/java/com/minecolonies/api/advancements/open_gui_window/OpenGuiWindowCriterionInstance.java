package com.minecolonies.api.advancements.open_gui_window;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.util.GsonHelper;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The test instance to check the "window_resource_location" for the "open_gui_window" trigger
 */
public class OpenGuiWindowCriterionInstance extends AbstractCriterionTriggerInstance
{
    private ResourceLocation windowResource;

    public OpenGuiWindowCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_OPEN_GUI_WINDOW), ContextAwarePredicate.ANY);
    }

    /**
     * Construct the check with a single condition
     * @param windowResource the window that has to be opened to succeed
     */
    public OpenGuiWindowCriterionInstance(final ResourceLocation windowResource)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_OPEN_GUI_WINDOW), ContextAwarePredicate.ANY);

        this.windowResource = windowResource;
    }

    /**
     * Performs the check for the conditions
     * @param windowResource the blockui window id that was just opened
     * @return whether the check succeeded
     */
    public boolean test(final ResourceLocation windowResource)
    {
        return Objects.equals(this.windowResource, windowResource);
    }

    @NotNull
    public static OpenGuiWindowCriterionInstance deserializeFromJson(@NotNull final JsonObject jsonObject,
                                                                     @NotNull final DeserializationContext context)
    {
        if (jsonObject.has("window_resource_location"))
        {
            final ResourceLocation windowResource = GsonHelper.getAsResourceLocation(jsonObject, "window_resource_location");
            return new OpenGuiWindowCriterionInstance(windowResource);
        }
        return new OpenGuiWindowCriterionInstance();
    }

    @NotNull
    @Override
    public JsonObject serializeToJson(@NotNull final SerializationContext context)
    {
        final JsonObject json = super.serializeToJson(context);
        if (this.windowResource != null)
        {
            json.addProperty("window_resource_location", this.windowResource.toString());
        }
        return json;
    }
}
