package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.coremod.colony.colonyEvents.buildingEvents.BuildingBuiltEvent;
import com.minecolonies.coremod.colony.colonyEvents.buildingEvents.BuildingDeconstructedEvent;
import com.minecolonies.coremod.colony.colonyEvents.buildingEvents.BuildingRepairedEvent;
import com.minecolonies.coremod.colony.colonyEvents.buildingEvents.BuildingUpgradedEvent;
import com.minecolonies.coremod.colony.colonyEvents.citizenEvents.*;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Initializer for colony event types, register new event types here.
 */
public final class ModColonyEventDescriptionTypeInitializer
{
    private ModColonyEventDescriptionTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModColonyEventDescriptionTypeInitializer but this is a Utility class.");
    }

    public static void init(final RegisterEvent event)
    {
        final IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> reg = event.getForgeRegistry();
        register(reg, new ColonyEventDescriptionTypeRegistryEntry(CitizenBornEvent::loadFromNBT, CitizenBornEvent::loadFromFriendlyByteBuf, CitizenBornEvent.CITIZEN_BORN_EVENT_ID));
        register(reg, new ColonyEventDescriptionTypeRegistryEntry(CitizenSpawnedEvent::loadFromNBT, CitizenSpawnedEvent::loadFromFriendlyByteBuf, CitizenSpawnedEvent.CITIZEN_SPAWNED_EVENT_ID));
        register(reg, new ColonyEventDescriptionTypeRegistryEntry(VisitorSpawnedEvent::loadFromNBT, VisitorSpawnedEvent::loadFromFriendlyByteBuf, VisitorSpawnedEvent.VISITOR_SPAWNED_EVENT_ID));
        register(reg, new ColonyEventDescriptionTypeRegistryEntry(CitizenDiedEvent::loadFromNBT, CitizenDiedEvent::loadFromFriendlyByteBuf, CitizenDiedEvent.CITIZEN_DIED_EVENT_ID));
        register(reg, new ColonyEventDescriptionTypeRegistryEntry(CitizenGrownUpEvent::loadFromNBT, CitizenGrownUpEvent::loadFromFriendlyByteBuf, CitizenGrownUpEvent.CITIZEN_GROWN_UP_EVENT_ID));
        register(reg, new ColonyEventDescriptionTypeRegistryEntry(BuildingBuiltEvent::loadFromNBT, BuildingBuiltEvent::loadFromFriendlyByteBuf, BuildingBuiltEvent.BUILDING_BUILT_EVENT_ID));
        register(reg, new ColonyEventDescriptionTypeRegistryEntry(BuildingUpgradedEvent::loadFromNBT, BuildingUpgradedEvent::loadFromFriendlyByteBuf, BuildingUpgradedEvent.BUILDING_UPGRADED_EVENT_ID));
        register(reg, new ColonyEventDescriptionTypeRegistryEntry(BuildingRepairedEvent::loadFromNBT, BuildingRepairedEvent::loadFromFriendlyByteBuf, BuildingRepairedEvent.BUILDING_REPAIRED_EVENT_ID));
        register(reg, new ColonyEventDescriptionTypeRegistryEntry(BuildingDeconstructedEvent::loadFromNBT, BuildingDeconstructedEvent::loadFromFriendlyByteBuf, BuildingDeconstructedEvent.BUILDING_DECONSTRUCTED_EVENT_ID));
    }
    
    /**
     * Register the building entry.
     * @param reg the registry to register it to.
     * @param entry the entry to register.
     */
    private static void register(final IForgeRegistry<ColonyEventDescriptionTypeRegistryEntry> reg, final ColonyEventDescriptionTypeRegistryEntry entry)
    {
        reg.register(entry.getRegistryName(), entry);
    }
}
