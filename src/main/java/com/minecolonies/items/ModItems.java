package com.minecolonies.items;

import net.minecraft.item.Item;

public final class ModItems
{
    public static Item supplyChest;

    public static void init()
    {
        supplyChest = new SupplyChest();
    }
}
