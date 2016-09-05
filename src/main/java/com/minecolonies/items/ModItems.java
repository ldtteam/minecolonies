package com.minecolonies.items;

import net.minecraft.item.Item;

public final class ModItems
{
    public static final Item supplyChest = new ItemSupplyChestDeployer();
    public static final Item buildTool   = new ItemBuildTool();
    public static final Item scanTool    = new ItemScanTool();
    public static final Item caliper     = new ItemCaliper();
    
    public static final Item itemProxySizeSettlement = new ItemProxyColonySize("sizeSettlement");
    public static final Item itemProxySizeTown       = new ItemProxyColonySize("sizeTown");
    public static final Item itemProxySizeCity       = new ItemProxyColonySize("sizeCity");
    public static final Item itemProxySizeMetropolis = new ItemProxyColonySize("sizeMetropolis");

    /**
     * private constructor to hide the implicit public one.
     */
    private ModItems()
    {
    }
}
