package com.minecolonies.api.util.constant;

/**
 * Class which holds the tags used in schematics
 */
public class SchematicTagConstants
{
    /**
     * General tags.
     */
    public static final String TAG_SITTING   = "sit";
    public static final String TAG_WORK      = "work";
    public static final String TAG_SIT_IN    = "sit_in";
    public static final String TAG_SIT_OUT   = "sit_out";
    public static final String TAG_STAND_IN  = "stand_in";
    public static final String TAG_STAND_OUT = "stand_out";
    public static final String BUILDING_SIGN = "building_sign";

    /**
     * Gate tags.
     */
    public static final String TAG_KNIGHT = "knight";
    public static final String TAG_ARCHER = "archer";
    public static final String TAG_GATE   = "gate";

    /**
     * Raider tags.
     */
    public static final String NORMAL_RAIDER = "normal";
    public static final String ARCHER_RAIDER = "archer";
    public static final String BOSS_RAIDER   = "boss";

    /**
     * Plantation field tags.
     */
    public static final String SUGAR_FIELD       = "sugar_field";
    public static final String SUGAR_CROP        = "sugar";
    public static final String CACTUS_FIELD      = "cactus_field";
    public static final String CACTUS_CROP       = "cactus";
    public static final String BAMBOO_FIELD      = "bamboo_field";
    public static final String BAMBOO_CROP       = "bamboo";
    public static final String COCOA_FIELD       = "cocoa_field";
    public static final String COCOA_CROP        = "cocoa";
    public static final String VINE_FIELD        = "vine_field";
    public static final String VINE_CROP         = "vine";
    public static final String KELP_FIELD        = "kelp_field";
    public static final String KELP_CROP         = "kelp";
    public static final String SEA_GRASS_FIELD   = "seagrass_field";
    public static final String SEA_GRASS_CROP    = "seagrass";
    public static final String SEA_PICKLE_FIELD  = "seapickle_field";
    public static final String SEA_PICKLE_CROP   = "seapickle";
    public static final String GLOW_BERRY_FIELD  = "glowb_field";
    public static final String GLOW_BERRY_CROP   = "glowb_vine";
    public static final String WEEPY_VINE_FIELD  = "weepv_field";
    public static final String WEEPY_VINE_CROP   = "weepv_vine";
    public static final String TWISTY_VINE_FIELD = "twistv_field";
    public static final String TWISTY_VINE_CROP  = "twistv_vine";
    public static final String CRIMSON_FIELD     = "crimsonp_field";
    public static final String CRIMSON_CROP      = "crimsonp_ground";
    public static final String WARPED_FIELD      = "warpedp_field";
    public static final String WARPED_CROP       = "warpedp_ground";

    public static String[] getPlantationTags()
    {
        return new String[] {
            SUGAR_FIELD,       SUGAR_CROP,
            CACTUS_FIELD,      CACTUS_CROP,
            BAMBOO_FIELD,      BAMBOO_CROP,
            COCOA_FIELD,       COCOA_CROP,
            VINE_FIELD,        VINE_CROP,
            KELP_FIELD,        KELP_CROP,
            SEA_GRASS_FIELD,   SEA_GRASS_CROP,
            SEA_PICKLE_FIELD,  SEA_PICKLE_CROP,
            GLOW_BERRY_FIELD,  GLOW_BERRY_CROP,
            WEEPY_VINE_FIELD,  WEEPY_VINE_CROP,
            TWISTY_VINE_FIELD, TWISTY_VINE_CROP,
            CRIMSON_FIELD,     CRIMSON_CROP,
            WARPED_FIELD,      WARPED_CROP
        };
    }
}
