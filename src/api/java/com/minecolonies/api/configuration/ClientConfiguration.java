package com.minecolonies.api.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod client configuration. Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    public final ForgeConfigSpec.BooleanValue citizenVoices;
    public final ForgeConfigSpec.BooleanValue pathfindingDebugDraw;
    public final ForgeConfigSpec.BooleanValue neighborbuildingrendering;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");
        citizenVoices = defineBoolean(builder, "disablecitizenvoices", true);
        neighborbuildingrendering = defineBoolean(builder, "blueprintrender", true);

        swapToCategory(builder, "pathfinding");
        pathfindingDebugDraw = defineBoolean(builder, "pathfindingdebugdraw", false);

        finishCategory(builder);
    }
}
