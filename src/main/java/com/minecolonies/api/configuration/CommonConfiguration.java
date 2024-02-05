package com.minecolonies.api.configuration;

import com.ldtteam.common.config.AbstractConfiguration;
import com.minecolonies.api.util.constant.Constants;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class CommonConfiguration extends AbstractConfiguration
{
    public final BooleanValue generateSupplyLoot;
    public final BooleanValue rsEnableDebugLogging;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    public CommonConfiguration(final Builder builder)
    {
        super(builder, Constants.MOD_ID);

        createCategory("gameplay");
        generateSupplyLoot = defineBoolean("generatesupplyloot", true);

        swapToCategory("requestsystem");

        rsEnableDebugLogging = defineBoolean("enabledebuglogging", false);
        finishCategory();
    }
}
