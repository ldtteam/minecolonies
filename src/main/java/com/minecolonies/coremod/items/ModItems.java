package com.minecolonies.coremod.items;

import net.minecraft.item.Item;

/**
 * Class handling the registering of the mod items.
 */
public final class ModItems
{
    public static final Item supplyChest  = new ItemSupplyChestDeployer();
    public static final Item buildTool    = new ItemBuildTool();
    public static final Item scanTool     = new ItemScanTool();
    public static final Item permTool     = new ItemScepterPermission();
    public static final Item caliper      = new ItemCaliper();
    public static final Item scepterGuard = new ItemScepterGuard();
    public static final Item supplyCamp   = new ItemSupplyCampDeployer();
    public static final Item ancientTome  = new ItemAncientTome();
    public static final Item chiefSword   = new ItemChiefSword();

    public static final Item itemAchievementProxySettlement = new ItemAchievementProxy("sizeSettlement");
    public static final Item itemAchievementProxyTown       = new ItemAchievementProxy("sizeTown");
    public static final Item itemAchievementProxyCity       = new ItemAchievementProxy("sizeCity");
    public static final Item itemAchievementProxyMetropolis = new ItemAchievementProxy("sizeMetropolis");

    // deactivated for now
    // public static final Item compost    = new ItemCompost();

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
