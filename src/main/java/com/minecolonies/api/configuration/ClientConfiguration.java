package com.minecolonies.api.configuration;

import net.neoforged.neoforge.common.NeoForgeConfigSpec;

/**
 * Mod client configuration. Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    public final NeoForgeConfigSpec.BooleanValue citizenVoices;
    public final NeoForgeConfigSpec.BooleanValue neighborbuildingrendering;
    public final NeoForgeConfigSpec.IntValue neighborbuildingrange;
    public final NeoForgeConfigSpec.IntValue buildgogglerange;
    public final NeoForgeConfigSpec.BooleanValue colonyteamborders;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final NeoForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");
        citizenVoices = defineBoolean(builder, "enablecitizenvoices", true);
        neighborbuildingrendering = defineBoolean(builder, "neighborbuildingrendering", true);
        neighborbuildingrange = defineInteger(builder, "neighborbuildingrange", 4, -2, 16);
        buildgogglerange = defineInteger(builder, "buildgogglerange", 50, 1, 250);
        colonyteamborders = defineBoolean(builder, "colonyteamborders", true);

        swapToCategory(builder, "pathfinding");

        finishCategory(builder);
    }
}
