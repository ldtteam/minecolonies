package com.minecolonies.api.items;

import net.minecraft.item.Item;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444"})
public final class ModItems
{
    public static Item supplyChest;
    public static Item permTool;
    public static Item scepterGuard;
    public static Item bannerRallyGuards;
    public static Item supplyCamp;
    public static Item ancientTome;
    public static Item chiefSword;
    public static Item scimitar;
    public static Item scepterLumberjack;
    public static Item pharaoscepter;
    public static Item firearrow;

    public static Item clipboard;
    public static Item compost;
    public static Item resourceScroll;

    public static Item pirateHelmet_1;
    public static Item pirateChest_1;
    public static Item pirateLegs_1;
    public static Item pirateBoots_1;

    public static Item pirateHelmet_2;
    public static Item pirateChest_2;
    public static Item pirateLegs_2;
    public static Item pirateBoots_2;

    public static Item santaHat;

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModItems()
    {
        /*
         * Intentionally left empty.
         */
    }
}