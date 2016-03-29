package com.minecolonies.items;

import net.minecraft.item.Item;

public final class ModItems
{
    public static Item supplyChest;
    public static Item buildTool;
    public static Item scanTool;
    public static Item caliper;

    public static void init()
    {
        supplyChest = new ItemSupplyChestDeployer();
        buildTool   = new ItemBuildTool();
        scanTool    = new ItemScanTool();
        caliper     = new ItemCaliper();
    }
}
