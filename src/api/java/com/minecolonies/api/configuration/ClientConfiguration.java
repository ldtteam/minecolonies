package com.minecolonies.api.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod client configuration. Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    public final ForgeConfigSpec.BooleanValue citizenVoices;
    public final ForgeConfigSpec.BooleanValue pathfindingDebugDraw;
    public final ForgeConfigSpec.IntValue     pathfindingDebugVerbosity;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");
        citizenVoices = defineBoolean(builder, "citizenvoices", true);

        swapToCategory(builder, "pathfinding");
        pathfindingDebugDraw = defineBoolean(builder, "pathfindingdebugdraw", false);
        pathfindingDebugVerbosity = defineInteger(builder, "pathfindingdebugverbosity", 0, 0, 10);

        finishCategory(builder);
    }
}
