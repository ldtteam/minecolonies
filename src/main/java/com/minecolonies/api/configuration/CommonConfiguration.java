package com.minecolonies.api.configuration;

import net.neoforged.neoforge.common.NeoForgeConfigSpec;

public class CommonConfiguration extends AbstractConfiguration
{
    public final NeoForgeConfigSpec.BooleanValue generateSupplyLoot;
    public final NeoForgeConfigSpec.BooleanValue rsEnableDebugLogging;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected CommonConfiguration(final NeoForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");
        generateSupplyLoot = defineBoolean(builder, "generatesupplyloot", true);
        finishCategory(builder);

        createCategory(builder, "requestsystem");
        rsEnableDebugLogging = defineBoolean(builder, "enabledebuglogging", false);
        finishCategory(builder);
    }
}
