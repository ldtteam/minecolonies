package com.minecolonies.api.research.util;

import com.ldtteam.blockui.Color;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.function.Predicate;

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
    public static final int BASE_RESEARCH_TIME = 60 * 60 / 25 / 2;

    /**
     * Constants to write the research to NBT.
     */
    public static final String TAG_PARENT         = "parent";
    public static final String TAG_STATE          = "state";
    public static final String TAG_ID             = "id";
    public static final String TAG_BRANCH         = "branch";
    public static final String TAG_NAME           = "name";
    public static final String TAG_COSTS          = "cost";
    public static final String TAG_COST_TYPE      = "cost-type";
    public static final String TAG_COST_ITEMS     = "cost-items";
    public static final String TAG_COST_ITEM      = "cost-item";
    public static final String TAG_COST_TAG       = "cost-tag";
    public static final String TAG_COST_NBT       = "cost-nbt";
    public static final String TAG_COST_COUNT     = "cost-count";
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
    public static final Identifier ARCHER_DAMAGE                = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/archerdamageaddition");
    public static final Identifier CITIZEN_INV_SLOTS            = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/citizeninvslotsaddition");
    public static final Identifier CITIZEN_CAP                  = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/citizencapaddition");
    public static final Identifier MECHANIC_ENHANCED_GATES      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/enhancesgatedurabilityaddition");
    public static final Identifier FLEEING_SPEED                = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/fleeingspeedaddition");
    public static final Identifier SATLIMIT                     = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/healingsaturationlimitaddition");
    public static final Identifier HEALTH_BOOST                 = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/healthaddition");
    public static final Identifier MELEE_DAMAGE                 = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/meleedamageaddition");
    public static final Identifier WORK_LONGER                  = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/workingdayhaddition");
    public static final Identifier RESURRECT_CHANCE             = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/resurrectchanceaddition");
    public static final Identifier GRAVE_DECAY_BONUS            = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/gravedecaybonus");
    public static final Identifier UNDERTAKER_RUN               = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/undertakerrun");

    //Multiplier Modifiers
    public static final Identifier ARCHER_ARMOR      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/archerarmormultiplier");
    public static final Identifier ARMOR_DURABILITY  = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/armordurabilitymultiplier");
    public static final Identifier BLOCK_ATTACKS     = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/blockattacksmultiplier");
    public static final Identifier BLOCK_BREAK_SPEED = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/blockbreakspeedmultiplier");
    public static final Identifier BLOCK_PLACE_SPEED = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/blockplacespeedmultiplier");
    public static final Identifier DOUBLE_ARROWS     = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/doublearrowsmultiplier");
    public static final Identifier FARMING           = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/farmingmultiplier");
    public static final Identifier FLEEING_DAMAGE    = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/fleeingdamagemultiplier");
    public static final Identifier GROWTH            = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/growthmultiplier");
    public static final Identifier HAPPINESS         = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/happinessmultiplier");
    public static final Identifier LEVELING          = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/levelingmultiplier");
    public static final Identifier MELEE_ARMOR       = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/meleearmormultiplier");
    public static final Identifier MINIMUM_STOCK     = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/minimumstockmultiplier");
    public static final Identifier MORE_ORES         = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/moreoresmultiplier");
    public static final Identifier PODZOL_CHANCE     = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/podzolchancemultiplier");
    public static final Identifier RECIPES           = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/recipesmultiplier");
    public static final Identifier REGENERATION      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/regenerationmultiplier");
    public static final Identifier SATURATION        = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/saturationmultiplier");
    public static final Identifier SLEEP_LESS        = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/sleeplessmultiplier");
    public static final Identifier TEACHING          = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/teachingmultiplier");
    public static final Identifier TOOL_DURABILITY   = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/tooldurabilitymultiplier");
    public static final Identifier WALKING           = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/walkingmultiplier");
    public static final Identifier LOOTING           = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/looting");

    //Unlock Ability modifiers.
    public static final Identifier CRUSHING_11       = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/crushing11unlock");
    public static final Identifier ARCHER_USE_ARROWS = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/consumearrowsunlock");
    public static final Identifier KNIGHT_TAUNT      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/knighttauntmobsunlock");
    public static final Identifier GUARD_CRIT = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/guardcrit");
    public static final Identifier FIRE_RES          = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/minerfireresunlock");
    public static final Identifier ARROW_PIERCE      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/piercingarrowsunlock");
    public static final Identifier PLANTATION_LARGE  = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/plantationlarge");
    public static final Identifier PLANTATION_JUNGLE = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/plantationjungle");
    public static final Identifier PLANTATION_SEA    = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/plantationsea");
    public static final Identifier PLANTATION_EXOTIC = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/plantationexotic");
    public static final Identifier PLANTATION_NETHER = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/plantationnether");
    public static final Identifier BEEKEEP_2         = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/beekeep2unlock");
    public static final Identifier PLATE_ARMOR       = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/platearmorunlock");
    public static final Identifier RAILS             = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/railsunlock");
    public static final Identifier VINES             = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/vinesunlock");
    public static final Identifier RETREAT           = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/retreatunlock");
    public static final Identifier SHIELD_USAGE      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/shieldusageunlock");
    public static final Identifier KNIGHT_WHIRLWIND  = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/whirlwindabilityunlock");
    public static final Identifier WORKING_IN_RAIN   = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/workinginrainunlock");
    public static final Identifier USE_TOTEM         = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/usetotemunlock");
    public static final Identifier RECIPE_MODE       = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/recipemodeunlock");
    public static final Identifier BUILDER_MODE      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/buildermodeunlock");
    public static final Identifier DRUID_USE_POTIONS = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/consumepotions");
    public static final Identifier SOFT_SHOES        = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/softshoesunlock");
    public static final Identifier FISH_TREASURE     = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/fishingtreasure");
    public static final Identifier NETHER_LOG        = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/netherexpeditionlog");
    public static final Identifier MASKS             = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/masks");
    public static final Identifier VACCINES          = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/vaccines");
    public static final Identifier TELESCOPE          = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/telescope");
    public static final Identifier STANDARD          = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/standard");
    public static final Identifier MORE_AIR          = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/air");
    public static final Identifier MIN_ORDER         = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/min_order");
    public static final Identifier GREEN_REVOLUTION  = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/greenrevolution");
    public static final Identifier BUILDERS_ASSISTANT_HAMMER = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/assistanthammerunlock");
    public static final Identifier MARKSMAN          = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/marksman");
    public static final Identifier HUSCARL           = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/huscarl");

    //Recipe unlocks
    public static final Identifier THE_END           = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/knowledgeoftheendunlock");
    public static final Identifier THE_DEPTHS        = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/knowledgeofthedepthsunlock");
    public static final Identifier MORE_SCROLLS      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/morescrollsunlock");
    public static final Identifier SIFTER_STRING     = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/sifterstringunlock");
    public static final Identifier SIFTER_FLINT      = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/sifterflintunlock");
    public static final Identifier SIFTER_IRON       = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/sifterironunlock");
    public static final Identifier SIFTER_DIAMOND    = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "effects/sifterdiamondunlock");

    /**
     * Predicate for selecting any fire-related damage
     */
    public static final Predicate<ResourceKey<DamageType>> FIRE_DAMAGE_PREDICATE = type -> type.equals(DamageTypes.LAVA)
                                                                                             || type.equals(DamageTypes.HOT_FLOOR)
                                                                                             || type.equals(DamageTypes.IN_FIRE)
                                                                                             || type.equals(DamageTypes.ON_FIRE);

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
