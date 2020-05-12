package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent.BarbarianRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.egyptianevent.EgyptianRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateRaidEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Initializer for colony event types, register new event types here.
 */
public final class ModColonyEventTypeInitializer
{
    private ModColonyEventTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: RaidTypeRegistration but this is a Utility class.");
    }

    public static void init(final RegistryEvent.Register<ColonyEventTypeRegistryEntry> event)
    {
        final IForgeRegistry<ColonyEventTypeRegistryEntry> reg = event.getRegistry();
        reg.register(new ColonyEventTypeRegistryEntry(PirateRaidEvent::loadFromNBT, PirateRaidEvent.PIRATE_RAID_EVENT_TYPE_ID));
        reg.register(new ColonyEventTypeRegistryEntry(BarbarianRaidEvent::loadFromNBT, BarbarianRaidEvent.BABARIAN_RAID_EVENT_TYPE_ID));
        reg.register(new ColonyEventTypeRegistryEntry(EgyptianRaidEvent::loadFromNBT, EgyptianRaidEvent.EGYPTIAN_RAID_EVENT_TYPE_ID));
    }
}
