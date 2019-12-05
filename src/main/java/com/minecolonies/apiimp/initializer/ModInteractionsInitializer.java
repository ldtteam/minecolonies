package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.coremod.colony.interactionhandling.PosBasedInteractionResponseHandler;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteractionResponseHandler;
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

        ModInteractionResponseHandlers.standard = new InteractionResponseHandlerEntry.Builder()
                            .setJobProducer(StandardInteractionResponseHandler::new)
                            .setRegistryName(ModInteractionResponseHandlers.STANDARD)
                            .createJobEntry();

        ModInteractionResponseHandlers.pos = new InteractionResponseHandlerEntry.Builder()
                                                    .setJobProducer(PosBasedInteractionResponseHandler::new)
                                                    .setRegistryName(ModInteractionResponseHandlers.POS)
                                                    .createJobEntry();


        reg.register(ModInteractionResponseHandlers.standard);
        reg.register(ModInteractionResponseHandlers.pos);
    }
}
