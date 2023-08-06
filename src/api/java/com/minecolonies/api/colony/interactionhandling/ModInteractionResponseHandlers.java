package com.minecolonies.api.colony.interactionhandling;

import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

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
    public static RegistryObject<InteractionResponseHandlerEntry> standard;
    public static RegistryObject<InteractionResponseHandlerEntry> simpleNotification;
    public static RegistryObject<InteractionResponseHandlerEntry> pos;
    public static RegistryObject<InteractionResponseHandlerEntry> request;
    public static RegistryObject<InteractionResponseHandlerEntry> recruitment;
    public static RegistryObject<InteractionResponseHandlerEntry> quest;
    public static RegistryObject<InteractionResponseHandlerEntry> questAction;

    private ModInteractionResponseHandlers()
    {
        throw new IllegalStateException("Tried to initialize: ModJobs but this is a Utility class.");
    }
}
