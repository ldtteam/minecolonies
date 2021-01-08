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
    public static final String TAG_BRANCH         = "branch";
    public static final String TAG_NAME           = "name";
    public static final String TAG_COSTS          = "cost";
    public static final String TAG_COST_ITEM      = "cost-item";
    public static final String TAG_REQS           = "requirements";
    public static final String TAG_REQ_ITEM       = "requirement-item";
    public static final String TAG_EFFECTS        = "effects";
    public static final String TAG_EFFECT_ITEM    = "effect-item";
    public static final String TAG_RESEARCH_LVL   = "depth";
    public static final String TAG_RESEARCH_SORT  = "sort";
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
    public static final int    RESEARCH_WIDTH       = 175;
    public static final int    GRADIENT_WIDTH       = 175;
    public static final int    X_SPACING            = 40;
    public static final int    RESEARCH_HEIGHT      = 50;
    public static final int    GRADIENT_HEIGHT      = 50;
    public static final int    NAME_LABEL_WIDTH     = 175;
    public static final int    NAME_LABEL_HEIGHT    = 18;
    public static final int    Y_SPACING            = 20;
    public static final int    COST_OFFSET          = 20;
    public static final int    TIMELABEL_Y_POSITION = 10;
    public static final int    MAX_DEPTH            = 6;
    public static final int    INITIAL_X_OFFSET     = 10;
    public static final int    NAME_OFFSET          = 30;
    public static final int    INITIAL_Y_OFFSET     = 10;
    public static final int    TEXT_X_OFFSET        = 5;
    public static final int    ICON_X_OFFSET        = 3;
    public static final int    ICON_Y_OFFSET        = 4;
    public static final int    ICON_WIDTH           = 15;
    public static final int    ICON_HEIGHT          = 17;
    public static final int    TEXT_Y_OFFSET        = 6;
    public static final int    DEFAULT_COST_SIZE    = 16;
    public static final int    RESEARCH_ICON_WIDTH  = 30;
    public static final int    RESEARCH_ICON_HEIGHT = 32;
    public static final int    OR_X_OFFSET          = 14;
    public static final int    OR_Y_OFFSET          = 10;
    public static final int    OR_HEIGHT            = 16;
    public static final int    OR_WIDTH             = 16;
    public static final int    TIME_HEIGHT          = 12;
    public static final int    TIME_WIDTH           = 95;

    public static final boolean DRAW_ICONS = false;

    public static final int    COLOR_TEXT_NEGATIVE  = Color.rgbaToInt(218, 10, 10, 255);
    public static final int    COLOR_TEXT_LABEL     = Color.rgbaToInt(218, 202, 171, 255);
    public static final int    COLOR_TEXT_DARK      = Color.rgbaToInt(60, 60, 60, 255);

    /**
     * Research Effect Name strings, used for ResearchEffectManager and ResearchRegistry lookups.
     */
    //Addition Multipliers
    public static final String ARCHER_DAMAGE                = "minecolonies:effects/archerdamageaddition";
    public static final String CITIZEN_INV_SLOTS            = "minecolonies:effects/citizeninvslotsaddition";
    public static final String CITIZEN_CAP                  = "minecolonies:effects/citizencapaddition";
    public static final String MECHANIC_ENHANCED_GATES      = "minecolonies:effects/enhancesgatedurabilityaddition";
    public static final String FLEEING_SPEED                = "minecolonies:effects/fleeingspeedaddition";
    public static final String SATLIMIT                     = "minecolonies:effects/healingsaturationlimitaddition";
    public static final String HEALTH_BOOST                 = "minecolonies:effects/healthaddition";
    public static final String MELEE_DAMAGE                 = "minecolonies:effects/meleedamageaddition";
    public static final String WORK_LONGER                  = "minecolonies:effects/workingdayhaddition";

    //Multiplier Modifiers
    public static final String ARCHER_ARMOR      = "minecolonies:effects/archerarmormultiplier";
    public static final String ARMOR_DURABILITY  = "minecolonies:effects/armordurabilitymultiplier";
    public static final String BLOCK_ATTACKS     = "minecolonies:effects/blockattacksmultiplier";
    public static final String BLOCK_BREAK_SPEED = "minecolonies:effects/blockbreakspeedmultiplier";
    public static final String BLOCK_PLACE_SPEED = "minecolonies:effects/blockplacespeedmultiplier";
    public static final String DOUBLE_ARROWS     = "minecolonies:effects/doublearrowsmultiplier";
    public static final String FARMING           = "minecolonies:effects/farmingmultiplier";
    public static final String FLEEING_DAMAGE    = "minecolonies:effects/fleeingdamagemultiplier";
    public static final String GROWTH            = "minecolonies:effects/growthmultiplier";
    public static final String HAPPINESS         = "minecolonies:effects/happinessmultiplier";
    public static final String LEVELING          = "minecolonies:effects/levelingmultiplier";
    public static final String MELEE_ARMOR       = "minecolonies:effects/meleearmormultiplier";
    public static final String MINIMUM_STOCK     = "minecolonies:effects/minimumstockmultiplier";
    public static final String MORE_ORES         = "minecolonies:effects/moreoresmultiplier";
    public static final String RECIPES           = "minecolonies:effects/recipesmultiplier";
    public static final String REGENERATION      = "minecolonies:effects/regenerationmultiplier";
    public static final String SATURATION        = "minecolonies:effects/saturationmultiplier";
    public static final String SLEEP_LESS        = "minecolonies:effects/sleeplessmultiplier";
    public static final String TEACHING          = "minecolonies:effects/teachingmultiplier";
    public static final String TOOL_DURABILITY   = "minecolonies:effects/tooldurabilitymultiplier";
    public static final String WALKING           = "minecolonies:effects/walkingmultiplier";

    //Unlock Ability modifiers.
    public static final String CRUSHING_11       = "minecolonies:effects/crushing11unlock";
    public static final String ARCHER_USE_ARROWS = "minecolonies:effects/consumearrowsunlock";
    public static final String KNIGHT_TAUNT      = "minecolonies:effects/knighttauntmobsunlock";
    public static final String FIRE_RES          = "minecolonies:effects/minerfireresunlock";
    public static final String ARROW_PIERCE      = "minecolonies:effects/piercingarrowsunlock";
    public static final String PLANT_2           = "minecolonies:effects/plant2unlock";
    public static final String RAILS             = "minecolonies:effects/railsunlock";
    public static final String RETREAT           = "minecolonies:effects/retreatunlock";
    public static final String SHIELD_USAGE      = "minecolonies:effects/shieldusageunlock";
    public static final String KNIGHT_WHIRLWIND  = "minecolonies:effects/whirlwindabilityunlock";
    public static final String WORKING_IN_RAIN   = "minecolonies:effects/workinginrainunlock";

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
