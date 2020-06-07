package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.items.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModItemsInitializer
{
    /**
     * Spawn egg colors.
     */
    private static final int PRIMARY_COLOR_BARBARIAN   = 5;
    private static final int SECONDARY_COLOR_BARBARIAN = 700;
    private static final int PRIMARY_COLOR_PIRATE   = 7;
    private static final int SECONDARY_COLOR_PIRATE = 600;
    private static final int PRIMARY_COLOR_MERC   = 8;
    private static final int SECONDARY_COLOR_MERC = 300;
    private static final int PRIMARY_COLOR_EG   = 10;
    private static final int SECONDARY_COLOR_EG = 400;

    private ModItemsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModItemsInitializer but this is a Utility class.");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        ModItemsInitializer.init(event.getRegistry());
    }


    /**
     * Initates all the blocks. At the correct time.
     * @param registry the registry.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final IForgeRegistry<Item> registry)
    {
        EntityInitializer.setupEntities();

        ModItems.scepterLumberjack = new ItemScepterLumberjack(new Item.Properties());
        ModItems.supplyChest = new ItemSupplyChestDeployer(new Item.Properties());
        ModItems.permTool = new ItemScepterPermission(new Item.Properties());
        ModItems.scepterGuard = new ItemScepterGuard(new Item.Properties());
        ModItems.supplyCamp = new ItemSupplyCampDeployer(new Item.Properties());
        ModItems.ancientTome = new ItemAncientTome(new Item.Properties());
        ModItems.chiefSword = new ItemChiefSword(new Item.Properties());
        ModItems.scimitar = new ItemIronScimitar(new Item.Properties());
        ModItems.clipboard = new ItemClipBoard(new Item.Properties());
        ModItems.compost = new ItemCompost(new Item.Properties());
        ModItems.resourceScroll = new ItemResourceScroll(new Item.Properties());
        ModItems.pharaoscepter = new ItemPharaoScepter(new Item.Properties());
        ModItems.firearrow = new ItemFireArrow(new Item.Properties());
        ModItems.scepterBeekeeper = new ItemScepterBeekeeper(new Item.Properties());

        ModItems.santaHat = new ItemSantaHead("santa_hat", ModCreativeTabs.MINECOLONIES, ItemSantaHead.SANTA_HAT, EquipmentSlotType.HEAD, new Item.Properties());

        ModItems.pirateHelmet_1 = new ItemPirateGear("pirate_hat", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, EquipmentSlotType.HEAD, new Item.Properties());
        ModItems.pirateChest_1 = new ItemPirateGear("pirate_top", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1,  EquipmentSlotType.CHEST, new Item.Properties());
        ModItems.pirateLegs_1 = new ItemPirateGear("pirate_leggins", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, EquipmentSlotType.LEGS, new Item.Properties());
        ModItems.pirateBoots_1 = new ItemPirateGear("pirate_boots", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, EquipmentSlotType.FEET, new Item.Properties());

        ModItems.pirateHelmet_2 = new ItemPirateGear("pirate_cap", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlotType.HEAD, new Item.Properties());
        ModItems.pirateChest_2 = new ItemPirateGear("pirate_chest", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlotType.CHEST, new Item.Properties());
        ModItems.pirateLegs_2 = new ItemPirateGear("pirate_legs", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlotType.LEGS, new Item.Properties());
        ModItems.pirateBoots_2 = new ItemPirateGear("pirate_shoes", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlotType.FEET, new Item.Properties());

        registry.register(ModItems.supplyChest);
        registry.register(ModItems.permTool);
        registry.register(ModItems.scepterGuard);
        registry.register(ModItems.supplyCamp);
        registry.register(ModItems.ancientTome);
        registry.register(ModItems.chiefSword);
        registry.register(ModItems.clipboard);
        registry.register(ModItems.compost);
        registry.register(ModItems.resourceScroll);
        registry.register(ModItems.scimitar);
        registry.register(ModItems.scepterLumberjack);
        registry.register(ModItems.pharaoscepter);
        registry.register(ModItems.firearrow);
        registry.register(ModItems.scepterBeekeeper);

        registry.register(ModItems.pirateHelmet_1);
        registry.register(ModItems.pirateChest_1);
        registry.register(ModItems.pirateLegs_1);
        registry.register(ModItems.pirateBoots_1);

        registry.register(ModItems.pirateHelmet_2);
        registry.register(ModItems.pirateChest_2);
        registry.register(ModItems.pirateLegs_2);
        registry.register(ModItems.pirateBoots_2);

        registry.register(ModItems.santaHat);

        registry.register(new SpawnEggItem(ModEntities.BARBARIAN, PRIMARY_COLOR_BARBARIAN, SECONDARY_COLOR_BARBARIAN, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("barbarianegg"));
        registry.register(new SpawnEggItem(ModEntities.ARCHERBARBARIAN, PRIMARY_COLOR_BARBARIAN, SECONDARY_COLOR_BARBARIAN, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("barbarcheregg"));
        registry.register(new SpawnEggItem(ModEntities.CHIEFBARBARIAN, PRIMARY_COLOR_BARBARIAN, SECONDARY_COLOR_BARBARIAN, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("barbchiefegg"));

        registry.register(new SpawnEggItem(ModEntities.PIRATE, PRIMARY_COLOR_PIRATE, SECONDARY_COLOR_PIRATE, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("pirateegg"));
        registry.register(new SpawnEggItem(ModEntities.ARCHERPIRATE, PRIMARY_COLOR_PIRATE, SECONDARY_COLOR_PIRATE, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("piratearcheregg"));
        registry.register(new SpawnEggItem(ModEntities.CHIEFPIRATE, PRIMARY_COLOR_PIRATE, SECONDARY_COLOR_PIRATE, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("piratecaptainegg"));

        registry.register(new SpawnEggItem(ModEntities.MUMMY, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("mummyegg"));
        registry.register(new SpawnEggItem(ModEntities.ARCHERMUMMY, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("mummyarcheregg"));
        registry.register(new SpawnEggItem(ModEntities.PHARAO, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("pharaoegg"));

        registry.register(new SpawnEggItem(ModEntities.SHIELDMAIDEN, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("shieldmaidenegg"));
        registry.register(new SpawnEggItem(ModEntities.NORSEMEN_ARCHER, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("norsemenarcheregg"));
        registry.register(new SpawnEggItem(ModEntities.NORSEMEN_CHIEF, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("norsemenchiefegg"));

        registry.register(new SpawnEggItem(ModEntities.AMAZON, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("amazonegg"));
        registry.register(new SpawnEggItem(ModEntities.AMAZONCHIEF, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("amazonchiefegg"));

        registry.register(new SpawnEggItem(ModEntities.MERCENARY, PRIMARY_COLOR_MERC, SECONDARY_COLOR_MERC, (new Item.Properties()).group(ModCreativeTabs.MINECOLONIES)).setRegistryName("mercegg"));
    }
}
