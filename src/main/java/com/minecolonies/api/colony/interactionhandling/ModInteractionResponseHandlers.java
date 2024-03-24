package com.minecolonies.api.colony.interactionhandling;

import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * List of mod interaction handlers.
 */
public final class ModInteractionResponseHandlers
{
    /**
     * List of IDs.
     */
    public static final ResourceLocation STANDARD            = new ResourceLocation(Constants.MOD_ID, "standard");
    public static final ResourceLocation SIMPLE_NOTIFICATION = new ResourceLocation(Constants.MOD_ID, "simplenotification");
    public static final ResourceLocation POS                 = new ResourceLocation(Constants.MOD_ID, "pos");
    public static final ResourceLocation REQUEST             = new ResourceLocation(Constants.MOD_ID, "request");
    public static final ResourceLocation RECRUITMENT         = new ResourceLocation(Constants.MOD_ID, "recruitment");
    public static final ResourceLocation QUEST               = new ResourceLocation(Constants.MOD_ID, "quest");
    public static final ResourceLocation QUEST_ACTION        = new ResourceLocation(Constants.MOD_ID, "questaction");

    /**
     * List of entries.
     */
    public static DeferredHolder<InteractionResponseHandlerEntry, InteractionResponseHandlerEntry> standard;
    public static DeferredHolder<InteractionResponseHandlerEntry, InteractionResponseHandlerEntry> simpleNotification;
    public static DeferredHolder<InteractionResponseHandlerEntry, InteractionResponseHandlerEntry> pos;
    public static DeferredHolder<InteractionResponseHandlerEntry, InteractionResponseHandlerEntry> request;
    public static DeferredHolder<InteractionResponseHandlerEntry, InteractionResponseHandlerEntry> recruitment;
    public static DeferredHolder<InteractionResponseHandlerEntry, InteractionResponseHandlerEntry> quest;
    public static DeferredHolder<InteractionResponseHandlerEntry, InteractionResponseHandlerEntry> questAction;

    private ModInteractionResponseHandlers()
    {
        throw new IllegalStateException("Tried to initialize: ModJobs but this is a Utility class.");
    }
}
