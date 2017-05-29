package com.minecolonies.coremod.util.constants;

import org.jetbrains.annotations.NonNls;

/**
 * Constants for tool levels.
 */
public final class ToolLevelConstants
{
    /**
     * Tool level for hand.
     */
    @NonNls
    public static final int TOOL_LEVEL_HAND  = -1;
    /**
     * Tool level for gold or wood.
     */
    @NonNls
    public static final int TOOL_LEVEL_WOOD_OR_GOLD  = 0;
    /**
     * Tool level for stone.
     */
    @NonNls
    public static final int TOOL_LEVEL_STONE = 1;
    /**
     * Tool level for iron.
     */
    @NonNls
    public static final int TOOL_LEVEL_IRON = 2;
    /**
     * Tool level for diamond.
     */
    @NonNls
    public static final int TOOL_LEVEL_DIAMOND = 3;
    /**
     * Tool level for better than diamond.
     */
    @NonNls
    public static final int TOOL_LEVEL_BETTER_THAN_DIAMOND = 4;

    private ToolLevelConstants()
    {
        //empty default
    }
}

