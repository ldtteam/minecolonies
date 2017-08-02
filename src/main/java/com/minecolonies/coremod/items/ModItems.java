package com.minecolonies.coremod.items;

import com.minecolonies.coremod.blocks.*;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

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
    public static Item ancientTome;
    public static Item chiefSword;

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
     * @param registry
     */
    public static void init(final IForgeRegistry<Item> registry)
    {
        registry.register(supplyChest = new ItemSupplyChestDeployer());
        registry.register(buildTool = new ItemBuildTool());
        registry.register(scanTool = new ItemScanTool());
        registry.register(permTool = new ItemScepterPermission());
        registry.register(caliper = new ItemCaliper());
        registry.register(scepterGuard = new ItemScepterGuard());
        registry.register(supplyCamp = new ItemSupplyCampDeployer());
        registry.register(ancientTome = new ItemAncientTome());
        registry.register(chiefSword = new ItemChiefSword());
    }
}
