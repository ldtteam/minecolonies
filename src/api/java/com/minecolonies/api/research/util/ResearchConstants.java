package com.minecolonies.api.research.util;

/**
 * Class for research constants.
 */
public final class ResearchConstants
{
    /**
     * The research tree tag.
     */
    public static final String TAG_RESEARCH_TREE = "researchTree";

    /**
     * Base research time, default to 3h playtime.
     */
    public static final int BASE_RESEARCH_TIME = 60 * 60 / 25;

    /**
     * Constants to write the research to NBT.
     */
    public static final String TAG_PARENT     = "parent";
    public static final String TAG_STATE      = "state";
    public static final String TAG_ID         = "id";
    public static final String TAG_BRANCH     = "branch";
    public static final String TAG_DESC       = "desc";
    public static final String TAG_EFFECT     = "effect";
    public static final String TAG_DEPTH      = "depth";
    public static final String TAG_PROGRESS   = "progress";
    public static final String TAG_CHILDS     = "hasResearchedChild";
    public static final String TAG_ONLY_CHILD     = "onlyChild";
    public static final String TAG_RESEARCH_CHILD = "child";

    /**
     * Research constants for window.
     */
    public static final String DRAG_VIEW_ID = "dragView";
    public static final int GRADIENT_WIDTH  = 175;
    public static final int X_SPACING       = 40;
    public static final int GRADIENT_HEIGHT = 50;
    public static final int Y_SPACING   = 20;
    public static final int COST_OFFSET          = 20;
    public static final int TIMELABEL_Y_POSITION = 10;
    public static final int MAX_DEPTH        = 6;
    public static final int INITIAL_X_OFFSET = 10;
    public static final int NAME_OFFSET      = 50;
    public static final int INITIAL_Y_OFFSET = 10;
    public static final int TEXT_X_OFFSET  = 5;
    public static final int XPBAR_Y_OFFSET = 30;
    public static final int XPBAR_LENGTH  = 90;
    public static final int TEXT_Y_OFFSET     = 10;
    public static final int DEFAULT_COST_SIZE = 16;
    public static final int LOCK_WIDTH        = 15;
    public static final int LOCK_HEIGHT = 17;
    public static final int OR_X_OFFSET = 14;
    public static final int OR_Y_OFFSET = 10;

    /**
     * Research effects.
     */
    public static final String ARCHER_ARMOR = "Archer Armour";
    public static final String MELEE_ARMOR = "Melee Armour";
    public static final String ARMOR_DURABILITY = "Armor Durability";
    public static final String FLEEING_DAMAGE = "Fleeing Damage";
    public static final String FLEEING_SPEED = "Fleeing Speed";
    public static final String MELEE_DAMAGE = "Melee Damage";
    public static final String ARCHER_DAMAGE = "Archer Damage";
    public static final String RETREAT       = "Retreat";
    public static final String NONE          = "None";
    public static final String SHIELD_USAGE  = "Shield Usage";
    public static final String DOUBLE_ARROWS = "Double Arrows";
    public static final String BLOCK_ATTACKS = "Block Attacks";
    public static final String SLEEP_LESS    = "Sleep Less";

    public static final String TEACHING = "Teaching";
    public static final String GROWTH = "Growth";
    public static final String CAP = "Citizen-Cap";
    public static final String RAILS = "Rails";
    public static final String WALKING = "Walking";

    public static final String HEALTH = "Health";
    public static final String REGENERATION = "Regeneration";
    public static final String SATLIMIT = "Healing Saturation Limit";
    public static final String HAPPINESS = "Happiness";
    public static final String SATURATION = "Saturation";

    public static final String FARMING = "Farming";
    public static final String BLOCK_BREAK_SPEED = "Block Break Speed";
    public static final String MINIMUM_STOCK = "Minimum Stock";
    public static final String RECIPES = "Recipes";
    public static final String BLOCK_PLACE_SPEED = "Block Place Speed";
    public static final String MORE_ORES = "More Ores";
    public static final String WORKING_IN_RAIN = "Working in Rain";
    public static final String TOOL_DURABILITY = "Tool Durability";
    public static final String INV_SLOTS       = "Citizen Inv Slots";

    /**
     * Private constructor to hide implicit public one.
     */
    private ResearchConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
