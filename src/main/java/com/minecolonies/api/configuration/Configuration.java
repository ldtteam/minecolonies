package com.minecolonies.api.configuration;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod root configuration.
 */
public class Configuration
{
    /**
     * Loaded clientside, not synced
     */
    private final ClientConfiguration clientConfig;

    /**
     * Loaded serverside, synced on connection
     */
    private final ServerConfiguration serverConfig;

    /**
     * Loaded serverside, synced on connection
     */
    private final CommonConfiguration commonConfiguration;

    /**
     * Builds configuration tree.
     */
    public Configuration()
    {
        final Pair<ClientConfiguration, NeoForgeConfigSpec> cli = new NeoForgeConfigSpec.Builder().configure(ClientConfiguration::new);
        final Pair<ServerConfiguration, NeoForgeConfigSpec> ser = new NeoForgeConfigSpec.Builder().configure(ServerConfiguration::new);
        final Pair<CommonConfiguration, NeoForgeConfigSpec> com = new NeoForgeConfigSpec.Builder().configure(CommonConfiguration::new);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, cli.getRight());
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ser.getRight());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, com.getRight());

        clientConfig = cli.getLeft();
        serverConfig = ser.getLeft();
        commonConfiguration = com.getLeft();
    }

    public ClientConfiguration getClient()
    {
        return clientConfig;
    }

    public ServerConfiguration getServer()
    {
        return serverConfig;
    }

    public CommonConfiguration getCommon()
    {
        return commonConfiguration;
    }
}
