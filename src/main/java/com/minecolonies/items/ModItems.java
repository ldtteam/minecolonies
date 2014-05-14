package com.minecolonies.items;

import net.minecraft.item.Item;

public final class ModItems
{
    public static Item supplyChest;
    public static Item buildTool;
    public static Item caliper;

    public static void init()
    {
        supplyChest = new ItemSupplyChestDeployer();
        buildTool = new ItemBuildTool();
        caliper = new ItemCaliper();
    }
}
