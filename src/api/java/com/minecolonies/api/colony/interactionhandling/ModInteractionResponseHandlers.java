package com.minecolonies.api.colony.interactionhandling;

import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

/**
 * List of mod interaction handlers.
 */
public final class ModInteractionResponseHandlers
{
    /**
     * List of IDs.
     */
    public static final ResourceLocation STANDARD = new ResourceLocation(Constants.MOD_ID, "standard");
    public static final ResourceLocation POS = new ResourceLocation(Constants.MOD_ID, "pos");
    public static final ResourceLocation REQUEST = new ResourceLocation(Constants.MOD_ID, "request");

    /**
     * List of entries.
     */
    public static InteractionResponseHandlerEntry standard;
    public static InteractionResponseHandlerEntry pos;
    public static InteractionResponseHandlerEntry request;

    private ModInteractionResponseHandlers()
    {
        throw new IllegalStateException("Tried to initialize: ModJobs but this is a Utility class.");
    }
}
