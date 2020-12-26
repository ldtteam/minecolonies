package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.raids.RaidType;
import com.minecolonies.api.colony.raids.registry.ModRaidTypes;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Initializes the built-in colony raider types
 */
public class ModRaidTypesInitializer
{
    private ModRaidTypesInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModGuardTypesInitializer but this is a Utility class.");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final RegistryEvent.Register<RaidType> event)
    {
        final IForgeRegistry<RaidType> reg = event.getRegistry();

        ModRaidTypes.amazon = new RaidType.Builder()
                                 .setId("amazon")
                                 .setRegistryName(ModRaidTypes.AMAZON_ID)
                                 .createRaidType();

        ModRaidTypes.barbarian = new RaidType.Builder()
                                .setId("barbarian")
                                .setRegistryName(ModRaidTypes.BARBARIAN_ID)
                                .createRaidType();

        ModRaidTypes.egyptian = new RaidType.Builder()
                                .setId("egyptian")
                                .setRegistryName(ModRaidTypes.EGYPTIAN_ID)
                                .createRaidType();

        ModRaidTypes.norsemen = new RaidType.Builder()
                                .setId("norsemen")
                                .setRegistryName(ModRaidTypes.NORSEMEN_ID)
                                .createRaidType();

        ModRaidTypes.pirate = new RaidType.Builder()
                                .setId("pirate")
                                .setRegistryName(ModRaidTypes.PIRATE_ID)
                                .createRaidType();

        reg.register(ModRaidTypes.amazon);
        reg.register(ModRaidTypes.barbarian);
        reg.register(ModRaidTypes.egyptian);
        reg.register(ModRaidTypes.norsemen);
        reg.register(ModRaidTypes.pirate);
    }
}
