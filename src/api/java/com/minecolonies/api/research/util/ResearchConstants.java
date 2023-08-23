package com.minecolonies.api.research.util;

import com.ldtteam.blockui.Color;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;

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
    public static final String TAG_COST_NBT       = "cost-nbt";
    public static final String TAG_REQS           = "requirements";
    public static final String TAG_REQ_TYPE       = "requirement-type";
    public static final String TAG_REQ_ITEM       = "requirement-item";
    public static final String TAG_EFFECTS        = "effects";
    public static final String TAG_EFFECT_TYPE    = "effect-type";
    public static final String TAG_EFFECT_ITEM    = "effect-item";
    public static final String TAG_RESEARCH_LVL   = "depth";
    public static final String TAG_RESEARCH_SORT  = "sort";
    public static final String TAG_PROGRESS       = "progress";
    public static final String TAG_CHILDS         = "hasResearchedChild";
    public static final String TAG_ONLY_CHILD     = "onlyChild";
    public static final String TAG_ICON_TEXTURE   = "icon_tex";
    public static final String TAG_ICON_ITEM_STACK= "icon_is";
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

    public static final int    COLOR_TEXT_NEGATIVE   = Color.rgbaToInt(218, 10, 10, 255);
    public static final int    COLOR_TEXT_LABEL      = Color.rgbaToInt(218, 202, 171, 255);
    public static final int    COLOR_TEXT_DARK       = Color.rgbaToInt(60, 60, 60, 255);
    public static final int    COLOR_TEXT_NAME       = Color.rgbaToInt(255,170,0, 255);
    public static final int    COLOR_TEXT_UNFULFILLED= Color.rgbaToInt(240,150,135,255);
    public static final int    COLOR_TEXT_FULFILLED  = Color.rgbaToInt(85,255,255,255);

    /**
     * Research Effect Name strings, used for ResearchEffectManager and ResearchRegistry lookups.
     */
    //Addition Multipliers
    public static final ResourceLocation ARCHER_DAMAGE                = new ResourceLocation(Constants.MOD_ID, "effects/archerdamageaddition");
    public static final ResourceLocation CITIZEN_INV_SLOTS            = new ResourceLocation(Constants.MOD_ID, "effects/citizeninvslotsaddition");
    public static final ResourceLocation CITIZEN_CAP                  = new ResourceLocation(Constants.MOD_ID, "effects/citizencapaddition");
    public static final ResourceLocation MECHANIC_ENHANCED_GATES      = new ResourceLocation(Constants.MOD_ID, "effects/enhancesgatedurabilityaddition");
    public static final ResourceLocation FLEEING_SPEED                = new ResourceLocation(Constants.MOD_ID, "effects/fleeingspeedaddition");
    public static final ResourceLocation SATLIMIT                     = new ResourceLocation(Constants.MOD_ID, "effects/healingsaturationlimitaddition");
    public static final ResourceLocation HEALTH_BOOST                 = new ResourceLocation(Constants.MOD_ID, "effects/healthaddition");
    public static final ResourceLocation MELEE_DAMAGE                 = new ResourceLocation(Constants.MOD_ID, "effects/meleedamageaddition");
    public static final ResourceLocation WORK_LONGER                  = new ResourceLocation(Constants.MOD_ID, "effects/workingdayhaddition");
    public static final ResourceLocation RESURRECT_CHANCE             = new ResourceLocation(Constants.MOD_ID, "effects/resurrectchanceaddition");
    public static final ResourceLocation GRAVE_DECAY_BONUS            = new ResourceLocation(Constants.MOD_ID, "effects/gravedecaybonus");
    public static final ResourceLocation UNDERTAKER_RUN               = new ResourceLocation(Constants.MOD_ID, "effects/undertakerrun");

    //Multiplier Modifiers
    public static final ResourceLocation ARCHER_ARMOR      = new ResourceLocation(Constants.MOD_ID, "effects/archerarmormultiplier");
    public static final ResourceLocation ARMOR_DURABILITY  = new ResourceLocation(Constants.MOD_ID, "effects/armordurabilitymultiplier");
    public static final ResourceLocation BLOCK_ATTACKS     = new ResourceLocation(Constants.MOD_ID, "effects/blockattacksmultiplier");
    public static final ResourceLocation BLOCK_BREAK_SPEED = new ResourceLocation(Constants.MOD_ID, "effects/blockbreakspeedmultiplier");
    public static final ResourceLocation BLOCK_PLACE_SPEED = new ResourceLocation(Constants.MOD_ID, "effects/blockplacespeedmultiplier");
    public static final ResourceLocation DOUBLE_ARROWS     = new ResourceLocation(Constants.MOD_ID, "effects/doublearrowsmultiplier");
    public static final ResourceLocation FARMING           = new ResourceLocation(Constants.MOD_ID, "effects/farmingmultiplier");
    public static final ResourceLocation FLEEING_DAMAGE    = new ResourceLocation(Constants.MOD_ID, "effects/fleeingdamagemultiplier");
    public static final ResourceLocation GROWTH            = new ResourceLocation(Constants.MOD_ID, "effects/growthmultiplier");
    public static final ResourceLocation HAPPINESS         = new ResourceLocation(Constants.MOD_ID, "effects/happinessmultiplier");
    public static final ResourceLocation LEVELING          = new ResourceLocation(Constants.MOD_ID, "effects/levelingmultiplier");
    public static final ResourceLocation MELEE_ARMOR       = new ResourceLocation(Constants.MOD_ID, "effects/meleearmormultiplier");
    public static final ResourceLocation MINIMUM_STOCK     = new ResourceLocation(Constants.MOD_ID, "effects/minimumstockmultiplier");
    public static final ResourceLocation MORE_ORES         = new ResourceLocation(Constants.MOD_ID, "effects/moreoresmultiplier");
    public static final ResourceLocation PODZOL_CHANCE     = new ResourceLocation(Constants.MOD_ID, "effects/podzolchancemultiplier");
    public static final ResourceLocation RECIPES           = new ResourceLocation(Constants.MOD_ID, "effects/recipesmultiplier");
    public static final ResourceLocation REGENERATION      = new ResourceLocation(Constants.MOD_ID, "effects/regenerationmultiplier");
    public static final ResourceLocation SATURATION        = new ResourceLocation(Constants.MOD_ID, "effects/saturationmultiplier");
    public static final ResourceLocation SLEEP_LESS        = new ResourceLocation(Constants.MOD_ID, "effects/sleeplessmultiplier");
    public static final ResourceLocation TEACHING          = new ResourceLocation(Constants.MOD_ID, "effects/teachingmultiplier");
    public static final ResourceLocation TOOL_DURABILITY   = new ResourceLocation(Constants.MOD_ID, "effects/tooldurabilitymultiplier");
    public static final ResourceLocation WALKING           = new ResourceLocation(Constants.MOD_ID, "effects/walkingmultiplier");

    //Unlock Ability modifiers.
    public static final ResourceLocation CRUSHING_11       = new ResourceLocation(Constants.MOD_ID, "effects/crushing11unlock");
    public static final ResourceLocation CRUSHING_ADV      = new ResourceLocation(Constants.MOD_ID, "effects/crushingadvancedunlock");
    public static final ResourceLocation ARCHER_USE_ARROWS = new ResourceLocation(Constants.MOD_ID, "effects/consumearrowsunlock");
    public static final ResourceLocation KNIGHT_TAUNT      = new ResourceLocation(Constants.MOD_ID, "effects/knighttauntmobsunlock");
    public static final ResourceLocation FIRE_RES          = new ResourceLocation(Constants.MOD_ID, "effects/minerfireresunlock");
    public static final ResourceLocation ARROW_PIERCE      = new ResourceLocation(Constants.MOD_ID, "effects/piercingarrowsunlock");
    public static final ResourceLocation PLANTATION_LARGE  = new ResourceLocation(Constants.MOD_ID, "effects/plantationlarge");
    public static final ResourceLocation PLANTATION_JUNGLE = new ResourceLocation(Constants.MOD_ID, "effects/plantationjungle");
    public static final ResourceLocation PLANTATION_SEA    = new ResourceLocation(Constants.MOD_ID, "effects/plantationsea");
    public static final ResourceLocation PLANTATION_EXOTIC = new ResourceLocation(Constants.MOD_ID, "effects/plantationexotic");
    public static final ResourceLocation PLANTATION_NETHER = new ResourceLocation(Constants.MOD_ID, "effects/plantationnether");
    public static final ResourceLocation BEEKEEP_2         = new ResourceLocation(Constants.MOD_ID, "effects/beekeep2unlock");
    public static final ResourceLocation PLATE_ARMOR       = new ResourceLocation(Constants.MOD_ID, "effects/platearmorunlock");
    public static final ResourceLocation RAILS             = new ResourceLocation(Constants.MOD_ID, "effects/railsunlock");
    public static final ResourceLocation VINES             = new ResourceLocation(Constants.MOD_ID, "effects/vinesunlock");
    public static final ResourceLocation RETREAT           = new ResourceLocation(Constants.MOD_ID, "effects/retreatunlock");
    public static final ResourceLocation SHIELD_USAGE      = new ResourceLocation(Constants.MOD_ID, "effects/shieldusageunlock");
    public static final ResourceLocation KNIGHT_WHIRLWIND  = new ResourceLocation(Constants.MOD_ID, "effects/whirlwindabilityunlock");
    public static final ResourceLocation WORKING_IN_RAIN   = new ResourceLocation(Constants.MOD_ID, "effects/workinginrainunlock");
    public static final ResourceLocation USE_TOTEM         = new ResourceLocation(Constants.MOD_ID, "effects/usetotemunlock");
    public static final ResourceLocation RECIPE_MODE       = new ResourceLocation(Constants.MOD_ID, "effects/recipemodeunlock");
    public static final ResourceLocation BUILDER_MODE      = new ResourceLocation(Constants.MOD_ID, "effects/buildermodeunlock");
    public static final ResourceLocation DRUID_USE_POTIONS = new ResourceLocation(Constants.MOD_ID, "effects/consumepotions");
    public static final ResourceLocation SOFT_SHOES        = new ResourceLocation(Constants.MOD_ID, "effects/softshoesunlock");
    public static final ResourceLocation FISH_TREASURE     = new ResourceLocation(Constants.MOD_ID, "effects/fishingtreasure");
    public static final ResourceLocation NETHER_LOG        = new ResourceLocation(Constants.MOD_ID, "effects/netherexpeditionlog");
    public static final ResourceLocation MASKS             = new ResourceLocation(Constants.MOD_ID, "effects/masks");
    public static final ResourceLocation VACCINES          = new ResourceLocation(Constants.MOD_ID, "effects/vaccines");
    public static final ResourceLocation TELESCOPE          = new ResourceLocation(Constants.MOD_ID, "effects/telescope");
    public static final ResourceLocation STANDARD          = new ResourceLocation(Constants.MOD_ID, "effects/standard");
    public static final ResourceLocation MORE_AIR          = new ResourceLocation(Constants.MOD_ID, "effects/air");
    public static final ResourceLocation MIN_ORDER         = new ResourceLocation(Constants.MOD_ID, "effects/min_order");

    //Recipe unlocks
    public static final ResourceLocation THE_END           = new ResourceLocation(Constants.MOD_ID, "effects/knowledgeoftheendunlock");
    public static final ResourceLocation THE_DEPTHS        = new ResourceLocation(Constants.MOD_ID, "effects/knowledgeofthedepthsunlock");
    public static final ResourceLocation MORE_SCROLLS      = new ResourceLocation(Constants.MOD_ID, "effects/morescrollsunlock");
    public static final ResourceLocation SIFTER_STRING     = new ResourceLocation(Constants.MOD_ID, "effects/sifterstringunlock");
    public static final ResourceLocation SIFTER_FLINT      = new ResourceLocation(Constants.MOD_ID, "effects/sifterflintunlock");
    public static final ResourceLocation SIFTER_IRON       = new ResourceLocation(Constants.MOD_ID, "effects/sifterironunlock");
    public static final ResourceLocation SIFTER_DIAMOND    = new ResourceLocation(Constants.MOD_ID, "effects/sifterdiamondunlock");

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
