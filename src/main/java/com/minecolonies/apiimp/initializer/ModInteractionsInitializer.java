package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.coremod.colony.interactionhandling.PosBasedInteraction;
import com.minecolonies.coremod.colony.interactionhandling.RecruitmentInteraction;
import com.minecolonies.coremod.colony.interactionhandling.RequestBasedInteraction;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModInteractionsInitializer
{
    private ModInteractionsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModInteractionsInitializer but this is a Utility class.");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final RegistryEvent.Register<InteractionResponseHandlerEntry> event)
    {
        final IForgeRegistry<InteractionResponseHandlerEntry> reg = event.getRegistry();

        ModInteractionResponseHandlers.standard = new InteractionResponseHandlerEntry.Builder()
                                                    .setResponseHandlerProducer(StandardInteraction::new)
                                                    .setRegistryName(ModInteractionResponseHandlers.STANDARD)
                                                    .createEntry();

        ModInteractionResponseHandlers.simpleNotification = new InteractionResponseHandlerEntry.Builder()
                                                              .setResponseHandlerProducer(StandardInteraction::new)
                                                              .setRegistryName(ModInteractionResponseHandlers.SIMPLE_NOTIFICATION)
                                                              .createEntry();

        ModInteractionResponseHandlers.pos = new InteractionResponseHandlerEntry.Builder()
                                               .setResponseHandlerProducer(PosBasedInteraction::new)
                                               .setRegistryName(ModInteractionResponseHandlers.POS)
                                               .createEntry();

        ModInteractionResponseHandlers.request = new InteractionResponseHandlerEntry.Builder()
                                                   .setResponseHandlerProducer(RequestBasedInteraction::new)
                                                   .setRegistryName(ModInteractionResponseHandlers.REQUEST)
                                                   .createEntry();

        ModInteractionResponseHandlers.recruitment = new InteractionResponseHandlerEntry.Builder()
                                                       .setResponseHandlerProducer(RecruitmentInteraction::new)
                                                       .setRegistryName(ModInteractionResponseHandlers.RECRUITMENT)
                                                       .createEntry();

        reg.register(ModInteractionResponseHandlers.standard);
        reg.register(ModInteractionResponseHandlers.pos);
        reg.register(ModInteractionResponseHandlers.request);
        reg.register(ModInteractionResponseHandlers.simpleNotification);
        reg.register(ModInteractionResponseHandlers.recruitment);
    }
}
