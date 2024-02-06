package com.minecolonies.core.colony.interactionhandling.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import net.minecraft.core.Registry;

public interface IInteractionResponseHandlerRegistry
{
    static Registry<InteractionResponseHandlerEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getInteractionResponseHandlerRegistry();
    }
}
