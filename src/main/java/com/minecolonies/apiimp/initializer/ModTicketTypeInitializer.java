package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.TicketType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Registers Minecolonies' chunk-loading ticket before built-in registries freeze. */
@EventBusSubscriber(modid = Constants.MOD_ID)
public final class ModTicketTypeInitializer
{
    private ModTicketTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize ModTicketTypeInitializer as a utility class.");
    }

    @SubscribeEvent
    public static void registerTicketType(final RegisterEvent event)
    {
        if (event.getRegistryKey().equals(Registries.TICKET_TYPE))
        {
            event.register(
                Registries.TICKET_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "initial_chunkload"),
                () -> {
                    final TicketType ticketType = new TicketType(0L, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION);
                    ColonyConstants.KEEP_LOADED_TYPE = ticketType;
                    return ticketType;
                });
        }
    }
}
