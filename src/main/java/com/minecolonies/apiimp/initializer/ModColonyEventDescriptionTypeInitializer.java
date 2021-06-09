package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.coremod.colony.colonyEvents.buildingEvents.BuildingBuiltEvent;
import com.minecolonies.coremod.colony.colonyEvents.buildingEvents.BuildingDeconstructedEvent;
import com.minecolonies.coremod.colony.colonyEvents.buildingEvents.BuildingUpgradedEvent;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Initializer for colony event types, register new event types here.
 */
public final class ModColonyEventDescriptionTypeInitializer
{
    private ModColonyEventDescriptionTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModColonyEventDescriptionTypeInitializer but this is a Utility class.");
    }

    public static void init(final RegistryEvent.Register<ColonyEventDescriptionTypeRegistryEntry> event)
    {
        final IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> reg = event.getRegistry();
        reg.register(new ColonyEventDescriptionTypeRegistryEntry(CitizenBornEvent::loadFromNBT, CitizenBornEvent::loadFromPacketBuffer, CitizenBornEvent.CITIZEN_BORN_EVENT_ID));
        reg.register(new ColonyEventDescriptionTypeRegistryEntry(CitizenSpawnedEvent::loadFromNBT, CitizenSpawnedEvent::loadFromPacketBuffer, CitizenSpawnedEvent.CITIZEN_SPAWNED_EVENT_ID));
        reg.register(new ColonyEventDescriptionTypeRegistryEntry(VisitorSpawnedEvent::loadFromNBT, VisitorSpawnedEvent::loadFromPacketBuffer, VisitorSpawnedEvent.VISITOR_SPAWNED_EVENT_ID));
        reg.register(new ColonyEventDescriptionTypeRegistryEntry(CitizenDiedEvent::loadFromNBT, CitizenDiedEvent::loadFromPacketBuffer, CitizenDiedEvent.CITIZEN_DIED_EVENT_ID));
        reg.register(new ColonyEventDescriptionTypeRegistryEntry(CitizenGrownUpEvent::loadFromNBT, CitizenGrownUpEvent::loadFromPacketBuffer, CitizenGrownUpEvent.CITIZEN_GROWN_UP_EVENT_ID));
        reg.register(new ColonyEventDescriptionTypeRegistryEntry(BuildingBuiltEvent::loadFromNBT, BuildingBuiltEvent::loadFromPacketBuffer, BuildingBuiltEvent.BUILDING_BUILT_EVENT_ID));
        reg.register(new ColonyEventDescriptionTypeRegistryEntry(BuildingUpgradedEvent::loadFromNBT, BuildingUpgradedEvent::loadFromPacketBuffer, BuildingUpgradedEvent.BUILDING_UPGRADED_EVENT_ID));
        reg.register(new ColonyEventDescriptionTypeRegistryEntry(BuildingDeconstructedEvent::loadFromNBT, BuildingDeconstructedEvent::loadFromPacketBuffer, BuildingDeconstructedEvent.BUILDING_DECONSTRUCTED_EVENT_ID));
    }
}
