package com.minecolonies.api.util.constant;

import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import net.minecraft.resources.ResourceLocation;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Constants used by the Undertaker AIs.
 */
public class UndertakerConstants
{
    /**
     * The EXP Earned per dig.
     */
    public static final double XP_PER_DIG = 7.5;

    /**
     * The EXP Earned per wander.
     */
    public static final double XP_PER_WANDER = 2;

    /**
     * The weigth of each building level on the resurrection chances.
     */
    public static final double RESURRECT_BUILDING_LVL_WEIGHT = 0.005;

    /**
     * The weigth of each worker level on the resurrection chances.
     */
    public static final double RESURRECT_WORKER_MANA_LVL_WEIGHT = 0.00125;

    /**
     * The max resurrection chance cap [0.0 min equals 1.0 max]
     */
    public static final double MAX_RESURRECTION_CHANCE = 0.025;

    /**
     * The bonus to max resurrection chance cap per max lvl of Mystical Site in the city
     */
    public static final double MAX_RESURRECTION_CHANCE_MYSTICAL_LVL_BONUS = 0.005;

    /**
     * The bonus to the resurrection chance for having a single totem of undying
     */
    public static final double SINGLE_TOTEM_RESURRECTION_CHANCE_BONUS = 0.05;

    /**
     * The bonus to the resurrection chance for having multiples totems of undying
     */
    public static final double MULTIPLE_TOTEMS_RESURRECTION_CHANCE_BONUS = 0.075;

    /**
     * The chance that a used totem of undying breaks on each resurrection attempt
     */
    public static final double TOTEM_BREAK_CHANCE = 0.01;

    /**
     * Effort needed to empty a grave
     */
    public static final int EFFORT_EMPTY_GRAVE = 100;

    /**
     * Effort needed to burry a citizen
     */
    public static final int EFFORT_BURY = 400;

    /**
     * Effort needed to resurrect a citizen
     */
    public static final int EFFORT_RESURRECT = 400;
    /**
     * Undertaker emptying icon
     */
    public final static VisibleCitizenStatus EMPTYING_ICON =
            new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/undertaker.png"), MESSAGE_INFO_CITIZEN_STATUS_UNDERTAKER_EMPTYING);

    /**
     * Undertaker digging icon
     */
    public final static VisibleCitizenStatus DIGGING_ICON =
            new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/undertaker.png"), MESSAGE_INFO_CITIZEN_STATUS_UNDERTAKER_DIGGING);

    /**
     * Undertaker bury icon
     */
    public final static VisibleCitizenStatus BURYING_ICON =
            new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/undertaker.png"), MESSAGE_INFO_CITIZEN_STATUS_UNDERTAKER_BURYING);

}
