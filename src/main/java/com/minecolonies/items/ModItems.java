package com.minecolonies.items;

import net.minecraft.item.Item;

public final class ModItems
{
    public static final Item supplyChest = new ItemSupplyChestDeployer();
    public static final Item buildTool   = new ItemBuildTool();
    public static final Item scanTool    = new ItemScanTool();
    public static final Item caliper     = new ItemCaliper();

    /**
     * private constructor to hide the implicit public one.
     */
    private ModItems()
    {
    }
}
