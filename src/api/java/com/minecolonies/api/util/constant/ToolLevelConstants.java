package com.minecolonies.api.util.constant;

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
    public static final int TOOL_LEVEL_HAND                = -1;
    /**
     * Tool level for gold or wood.
     */
    @NonNls
    public static final int TOOL_LEVEL_WOOD_OR_GOLD        = 0;
    /**
     * Tool level for stone.
     */
    @NonNls
    public static final int TOOL_LEVEL_STONE               = 1;
    /**
     * Tool level for iron.
     */
    @NonNls
    public static final int TOOL_LEVEL_IRON                = 2;
    /**
     * Tool level for diamond.
     */
    @NonNls
    public static final int TOOL_LEVEL_DIAMOND             = 3;
    /**
     * Tool level for better than diamond.
     */
    @NonNls
    public static final int TOOL_LEVEL_BETTER_THAN_DIAMOND = 4;

    /**
     * Maximum durability before an item is likely not made of wood or gold.  Ie iron, diamond, modded materials
     * Significantly higher than vanilla defaults (<70) to handle Anvil Repair or Tinker/Tetra situations.
     */
    @NonNls
    public static final int    DURABILITY_MAX_WOOD_OR_GOLD      = 120;

    /**
     * Maximum durability before an item is likely not made of wood, gold, or iron. ie diamond, modded materials
     * Significantly higher than vanilla defaults (<140) to handle Anvil Repair or Tinker/Tetra situations.
     */
    @NonNls
    public static final int    DURABILITY_MAX_IRON              = 400;

    /**
     * Armor level for leather.
     */
    @NonNls
    public static final int ARMOR_LEVEL_LEATHER = 0;

    /**
     * Armor level for gold.
     */
    @NonNls
    public static final int ARMOR_LEVEL_GOLD = 1;

    /**
     * Armor level for chain.
     */
    @NonNls
    public static final int ARMOR_LEVEL_CHAIN = 2;

    /**
     * Armor level for iron.
     */
    @NonNls
    public static final int ARMOR_LEVEL_IRON = 3;

    /**
     * Armor level for diamond.
     */
    @NonNls
    public static final int ARMOR_LEVEL_DIAMOND = 4;

    /**
     * Armor level for diamond.
     */
    @NonNls
    public static final int ARMOR_LEVEL_MAX = 100;

    /**
     * Tool level for maximum.
     */
    @NonNls
    public static final int TOOL_LEVEL_MAXIMUM = Integer.SIZE;

    private ToolLevelConstants()
    {
        //empty default
    }
}

