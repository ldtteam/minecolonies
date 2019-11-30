package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.coremod.colony.interactionhandling.ChitChatInteractionResponseHandler;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModInteractionsInitializer
{
    private ModInteractionsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModJobsInitializer but this is a Utility class.");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final RegistryEvent.Register<InteractionResponseHandlerEntry> event)
    {
        final IForgeRegistry<InteractionResponseHandlerEntry> reg = event.getRegistry();

        ModInteractionResponseHandlers.chitchat = new InteractionResponseHandlerEntry.Builder()
                            .setJobProducer(ChitChatInteractionResponseHandler::new)
                            .setRegistryName(ModInteractionResponseHandlers.CHITCHAT)
                            .createJobEntry();

        reg.register(ModInteractionResponseHandlers.chitchat);
    }
}
