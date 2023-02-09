package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.interactionhandling.PosBasedInteraction;
import com.minecolonies.coremod.colony.interactionhandling.RecruitmentInteraction;
import com.minecolonies.coremod.colony.interactionhandling.RequestBasedInteraction;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

public final class ModInteractionsInitializer
{
    public final static DeferredRegister<InteractionResponseHandlerEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "interactionresponsehandlers"), Constants.MOD_ID);

    private ModInteractionsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModInteractionsInitializer but this is a Utility class.");
    }

    static
    {
        ModInteractionResponseHandlers.standard = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.STANDARD.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                                    .setResponseHandlerProducer(StandardInteraction::new)
                                                    .createEntry());

        ModInteractionResponseHandlers.simpleNotification = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.SIMPLE_NOTIFICATION.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                                              .setResponseHandlerProducer(StandardInteraction::new)
                                                              .createEntry());

        ModInteractionResponseHandlers.pos = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.POS.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                               .setResponseHandlerProducer(PosBasedInteraction::new)
                                               .createEntry());

        ModInteractionResponseHandlers.request = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.REQUEST.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                                   .setResponseHandlerProducer(RequestBasedInteraction::new)
                                                   .createEntry());

        ModInteractionResponseHandlers.recruitment = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.RECRUITMENT.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                                       .setResponseHandlerProducer(RecruitmentInteraction::new)
                                                       .createEntry());
    }
}
