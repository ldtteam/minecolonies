package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.events.raid.amazonevent.AmazonRaidEvent;
import com.minecolonies.core.colony.events.raid.barbarianEvent.BarbarianRaidEvent;
import com.minecolonies.core.colony.events.raid.egyptianevent.EgyptianRaidEvent;
import com.minecolonies.core.colony.events.raid.norsemenevent.NorsemenRaidEvent;
import com.minecolonies.core.colony.events.raid.norsemenevent.NorsemenShipRaidEvent;
import com.minecolonies.core.colony.events.raid.pirateEvent.DrownedPirateRaidEvent;
import com.minecolonies.core.colony.events.raid.pirateEvent.PirateGroundRaidEvent;
import com.minecolonies.core.colony.events.raid.pirateEvent.PirateRaidEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

/**
 * Initializer for colony event types, register new event types here.
 */
public final class ModColonyEventTypeInitializer
{
    public final static DeferredRegister<ColonyEventTypeRegistryEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "colonyeventtypes"), Constants.MOD_ID);

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
        DEFERRED_REGISTER.register(DrownedPirateRaidEvent.PIRATE_RAID_EVENT_TYPE_ID.getPath(), () -> new ColonyEventTypeRegistryEntry(DrownedPirateRaidEvent::loadFromNBT, DrownedPirateRaidEvent.PIRATE_RAID_EVENT_TYPE_ID));
    }
}
