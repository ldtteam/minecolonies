package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.items.*;
import net.minecraft.inventory.EquipmentSlotType;
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
    public static void init(final IForgeRegistry<Item> registry)
    {
        ModItems.supplyChest = new ItemSupplyChestDeployer(new Item.Properties());
        ModItems.permTool = new ItemScepterPermission(new Item.Properties());
        ModItems.caliper = new ItemCaliper(new Item.Properties());
        ModItems.scepterGuard = new ItemScepterGuard(new Item.Properties());
        ModItems.supplyCamp = new ItemSupplyCampDeployer(new Item.Properties());
        ModItems.ancientTome = new ItemAncientTome(new Item.Properties());
        ModItems.chiefSword = new ItemChiefSword(new Item.Properties());
        ModItems.scimitar = new ItemIronScimitar(new Item.Properties());
        ModItems.clipboard = new ItemClipBoard(new Item.Properties());
        ModItems.compost = new ItemCompost(new Item.Properties());
        ModItems.resourceScroll = new ItemResourceScroll(new Item.Properties());

        ModItems.santaHat = new ItemSantaHead("santa_hat", ModCreativeTabs.MINECOLONIES, ItemSantaHead.SANTA_HAT, EquipmentSlotType.HEAD, new Item.Properties());

        ModItems.pirateHelmet_1 = new ItemPirateGear("pirate_hat", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, EquipmentSlotType.HEAD, new Item.Properties());
        ModItems.pirateChest_1 = new ItemPirateGear("pirate_top", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1,  EquipmentSlotType.CHEST, new Item.Properties());
        ModItems.pirateLegs_1 = new ItemPirateGear("pirate_leggins", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, EquipmentSlotType.LEGS, new Item.Properties());
        ModItems.pirateBoots_1 = new ItemPirateGear("pirate_boots", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, EquipmentSlotType.FEET, new Item.Properties());

        ModItems.pirateHelmet_2 = new ItemPirateGear("pirate_cap", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlotType.HEAD, new Item.Properties());
        ModItems.pirateChest_2 = new ItemPirateGear("pirate_chest", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlotType.CHEST, new Item.Properties());
        ModItems.pirateLegs_2 = new ItemPirateGear("pirate_legs", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlotType.LEGS, new Item.Properties());
        ModItems.pirateBoots_2 = new ItemPirateGear("pirate_shoes", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlotType.FEET, new Item.Properties());

        ModItems.itemAchievementProxySettlement = new ItemAchievementProxy("sizeSettlement", new Item.Properties());
        ModItems.itemAchievementProxyTown = new ItemAchievementProxy("sizeTown", new Item.Properties());
        ModItems.itemAchievementProxyCity = new ItemAchievementProxy("sizeCity", new Item.Properties());
        ModItems.itemAchievementProxyMetropolis = new ItemAchievementProxy("sizeMetropolis", new Item.Properties());

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
