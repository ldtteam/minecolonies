package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.colonyEvents.buildingEvents.BuildingBuiltEvent;
import com.minecolonies.core.colony.colonyEvents.buildingEvents.BuildingDeconstructedEvent;
import com.minecolonies.core.colony.colonyEvents.buildingEvents.BuildingRepairedEvent;
import com.minecolonies.core.colony.colonyEvents.buildingEvents.BuildingUpgradedEvent;
import com.minecolonies.core.colony.colonyEvents.citizenEvents.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Initializer for colony event types, register new event types here.
 */
public final class ModColonyEventDescriptionTypeInitializer
{
    public final static DeferredRegister<ColonyEventDescriptionTypeRegistryEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "colonyeventdesctypes"), Constants.MOD_ID);

    private ModColonyEventDescriptionTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModColonyEventDescriptionTypeInitializer but this is a Utility class.");
    }

    static
    {
        DEFERRED_REGISTER.register(CitizenBornEvent.CITIZEN_BORN_EVENT_ID.getPath(), () -> new ColonyEventDescriptionTypeRegistryEntry(CitizenBornEvent::loadFromNBT, CitizenBornEvent::loadFromFriendlyByteBuf, CitizenBornEvent.CITIZEN_BORN_EVENT_ID));
        DEFERRED_REGISTER.register(CitizenSpawnedEvent.CITIZEN_SPAWNED_EVENT_ID.getPath(), () -> new ColonyEventDescriptionTypeRegistryEntry(CitizenSpawnedEvent::loadFromNBT, CitizenSpawnedEvent::loadFromFriendlyByteBuf, CitizenSpawnedEvent.CITIZEN_SPAWNED_EVENT_ID));
        DEFERRED_REGISTER.register(VisitorSpawnedEvent.VISITOR_SPAWNED_EVENT_ID.getPath(), () -> new ColonyEventDescriptionTypeRegistryEntry(VisitorSpawnedEvent::loadFromNBT, VisitorSpawnedEvent::loadFromFriendlyByteBuf, VisitorSpawnedEvent.VISITOR_SPAWNED_EVENT_ID));
        DEFERRED_REGISTER.register(CitizenDiedEvent.CITIZEN_DIED_EVENT_ID.getPath(), () -> new ColonyEventDescriptionTypeRegistryEntry(CitizenDiedEvent::loadFromNBT, CitizenDiedEvent::loadFromFriendlyByteBuf, CitizenDiedEvent.CITIZEN_DIED_EVENT_ID));
        DEFERRED_REGISTER.register(CitizenGrownUpEvent.CITIZEN_GROWN_UP_EVENT_ID.getPath(), () -> new ColonyEventDescriptionTypeRegistryEntry(CitizenGrownUpEvent::loadFromNBT, CitizenGrownUpEvent::loadFromFriendlyByteBuf, CitizenGrownUpEvent.CITIZEN_GROWN_UP_EVENT_ID));
        DEFERRED_REGISTER.register(BuildingBuiltEvent.BUILDING_BUILT_EVENT_ID.getPath(), () -> new ColonyEventDescriptionTypeRegistryEntry(BuildingBuiltEvent::loadFromNBT, BuildingBuiltEvent::loadFromFriendlyByteBuf, BuildingBuiltEvent.BUILDING_BUILT_EVENT_ID));
        DEFERRED_REGISTER.register(BuildingUpgradedEvent.BUILDING_UPGRADED_EVENT_ID.getPath(), () -> new ColonyEventDescriptionTypeRegistryEntry(BuildingUpgradedEvent::loadFromNBT, BuildingUpgradedEvent::loadFromFriendlyByteBuf, BuildingUpgradedEvent.BUILDING_UPGRADED_EVENT_ID));
        DEFERRED_REGISTER.register(BuildingRepairedEvent.BUILDING_REPAIRED_EVENT_ID.getPath(), () -> new ColonyEventDescriptionTypeRegistryEntry(BuildingRepairedEvent::loadFromNBT, BuildingRepairedEvent::loadFromFriendlyByteBuf, BuildingRepairedEvent.BUILDING_REPAIRED_EVENT_ID));
        DEFERRED_REGISTER.register(BuildingDeconstructedEvent.BUILDING_DECONSTRUCTED_EVENT_ID.getPath(), () -> new ColonyEventDescriptionTypeRegistryEntry(BuildingDeconstructedEvent::loadFromNBT, BuildingDeconstructedEvent::loadFromFriendlyByteBuf, BuildingDeconstructedEvent.BUILDING_DECONSTRUCTED_EVENT_ID));
    }
}
