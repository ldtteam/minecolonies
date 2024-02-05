package com.minecolonies.api.configuration;

import com.ldtteam.common.config.AbstractConfiguration;
import com.minecolonies.api.util.constant.Constants;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/**
 * Mod client configuration. Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    public final BooleanValue citizenVoices;
    public final BooleanValue neighborbuildingrendering;
    public final IntValue neighborbuildingrange;
    public final IntValue buildgogglerange;
    public final BooleanValue colonyteamborders;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    public ClientConfiguration(final Builder builder)
    {
        super(builder, Constants.MOD_ID);

        createCategory("gameplay");
        citizenVoices = defineBoolean("enablecitizenvoices", true);
        neighborbuildingrendering = defineBoolean("neighborbuildingrendering", true);
        neighborbuildingrange = defineInteger("neighborbuildingrange", 4, -2, 16);
        buildgogglerange = defineInteger("buildgogglerange", 50, 1, 250);
        colonyteamborders = defineBoolean("colonyteamborders", true);

        swapToCategory("pathfinding");

        finishCategory();
    }
}
