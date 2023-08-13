package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.items.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

import static com.minecolonies.api.blocks.decorative.AbstractBlockGate.IRON_GATE;
import static com.minecolonies.api.blocks.decorative.AbstractBlockGate.WOODEN_GATE;
import static com.minecolonies.api.util.constant.Constants.CHIEFSWORD_NAME;
import static com.minecolonies.api.util.constant.Constants.SCIMITAR_NAME;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModItemsInitializer
{
    /**
     * Spawn egg colors.
     */
    private static final int PRIMARY_COLOR_BARBARIAN   = 5;
    private static final int SECONDARY_COLOR_BARBARIAN = 700;
    private static final int PRIMARY_COLOR_PIRATE      = 7;
    private static final int SECONDARY_COLOR_PIRATE    = 600;
    private static final int PRIMARY_COLOR_MERC        = 8;
    private static final int SECONDARY_COLOR_MERC      = 300;
    private static final int PRIMARY_COLOR_EG          = 10;
    private static final int SECONDARY_COLOR_EG        = 400;

    private ModItemsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModItemsInitializer but this is a Utility class.");
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS))
        {
            ModItemsInitializer.init(event.getForgeRegistry());
        }
    }

    /**
     * Initates all the blocks. At the correct time.
     *
     * @param registry the registry.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final IForgeRegistry<Item> registry)
    {
        ModItems.scepterLumberjack = new ItemScepterLumberjack(new Item.Properties());
        ModItems.supplyChest = new ItemSupplyChestDeployer(new Item.Properties());
        ModItems.permTool = new ItemScepterPermission(new Item.Properties());
        ModItems.scepterGuard = new ItemScepterGuard(new Item.Properties());
        ModItems.bannerRallyGuards = new ItemBannerRallyGuards(new Item.Properties());
        ModItems.supplyCamp = new ItemSupplyCampDeployer(new Item.Properties());
        ModItems.ancientTome = new ItemAncientTome(new Item.Properties());
        ModItems.chiefSword = new ItemChiefSword(new Item.Properties());
        ModItems.scimitar = new ItemIronScimitar(new Item.Properties());
        ModItems.clipboard = new ItemClipboard(new Item.Properties());
        ModItems.compost = new ItemCompost(new Item.Properties());
        ModItems.resourceScroll = new ItemResourceScroll(new Item.Properties());
        ModItems.pharaoscepter = new ItemPharaoScepter(new Item.Properties());
        ModItems.firearrow = new ItemFireArrow(new Item.Properties());
        ModItems.scepterBeekeeper = new ItemScepterBeekeeper(new Item.Properties());
        ModItems.mistletoe = new ItemMistletoe(new Item.Properties());
        ModItems.spear = new ItemSpear(new Item.Properties());

        ModItems.breadDough = new ItemBreadDough(new Item.Properties());
        ModItems.cookieDough = new ItemCookieDough(new Item.Properties());
        ModItems.cakeBatter = new ItemCakeBatter(new Item.Properties());
        ModItems.rawPumpkinPie = new ItemRawPumpkinPie(new Item.Properties());

        ModItems.milkyBread = new ItemMilkyBread(new Item.Properties());
        ModItems.sugaryBread = new ItemSugaryBread(new Item.Properties());
        ModItems.goldenBread = new ItemGoldenBread(new Item.Properties());
        ModItems.chorusBread = new ItemChorusBread(new Item.Properties());

        ModItems.adventureToken = new ItemAdventureToken(new Item.Properties());

        ModItems.scrollColonyTP = new ItemScrollColonyTP(new Item.Properties().stacksTo(16).tab(ModCreativeTabs.MINECOLONIES));
        registry.register(new ResourceLocation(Constants.MOD_ID, "scroll_tp"), ModItems.scrollColonyTP);

        ModItems.scrollColonyAreaTP = new ItemScrollColonyAreaTP(new Item.Properties().stacksTo(16).tab(ModCreativeTabs.MINECOLONIES));
        registry.register(new ResourceLocation(Constants.MOD_ID, "scroll_area_tp"), ModItems.scrollColonyAreaTP);

        ModItems.scrollBuff = new ItemScrollBuff(new Item.Properties().stacksTo(16).tab(ModCreativeTabs.MINECOLONIES));
        registry.register(new ResourceLocation(Constants.MOD_ID, "scroll_buff"), ModItems.scrollBuff);

        ModItems.scrollGuardHelp = new ItemScrollGuardHelp(new Item.Properties().stacksTo(16).tab(ModCreativeTabs.MINECOLONIES));
        registry.register(new ResourceLocation(Constants.MOD_ID, "scroll_guard_help"), ModItems.scrollGuardHelp);

        ModItems.scrollHighLight = new ItemScrollHighlight(new Item.Properties().stacksTo(16).tab(ModCreativeTabs.MINECOLONIES));
        registry.register(new ResourceLocation(Constants.MOD_ID, "scroll_highlight"), ModItems.scrollHighLight);

        ModItems.santaHat = new ItemSantaHead("santa_hat", ModCreativeTabs.MINECOLONIES, ItemSantaHead.SANTA_HAT, EquipmentSlot.HEAD, new Item.Properties());
        ModItems.irongate = new ItemGate(IRON_GATE, ModBlocks.blockIronGate, ModCreativeTabs.MINECOLONIES, new Item.Properties());
        ModItems.woodgate = new ItemGate(WOODEN_GATE, ModBlocks.blockWoodenGate, ModCreativeTabs.MINECOLONIES, new Item.Properties());

        ModItems.flagBanner = new ItemColonyFlagBanner("colony_banner", ModCreativeTabs.MINECOLONIES, new Item.Properties());
        ModItems.pirateHelmet_1 = new ItemPirateGear("pirate_hat", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, EquipmentSlot.HEAD, new Item.Properties());
        ModItems.pirateChest_1 = new ItemPirateGear("pirate_top", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, EquipmentSlot.CHEST, new Item.Properties());
        ModItems.pirateLegs_1 = new ItemPirateGear("pirate_leggins", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, EquipmentSlot.LEGS, new Item.Properties());
        ModItems.pirateBoots_1 = new ItemPirateGear("pirate_boots", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_1, EquipmentSlot.FEET, new Item.Properties());

        ModItems.pirateHelmet_2 = new ItemPirateGear("pirate_cap", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlot.HEAD, new Item.Properties());
        ModItems.pirateChest_2 = new ItemPirateGear("pirate_chest", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlot.CHEST, new Item.Properties());
        ModItems.pirateLegs_2 = new ItemPirateGear("pirate_legs", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlot.LEGS, new Item.Properties());
        ModItems.pirateBoots_2 = new ItemPirateGear("pirate_shoes", ModCreativeTabs.MINECOLONIES, ItemPirateGear.PIRATE_ARMOR_2, EquipmentSlot.FEET, new Item.Properties());

        ModItems.plateArmorHelmet = new ItemPlateArmor("plate_armor_helmet", ModCreativeTabs.MINECOLONIES, ItemPlateArmor.PLATE_ARMOR, EquipmentSlot.HEAD, new Item.Properties());
        ModItems.plateArmorChest = new ItemPlateArmor("plate_armor_chest", ModCreativeTabs.MINECOLONIES, ItemPlateArmor.PLATE_ARMOR, EquipmentSlot.CHEST, new Item.Properties());
        ModItems.plateArmorLegs = new ItemPlateArmor("plate_armor_legs", ModCreativeTabs.MINECOLONIES, ItemPlateArmor.PLATE_ARMOR, EquipmentSlot.LEGS, new Item.Properties());
        ModItems.plateArmorBoots = new ItemPlateArmor("plate_armor_boots", ModCreativeTabs.MINECOLONIES, ItemPlateArmor.PLATE_ARMOR, EquipmentSlot.FEET, new Item.Properties());

        ModItems.sifterMeshString = new ItemSifterMesh("sifter_mesh_string", new Item.Properties().durability(500).setNoRepair());
        ModItems.sifterMeshFlint = new ItemSifterMesh("sifter_mesh_flint", new Item.Properties().durability(1000).setNoRepair());
        ModItems.sifterMeshIron = new ItemSifterMesh("sifter_mesh_iron", new Item.Properties().durability(1500).setNoRepair());
        ModItems.sifterMeshDiamond = new ItemSifterMesh("sifter_mesh_diamond", new Item.Properties().durability(2000).setNoRepair());

        ModItems.magicpotion = new ItemMagicPotion("magicpotion", ModCreativeTabs.MINECOLONIES, new Item.Properties());
        ModItems.buildGoggles = new ItemBuildGoggles("build_goggles", ModCreativeTabs.MINECOLONIES, new Item.Properties());
        ModItems.scanAnalyzer = new ItemScanAnalyzer("scan_analyzer", ModCreativeTabs.MINECOLONIES, new Item.Properties());

        registry.register(new ResourceLocation(Constants.MOD_ID, "supplychestdeployer"), ModItems.supplyChest);
        registry.register(new ResourceLocation(Constants.MOD_ID, "scan_analyzer"), ModItems.scanAnalyzer);
        registry.register(new ResourceLocation(Constants.MOD_ID, "scepterpermission"), ModItems.permTool);
        registry.register(new ResourceLocation(Constants.MOD_ID, "scepterguard"), ModItems.scepterGuard);
        registry.register(new ResourceLocation(Constants.MOD_ID, "banner_rally_guards"), ModItems.bannerRallyGuards);
        registry.register(new ResourceLocation(Constants.MOD_ID, "supplycampdeployer"), ModItems.supplyCamp);
        registry.register(new ResourceLocation(Constants.MOD_ID, "ancienttome"), ModItems.ancientTome);
        registry.register(new ResourceLocation(Constants.MOD_ID, CHIEFSWORD_NAME), ModItems.chiefSword);
        registry.register(new ResourceLocation(Constants.MOD_ID, "clipboard"), ModItems.clipboard);
        registry.register(new ResourceLocation(Constants.MOD_ID, "compost"), ModItems.compost);
        registry.register(new ResourceLocation(Constants.MOD_ID, "resourcescroll"), ModItems.resourceScroll);
        registry.register(new ResourceLocation(Constants.MOD_ID, SCIMITAR_NAME), ModItems.scimitar);
        registry.register(new ResourceLocation(Constants.MOD_ID, "scepterlumberjack"), ModItems.scepterLumberjack);
        registry.register(new ResourceLocation(Constants.MOD_ID, "pharaoscepter"), ModItems.pharaoscepter);
        registry.register(new ResourceLocation(Constants.MOD_ID, "firearrow"), ModItems.firearrow);
        registry.register(new ResourceLocation(Constants.MOD_ID, "scepterbeekeeper"), ModItems.scepterBeekeeper);
        registry.register(new ResourceLocation(Constants.MOD_ID, "mistletoe"), ModItems.mistletoe);
        registry.register(new ResourceLocation(Constants.MOD_ID, "spear"), ModItems.spear);

        registry.register(new ResourceLocation(Constants.MOD_ID, "bread_dough"), ModItems.breadDough);
        registry.register(new ResourceLocation(Constants.MOD_ID, "cookie_dough"), ModItems.cookieDough);
        registry.register(new ResourceLocation(Constants.MOD_ID, "cake_batter"), ModItems.cakeBatter);
        registry.register(new ResourceLocation(Constants.MOD_ID, "raw_pumpkin_pie"), ModItems.rawPumpkinPie);

        registry.register(new ResourceLocation(Constants.MOD_ID, "milky_bread"), ModItems.milkyBread);
        registry.register(new ResourceLocation(Constants.MOD_ID, "sugary_bread"), ModItems.sugaryBread);
        registry.register(new ResourceLocation(Constants.MOD_ID, "golden_bread"), ModItems.goldenBread);
        registry.register(new ResourceLocation(Constants.MOD_ID, "chorus_bread"), ModItems.chorusBread);

        registry.register(new ResourceLocation(Constants.MOD_ID, "adventure_token"), ModItems.adventureToken);

        registry.register(new ResourceLocation(Constants.MOD_ID, "pirate_hat"), ModItems.pirateHelmet_1);
        registry.register(new ResourceLocation(Constants.MOD_ID, "pirate_top"), ModItems.pirateChest_1);
        registry.register(new ResourceLocation(Constants.MOD_ID, "pirate_leggins"), ModItems.pirateLegs_1);
        registry.register(new ResourceLocation(Constants.MOD_ID, "pirate_boots"), ModItems.pirateBoots_1);

        registry.register(new ResourceLocation(Constants.MOD_ID, "pirate_cap"), ModItems.pirateHelmet_2);
        registry.register(new ResourceLocation(Constants.MOD_ID, "pirate_chest"), ModItems.pirateChest_2);
        registry.register(new ResourceLocation(Constants.MOD_ID, "pirate_legs"), ModItems.pirateLegs_2);
        registry.register(new ResourceLocation(Constants.MOD_ID, "pirate_shoes"), ModItems.pirateBoots_2);

        registry.register(new ResourceLocation(Constants.MOD_ID, "plate_armor_helmet"), ModItems.plateArmorHelmet);
        registry.register(new ResourceLocation(Constants.MOD_ID, "plate_armor_chest"), ModItems.plateArmorChest);
        registry.register(new ResourceLocation(Constants.MOD_ID, "plate_armor_legs"), ModItems.plateArmorLegs);
        registry.register(new ResourceLocation(Constants.MOD_ID, "plate_armor_boots"), ModItems.plateArmorBoots);


        registry.register(new ResourceLocation(Constants.MOD_ID, "santa_hat"), ModItems.santaHat);
        registry.register(new ResourceLocation(Constants.MOD_ID, IRON_GATE), ModItems.irongate);
        registry.register(new ResourceLocation(Constants.MOD_ID, WOODEN_GATE), ModItems.woodgate);
        registry.register(new ResourceLocation(Constants.MOD_ID, "colony_banner"), ModItems.flagBanner);


        registry.register(new ResourceLocation(Constants.MOD_ID, "sifter_mesh_string"), ModItems.sifterMeshString);
        registry.register(new ResourceLocation(Constants.MOD_ID, "sifter_mesh_flint"), ModItems.sifterMeshFlint);
        registry.register(new ResourceLocation(Constants.MOD_ID, "sifter_mesh_iron"), ModItems.sifterMeshIron);
        registry.register(new ResourceLocation(Constants.MOD_ID, "sifter_mesh_diamond"), ModItems.sifterMeshDiamond);

        registry.register(new ResourceLocation(Constants.MOD_ID, "magicpotion"), ModItems.magicpotion);
        registry.register(new ResourceLocation(Constants.MOD_ID, "build_goggles"), ModItems.buildGoggles);

        registry.register(new ResourceLocation(Constants.MOD_ID, "barbarianegg"), new SpawnEggItem(ModEntities.BARBARIAN,
          PRIMARY_COLOR_BARBARIAN,
          SECONDARY_COLOR_BARBARIAN,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));
        registry.register(new ResourceLocation(Constants.MOD_ID, "barbarcheregg"), new SpawnEggItem(ModEntities.ARCHERBARBARIAN,
          PRIMARY_COLOR_BARBARIAN,
          SECONDARY_COLOR_BARBARIAN,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));
        registry.register(new ResourceLocation(Constants.MOD_ID, "barbchiefegg"), new SpawnEggItem(ModEntities.CHIEFBARBARIAN,
          PRIMARY_COLOR_BARBARIAN,
          SECONDARY_COLOR_BARBARIAN,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));

        registry.register(new ResourceLocation(Constants.MOD_ID, "pirateegg"), new SpawnEggItem(ModEntities.PIRATE,
          PRIMARY_COLOR_PIRATE,
          SECONDARY_COLOR_PIRATE,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));
        registry.register(new ResourceLocation(Constants.MOD_ID, "piratearcheregg"), new SpawnEggItem(ModEntities.ARCHERPIRATE,
          PRIMARY_COLOR_PIRATE,
          SECONDARY_COLOR_PIRATE,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));
        registry.register(new ResourceLocation(Constants.MOD_ID, "piratecaptainegg"), new SpawnEggItem(ModEntities.CHIEFPIRATE,
          PRIMARY_COLOR_PIRATE,
          SECONDARY_COLOR_PIRATE,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));

        registry.register(new ResourceLocation(Constants.MOD_ID, "mummyegg"), new SpawnEggItem(ModEntities.MUMMY, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));
        registry.register(new ResourceLocation(Constants.MOD_ID, "mummyarcheregg"), new SpawnEggItem(ModEntities.ARCHERMUMMY,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));
        registry.register(new ResourceLocation(Constants.MOD_ID, "pharaoegg"), new SpawnEggItem(ModEntities.PHARAO, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));

        registry.register(new ResourceLocation(Constants.MOD_ID, "shieldmaidenegg"), new SpawnEggItem(ModEntities.SHIELDMAIDEN,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));
        registry.register(new ResourceLocation(Constants.MOD_ID, "norsemenarcheregg"), new SpawnEggItem(ModEntities.NORSEMEN_ARCHER,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));
        registry.register(new ResourceLocation(Constants.MOD_ID, "norsemenchiefegg"), new SpawnEggItem(ModEntities.NORSEMEN_CHIEF,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));

        registry.register(new ResourceLocation(Constants.MOD_ID, "amazonegg"), new SpawnEggItem(ModEntities.AMAZON, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));
        registry.register(new ResourceLocation(Constants.MOD_ID, "amazonspearmanegg"), new SpawnEggItem(ModEntities.AMAZONSPEARMAN,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          new Item.Properties().tab(ModCreativeTabs.MINECOLONIES)));
        registry.register(new ResourceLocation(Constants.MOD_ID, "amazonchiefegg"), new SpawnEggItem(ModEntities.AMAZONCHIEF,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));

        registry.register(new ResourceLocation(Constants.MOD_ID, "mercegg"), new SpawnEggItem(ModEntities.MERCENARY,
          PRIMARY_COLOR_MERC,
          SECONDARY_COLOR_MERC,
          (new Item.Properties()).tab(ModCreativeTabs.MINECOLONIES)));
    }
}
