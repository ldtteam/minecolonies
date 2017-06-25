package com.minecolonies.api.reference;

import net.minecraft.item.Item;

/**
 * Class handling the registering of the mod items.
 */
public final class ModItems
{
    public static Item supplyChest;
    public static Item buildTool;
    public static Item scanTool;
    public static Item permTool;
    public static Item caliper;
    public static Item scepterGuard;
    public static Item supplyCamp;

    public static Item itemAchievementProxySettlement;
    public static Item itemAchievementProxyTown;
    public static Item itemAchievementProxyCity;
    public static Item itemAchievementProxyMetropolis;

    // deactivated for now
    // public static Item compost    = new ItemCompost();

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
