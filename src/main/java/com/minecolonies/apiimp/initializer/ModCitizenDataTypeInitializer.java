package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.citizen.CitizenDataRegistryEntry;
import com.minecolonies.coremod.colony.CitizenData;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Initializer for citizen data registry entries
 */
public class ModCitizenDataTypeInitializer
{
    private ModCitizenDataTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: CitizenDataInitializer but this is a Utility class.");
    }

    public static void init(final RegistryEvent.Register<CitizenDataRegistryEntry> event)
    {
        final IForgeRegistry<CitizenDataRegistryEntry> reg = event.getRegistry();
        reg.register(new CitizenDataRegistryEntry(CitizenData::loadFromNBT, CitizenData.CITIZEN_DATA_TYPE));
    }
}
