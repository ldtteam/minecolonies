package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.colony.interactionhandling.registry.InteractionResponseHandlerEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.interactionhandling.*;
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
                                                    .setRegistryName(ModInteractionResponseHandlers.STANDARD)
                                                    .createEntry());

        ModInteractionResponseHandlers.simpleNotification = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.SIMPLE_NOTIFICATION.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                                              .setResponseHandlerProducer(StandardInteraction::new)
                                                              .setRegistryName(ModInteractionResponseHandlers.SIMPLE_NOTIFICATION)
                                                              .createEntry());

        ModInteractionResponseHandlers.pos = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.POS.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                               .setResponseHandlerProducer(PosBasedInteraction::new)
                                               .setRegistryName(ModInteractionResponseHandlers.POS)
                                               .createEntry());

        ModInteractionResponseHandlers.request = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.REQUEST.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                                   .setResponseHandlerProducer(RequestBasedInteraction::new)
                                                   .setRegistryName(ModInteractionResponseHandlers.REQUEST)
                                                   .createEntry());

        ModInteractionResponseHandlers.recruitment = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.RECRUITMENT.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                                       .setResponseHandlerProducer(RecruitmentInteraction::new)
                                                       .setRegistryName(ModInteractionResponseHandlers.RECRUITMENT)
                                                       .createEntry());

        ModInteractionResponseHandlers.quest = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.QUEST.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                                      .setResponseHandlerProducer(QuestDialogueInteraction::new)
                                                      .setRegistryName(ModInteractionResponseHandlers.QUEST)
                                                      .createEntry());

        ModInteractionResponseHandlers.questAction = DEFERRED_REGISTER.register(ModInteractionResponseHandlers.QUEST_ACTION.getPath(), () -> new InteractionResponseHandlerEntry.Builder()
                                                     .setResponseHandlerProducer(QuestDeliveryInteraction::new)
                                                     .setRegistryName(ModInteractionResponseHandlers.QUEST_ACTION)
                                                     .createEntry());
    }
}
