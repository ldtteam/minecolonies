package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.items.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModItemsInitializer
{

    private ModItemsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModItemsInitializer but this is a Utility class.");
    }

    /**
     * Initates all the blocks. At the correct time.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final IForgeRegistry<Item> registry)
    {
        ModItems.supplyChest = new ItemSupplyChestDeployer();
        ModItems.permTool = new ItemScepterPermission();
        ModItems.caliper = new ItemCaliper();
        ModItems.scepterGuard = new ItemScepterGuard();
        ModItems.supplyCamp = new ItemSupplyCampDeployer();
        ModItems.ancientTome = new ItemAncientTome();
        ModItems.chiefSword = new ItemChiefSword();
        ModItems.scimitar = new ItemIronScimitar();
        ModItems.clipboard = new ItemClipBoard();
        ModItems.compost = new ItemCompost();
        ModItems.resourceScroll = new ItemResourceScroll();
        ModItems.scepterLumberjack = new ItemScepterLumberjack();

        ModItems.santaHat = new ItemSantaHead("santa_hat", ModCreativeTabs.MINECOLONIES, ItemSantaHead.SANTA_HAT, 0, EntityEquipmentSlot.HEAD);

        ModItems.pirateHelmet_1 = new ItemPirateGear("pirate_hat", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, 0, EntityEquipmentSlot.HEAD);
        ModItems.pirateChest_1 = new ItemPirateGear("pirate_top", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, 0, EntityEquipmentSlot.CHEST);
        ModItems.pirateLegs_1 = new ItemPirateGear("pirate_leggins", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, 1, EntityEquipmentSlot.LEGS);
        ModItems.pirateBoots_1 = new ItemPirateGear("pirate_boots", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, 0, EntityEquipmentSlot.FEET);

        ModItems.pirateHelmet_2 = new ItemPirateGear("pirate_cap", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, 0, EntityEquipmentSlot.HEAD);
        ModItems.pirateChest_2 = new ItemPirateGear("pirate_chest", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, 0, EntityEquipmentSlot.CHEST);
        ModItems.pirateLegs_2 = new ItemPirateGear("pirate_legs", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, 1, EntityEquipmentSlot.LEGS);
        ModItems.pirateBoots_2 = new ItemPirateGear("pirate_shoes", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, 0, EntityEquipmentSlot.FEET);

        ModItems.itemAchievementProxySettlement = new ItemAchievementProxy("sizeSettlement");
        ModItems.itemAchievementProxyTown = new ItemAchievementProxy("sizeTown");
        ModItems.itemAchievementProxyCity = new ItemAchievementProxy("sizeCity");
        ModItems.itemAchievementProxyMetropolis = new ItemAchievementProxy("sizeMetropolis");

        registry.register(ModItems.supplyChest);
        registry.register(ModItems.permTool);
        registry.register(ModItems.caliper);
        registry.register(ModItems.scepterGuard);
        registry.register(ModItems.supplyCamp);
        registry.register(ModItems.ancientTome);
        registry.register(ModItems.chiefSword);
        registry.register(ModItems.clipboard);
        registry.register(ModItems.compost);
        registry.register(ModItems.resourceScroll);
        registry.register(ModItems.scimitar);
        registry.register(ModItems.scepterLumberjack);

        registry.register(ModItems.pirateHelmet_1);
        registry.register(ModItems.pirateChest_1);
        registry.register(ModItems.pirateLegs_1);
        registry.register(ModItems.pirateBoots_1);

        registry.register(ModItems.pirateHelmet_2);
        registry.register(ModItems.pirateChest_2);
        registry.register(ModItems.pirateLegs_2);
        registry.register(ModItems.pirateBoots_2);

        registry.register(ModItems.itemAchievementProxySettlement);
        registry.register(ModItems.itemAchievementProxyTown);
        registry.register(ModItems.itemAchievementProxyCity);
        registry.register(ModItems.itemAchievementProxyMetropolis);

        registry.register(ModItems.santaHat);
    }
}
