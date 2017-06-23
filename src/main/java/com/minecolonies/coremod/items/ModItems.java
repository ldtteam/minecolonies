package com.minecolonies.coremod.items;

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

    public static final Item itemAchievementProxySettlement = new ItemAchievementProxy("sizeSettlement");
    public static final Item itemAchievementProxyTown       = new ItemAchievementProxy("sizeTown");
    public static final Item itemAchievementProxyCity       = new ItemAchievementProxy("sizeCity");
    public static final Item itemAchievementProxyMetropolis = new ItemAchievementProxy("sizeMetropolis");

    // deactivated for now
    // public static final Item compost    = new ItemCompost();

    /**
     * Private constructor to hide the implicit public one.
     */
    public ModItems()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Initates all the blocks. At the correct time.
     */
    public static void init()
    {
        supplyChest = new ItemSupplyChestDeployer();
        buildTool = new ItemBuildTool();
        scanTool = new ItemScanTool();
        permTool = new ItemScepterPermission();
        caliper = new ItemCaliper();
        scepterGuard = new ItemScepterGuard();
        supplyCamp = new ItemSupplyCampDeployer();
    }
}
