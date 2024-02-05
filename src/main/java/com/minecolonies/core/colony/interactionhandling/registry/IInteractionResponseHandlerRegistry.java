package com.minecolonies.core.colony.interactionhandling.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import net.neoforged.neoforge.registries.IForgeRegistry;

public interface IInteractionResponseHandlerRegistry
{
    static IForgeRegistry<InteractionResponseHandlerEntry> getInstance()
    {
        return IMinecoloniesAPI.getInstance().getInteractionResponseHandlerRegistry();
    }
}
