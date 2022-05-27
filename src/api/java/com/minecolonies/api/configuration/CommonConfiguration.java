package com.minecolonies.api.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfiguration extends AbstractConfiguration
{
    public final ForgeConfigSpec.BooleanValue generateSupplyLoot;

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
    }
}
