package com.minecolonies.api.research.util;

import com.ldtteam.blockout.Color;

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
    public static final String TAG_PARENT         = "parent";
    public static final String TAG_STATE          = "state";
    public static final String TAG_ID             = "id";
    public static final String TAG_RESOURCE_PATH  = "resource-path";
    public static final String TAG_BRANCH         = "branch";
    public static final String TAG_DESC           = "desc";
    public static final String TAG_COSTS          = "cost";
    public static final String TAG_COST_ITEM      = "cost-item";
    public static final String TAG_REQS           = "requirements";
    public static final String TAG_REQ_ITEM       = "requirement-item";
    public static final String TAG_EFFECTS        = "effects";
    public static final String TAG_EFFECT_ITEM    = "effect-item";
    public static final String TAG_DEPTH          = "depth";
    public static final String TAG_PROGRESS       = "progress";
    public static final String TAG_CHILDS         = "hasResearchedChild";
    public static final String TAG_ONLY_CHILD     = "onlyChild";
    public static final String TAG_ICON           = "icon";
    public static final String TAG_SUBTITLE_NAME  = "subtitle";
    public static final String TAG_INSTANT        = "instant";
    public static final String TAG_AUTOSTART      = "autostart";
    public static final String TAG_IMMUTABLE      = "immutable";
    public static final String TAG_HIDDEN         = "hidden";
    public static final String TAG_RESEARCH_CHILD = "child";

    /**
     * Research constants for window.
     */
    public static final String DRAG_VIEW_ID         = "dragView";
    public static final int    GRADIENT_WIDTH       = 140;
    public static final int    X_SPACING            = 40;
    public static final int    GRADIENT_HEIGHT      = 40;
    public static final int    Y_SPACING            = 20;
    public static final int    COST_OFFSET          = 20;
    public static final int    TIMELABEL_Y_POSITION = 10;
    public static final int    MAX_DEPTH            = 6;
    public static final int    INITIAL_X_OFFSET     = 10;
    public static final int    NAME_OFFSET          = 30;
    public static final int    INITIAL_Y_OFFSET     = 10;
    public static final int    TEXT_X_OFFSET        = 5;
    public static final int    ICON_X_OFFSET        = 4;
    public static final int    ICON_Y_OFFSET        = 4;
    public static final int    XPBAR_Y_OFFSET       = 30;
    public static final int    XPBAR_LENGTH         = 90;
    public static final int    TEXT_Y_OFFSET        = 10;
    public static final int    DEFAULT_COST_SIZE    = 16;
    public static final int    LOCK_WIDTH           = 15;
    public static final int    LOCK_HEIGHT          = 17;
    public static final int    OR_X_OFFSET          = 14;
    public static final int    OR_Y_OFFSET          = 10;
    public static final int    OR_HEIGHT            = 16;
    public static final int    OR_WIDTH             = 16;
    public static final int    TIME_HEIGHT          = 5;
    public static final int    TIME_WIDTH           = 95;

    public static final int    COLOR_TEXT_NEGATIVE  = Color.rgbaToInt(218, 10, 10, 255);
    public static final int    COLOR_TEXT_LABEL     = Color.rgbaToInt(218, 202, 171, 255);
    public static final int    COLOR_TEXT_DARK      = Color.rgbaToInt(60, 60, 60, 255);

    /**
     * Research Effect Name strings, used for ResearchEffectManager and ResearchRegistry lookups.
     */
    //Addition Multipliers
    public static final String ARCHER_DAMAGE                = "archerdamageaddition";
    public static final String CITIZEN_INV_SLOTS                    = "citizeninvslotsaddition";
    public static final String CITIZEN_CAP                  = "citizencapaddition";
    public static final String MECHANIC_ENHANCED_GATES      = "enhancesgatedurabilityaddition";
    public static final String FLEEING_SPEED                = "fleeingspeedaddition";
    public static final String SATLIMIT                     = "healingsaturationlimitaddition";
    public static final String HEALTH_BOOST                 = "healthaddition";
    public static final String MELEE_DAMAGE                 = "meleedamageaddition";
    public static final String WORK_LONGER                  = "workingdayhaddition";

    //Multiplier Modifiers
    public static final String ARCHER_ARMOR      = "archerarmormultiplier";
    public static final String ARMOR_DURABILITY  = "armordurabilitymultiplier";
    public static final String BLOCK_ATTACKS     = "blockattacksmultiplier";
    public static final String BLOCK_BREAK_SPEED = "blockbreakspeedmultiplier";
    public static final String BLOCK_PLACE_SPEED = "blockplacespeedmultiplier";
    public static final String DOUBLE_ARROWS     = "doublearrowsmultiplier";
    public static final String FARMING           = "farmingmultiplier";
    public static final String FLEEING_DAMAGE    = "fleeingdamagemultiplier";
    public static final String GROWTH            = "growthmultiplier";
    public static final String HAPPINESS         = "happinessmultiplier";
    public static final String LEVELING          = "levelingmultiplier";
    public static final String MELEE_ARMOR       = "meleearmormultiplier";
    public static final String MINIMUM_STOCK     = "minimumstockmultiplier";
    public static final String MORE_ORES         = "moreoresmultiplier";
    public static final String RECIPES           = "recipesmultiplier";
    public static final String REGENERATION      = "regenerationmultiplier";
    public static final String SATURATION        = "saturationmultiplier";
    public static final String SLEEP_LESS        = "sleeplessmultiplier";
    public static final String TEACHING          = "teachingmultiplier";
    public static final String TOOL_DURABILITY   = "tooldurabilitymultiplier";
    public static final String WALKING           = "walkingmultiplier";

    //Unlock Ability modifiers.
    public static final String CRUSHING_11       = "crushing11unlock";
    public static final String ARCHER_USE_ARROWS = "consumearrowsunlock";
    public static final String KNIGHT_TAUNT      = "knighttauntmobsunlock";
    public static final String FIRE_RES          = "minerfireresunlock";
    public static final String ARROW_PIERCE      = "piercingarrowsunlock";
    public static final String PLANT_2           = "plant2unlock";
    public static final String RAILS             = "railsunlock";
    public static final String RETREAT           = "retreatunlock";
    public static final String SHIELD_USAGE      = "shieldusageunlock";
    public static final String KNIGHT_WHIRLWIND  = "whirlwindabilityunlock";
    public static final String WORKING_IN_RAIN   = "workinginrainunlock";

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
