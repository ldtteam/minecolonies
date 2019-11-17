package com.minecolonies.api.colony.interactionhandling;

import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

public final class ModInteractionResponseHandlers
{
    public static final ResourceLocation CHITCHAT       = new ResourceLocation(Constants.MOD_ID, "chitchat");
    public static final ResourceLocation REQUEST        = new ResourceLocation(Constants.MOD_ID, "request");
    public static final ResourceLocation ACTIONREQUIRED = new ResourceLocation(Constants.MOD_ID, "actionrequired");

    public static InteractionResponseHandlerEntry chitchat;
    public static InteractionResponseHandlerEntry request;
    public static InteractionResponseHandlerEntry actionrequired;

    private ModInteractionResponseHandlers()
    {
        throw new IllegalStateException("Tried to initialize: ModJobs but this is a Utility class.");
    }
}
