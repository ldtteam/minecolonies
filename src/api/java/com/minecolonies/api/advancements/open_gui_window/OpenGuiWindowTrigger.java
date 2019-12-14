package com.minecolonies.api.advancements.open_gui_window;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.minecolonies.api.advancements.AbstractCriterionTrigger;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public class OpenGuiWindowTrigger extends AbstractCriterionTrigger<OpenGuiWindowListeners, OpenGuiWindowCriterionInstance>
{
    public OpenGuiWindowTrigger()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_OPEN_GUI_WINDOW), OpenGuiWindowListeners::new);
    }

    public void trigger(final EntityPlayerMP player, final String windowResource)
    {
        final OpenGuiWindowListeners listeners = this.getListeners(player.getAdvancements());
        if (listeners != null)
        {
            listeners.trigger(windowResource);
        }
    }

    @NotNull
    @Override
    public OpenGuiWindowCriterionInstance deserializeInstance(@NotNull final JsonObject jsonObject, @NotNull final JsonDeserializationContext jsonDeserializationContext)
    {
        if (jsonObject.has("window_resource_location"))
        {
            final String windowResource = JsonUtils.getString(jsonObject, "window_resource_location");
            return new OpenGuiWindowCriterionInstance(windowResource);
        }
        return new OpenGuiWindowCriterionInstance();
    }
}
