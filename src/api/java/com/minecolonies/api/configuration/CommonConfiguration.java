package com.minecolonies.api.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration extends AbstractConfiguration
{
    public final ForgeConfigSpec.BooleanValue generateSupplyLoot;
    public final ForgeConfigSpec.BooleanValue rsEnableDebugLogging;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");
        generateSupplyLoot = defineBoolean(builder, "generatesupplyloot", true);
        finishCategory(builder);

        createCategory(builder, "requestsystem");
        rsEnableDebugLogging = defineBoolean(builder, "enabledebuglogging", false);
        finishCategory(builder);
    }
}
