package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.core.colony.colonyEvents.raidEvents.amazonevent.AmazonRaidEvent;
import com.minecolonies.core.colony.colonyEvents.raidEvents.barbarianEvent.BarbarianRaidEvent;
import com.minecolonies.core.colony.colonyEvents.raidEvents.egyptianevent.EgyptianRaidEvent;
import com.minecolonies.core.colony.colonyEvents.raidEvents.norsemenevent.NorsemenRaidEvent;
import com.minecolonies.core.colony.colonyEvents.raidEvents.norsemenevent.NorsemenShipRaidEvent;
import com.minecolonies.core.colony.colonyEvents.raidEvents.pirateEvent.PirateGroundRaidEvent;
import com.minecolonies.core.colony.colonyEvents.raidEvents.pirateEvent.PirateRaidEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Initializer for colony event types, register new event types here.
 */
public final class ModColonyEventTypeInitializer
{
    public final static DeferredRegister<ColonyEventTypeRegistryEntry> DEFERRED_REGISTER = DeferredRegister.create(CommonMinecoloniesAPIImpl.COLONY_EVENT_TYPES, Constants.MOD_ID);

    private ModColonyEventTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModColonyEventTypeInitializer but this is a Utility class.");
    }

    static
    {
        DEFERRED_REGISTER.register(PirateRaidEvent.PIRATE_RAID_EVENT_TYPE_ID.getPath(), () -> new ColonyEventTypeRegistryEntry(PirateRaidEvent::loadFromNBT, PirateRaidEvent.PIRATE_RAID_EVENT_TYPE_ID));
        DEFERRED_REGISTER.register(BarbarianRaidEvent.BARBARIAN_RAID_EVENT_TYPE_ID.getPath(), () -> new ColonyEventTypeRegistryEntry(BarbarianRaidEvent::loadFromNBT, BarbarianRaidEvent.BARBARIAN_RAID_EVENT_TYPE_ID));
        DEFERRED_REGISTER.register(EgyptianRaidEvent.EGYPTIAN_RAID_EVENT_TYPE_ID.getPath(), () -> new ColonyEventTypeRegistryEntry(EgyptianRaidEvent::loadFromNBT, EgyptianRaidEvent.EGYPTIAN_RAID_EVENT_TYPE_ID));
        DEFERRED_REGISTER.register(AmazonRaidEvent.AMAZON_RAID_EVENT_TYPE_ID.getPath(), () -> new ColonyEventTypeRegistryEntry(AmazonRaidEvent::loadFromNBT, AmazonRaidEvent.AMAZON_RAID_EVENT_TYPE_ID));
        DEFERRED_REGISTER.register(NorsemenRaidEvent.NORSEMEN_RAID_EVENT_TYPE_ID.getPath(), () -> new ColonyEventTypeRegistryEntry(NorsemenRaidEvent::loadFromNBT, NorsemenRaidEvent.NORSEMEN_RAID_EVENT_TYPE_ID));
        DEFERRED_REGISTER.register(NorsemenShipRaidEvent.NORSEMEN_RAID_EVENT_TYPE_ID.getPath(), () -> new ColonyEventTypeRegistryEntry(NorsemenShipRaidEvent::loadFromNBT, NorsemenShipRaidEvent.NORSEMEN_RAID_EVENT_TYPE_ID));
        DEFERRED_REGISTER.register(PirateGroundRaidEvent.PIRATE_GROUND_RAID_EVENT_TYPE_ID.getPath(), () -> new ColonyEventTypeRegistryEntry(PirateGroundRaidEvent::loadFromNBT, PirateGroundRaidEvent.PIRATE_GROUND_RAID_EVENT_TYPE_ID));
    }
}
