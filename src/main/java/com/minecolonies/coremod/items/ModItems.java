package com.minecolonies.coremod.items;

import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444"})
public final class ModItems
{
    public static Item supplyChest;
    public static Item permTool;
    public static Item caliper;
    public static Item scepterGuard;
    public static Item supplyCamp;
    public static Item ancientTome;
    public static Item chiefSword;
    public static Item scimitar;

    public static Item clipboard;
    public static Item compost;
    public static Item resourceScroll;

    public static Item pirateHelmet_1;
    public static Item pirateChest_1;
    public static Item pirateLegs_1;
    public static Item pirateBoots_1;

    public static Item pirateHelmet_2;
    public static Item pirateChest_2;
    public static Item pirateLegs_2;
    public static Item pirateBoots_2;

    public static Item itemAchievementProxySettlement;
    public static Item itemAchievementProxyTown;
    public static Item itemAchievementProxyCity;
    public static Item itemAchievementProxyMetropolis;

    public static Item santaHat;

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModItems()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Initates all the blocks. At the correct time.
     */
    public static void init(final IForgeRegistry<Item> registry)
    {
        supplyChest = new ItemSupplyChestDeployer();
        permTool = new ItemScepterPermission();
        caliper = new ItemCaliper();
        scepterGuard = new ItemScepterGuard();
        supplyCamp = new ItemSupplyCampDeployer();
        ancientTome = new ItemAncientTome();
        chiefSword = new ItemChiefSword();
        scimitar = new ItemIronScimitar();
        clipboard = new ItemClipBoard();
        compost = new ItemCompost();
        resourceScroll = new ItemResourceScroll();

        santaHat = new ItemSantaHead("santa_hat", ModCreativeTabs.MINECOLONIES, ItemSantaHead.SANTA_HAT, 0, EntityEquipmentSlot.HEAD);

        pirateHelmet_1 = new ItemPirateGear("pirate_hat", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, 0, EntityEquipmentSlot.HEAD);
        pirateChest_1 = new ItemPirateGear("pirate_top", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, 0, EntityEquipmentSlot.CHEST);
        pirateLegs_1 = new ItemPirateGear("pirate_leggins", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, 1, EntityEquipmentSlot.LEGS);
        pirateBoots_1 = new ItemPirateGear("pirate_boots", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, 0, EntityEquipmentSlot.FEET);

        pirateHelmet_2 = new ItemPirateGear("pirate_cap", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, 0, EntityEquipmentSlot.HEAD);
        pirateChest_2 = new ItemPirateGear("pirate_chest", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, 0, EntityEquipmentSlot.CHEST);
        pirateLegs_2 = new ItemPirateGear("pirate_legs", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, 1, EntityEquipmentSlot.LEGS);
        pirateBoots_2 = new ItemPirateGear("pirate_shoes", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, 0, EntityEquipmentSlot.FEET);

        itemAchievementProxySettlement = new ItemAchievementProxy("sizeSettlement");
        itemAchievementProxyTown = new ItemAchievementProxy("sizeTown");
        itemAchievementProxyCity = new ItemAchievementProxy("sizeCity");
        itemAchievementProxyMetropolis = new ItemAchievementProxy("sizeMetropolis");

        registry.register(supplyChest);
        registry.register(permTool);
        registry.register(caliper);
        registry.register(scepterGuard);
        registry.register(supplyCamp);
        registry.register(ancientTome);
        registry.register(chiefSword);
        registry.register(clipboard);
        registry.register(compost);
        registry.register(resourceScroll);
        registry.register(scimitar);

        registry.register(pirateHelmet_1);
        registry.register(pirateChest_1);
        registry.register(pirateLegs_1);
        registry.register(pirateBoots_1);

        registry.register(pirateHelmet_2);
        registry.register(pirateChest_2);
        registry.register(pirateLegs_2);
        registry.register(pirateBoots_2);

        registry.register(itemAchievementProxySettlement);
        registry.register(itemAchievementProxyTown);
        registry.register(itemAchievementProxyCity);
        registry.register(itemAchievementProxyMetropolis);

        registry.register(santaHat);
    }
}