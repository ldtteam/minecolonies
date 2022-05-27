package com.minecolonies.api.configuration;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
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
        final Pair<ClientConfiguration, ForgeConfigSpec> cli = new ForgeConfigSpec.Builder().configure(ClientConfiguration::new);
        final Pair<ServerConfiguration, ForgeConfigSpec> ser = new ForgeConfigSpec.Builder().configure(ServerConfiguration::new);
        final Pair<CommonConfiguration, ForgeConfigSpec> com = new ForgeConfigSpec.Builder().configure(CommonConfiguration::new);

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
