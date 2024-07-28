package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.RegisterEvent;

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
        if (event.getRegistryKey().equals(Registries.ITEM))
        {
            ModItemsInitializer.init(event.getRegistry(Registries.ITEM));
        }
    }

    /**
     * Initates all the blocks. At the correct time.
     *
     * @param registry the registry.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final Registry<Item> registry)
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
        ModItems.questLog = new ItemQuestLog(new Item.Properties());

        ModItems.breadDough = new ItemBreadDough(new Item.Properties());
        ModItems.cookieDough = new ItemCookieDough(new Item.Properties());
        ModItems.cakeBatter = new ItemCakeBatter(new Item.Properties());
        ModItems.rawPumpkinPie = new ItemRawPumpkinPie(new Item.Properties());

        ModItems.milkyBread = new ItemMilkyBread(new Item.Properties());
        ModItems.sugaryBread = new ItemSugaryBread(new Item.Properties());
        ModItems.goldenBread = new ItemGoldenBread(new Item.Properties());
        ModItems.chorusBread = new ItemChorusBread(new Item.Properties());

        ModItems.adventureToken = new ItemAdventureToken(new Item.Properties());

        ModItems.scrollColonyTP = new ItemScrollColonyTP(new Item.Properties().stacksTo(16));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "scroll_tp"), ModItems.scrollColonyTP);

        ModItems.scrollColonyAreaTP = new ItemScrollColonyAreaTP(new Item.Properties().stacksTo(16));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "scroll_area_tp"), ModItems.scrollColonyAreaTP);

        ModItems.scrollBuff = new ItemScrollBuff(new Item.Properties().stacksTo(16));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "scroll_buff"), ModItems.scrollBuff);

        ModItems.scrollGuardHelp = new ItemScrollGuardHelp(new Item.Properties().stacksTo(16));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "scroll_guard_help"), ModItems.scrollGuardHelp);

        ModItems.scrollHighLight = new ItemScrollHighlight(new Item.Properties().stacksTo(16));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "scroll_highlight"), ModItems.scrollHighLight);

        ModItems.santaHat = new ItemSantaHead("santa_hat", ItemSantaHead.SANTA_HAT, ArmorItem.Type.HELMET, new Item.Properties());
        ModItems.irongate = new ItemGate(IRON_GATE, ModBlocks.blockIronGate, new Item.Properties());
        ModItems.woodgate = new ItemGate(WOODEN_GATE, ModBlocks.blockWoodenGate, new Item.Properties());

        ModItems.flagBanner = new ItemColonyFlagBanner("colony_banner", new Item.Properties());
        ModItems.pirateHelmet_1 = new ItemPirateGear("pirate_hat", ItemPirateGear.PIRATE_ARMOR_1, ArmorItem.Type.HELMET, new Item.Properties());
        ModItems.pirateChest_1 = new ItemPirateGear("pirate_top", ItemPirateGear.PIRATE_ARMOR_1, ArmorItem.Type.CHESTPLATE, new Item.Properties());
        ModItems.pirateLegs_1 = new ItemPirateGear("pirate_leggins", ItemPirateGear.PIRATE_ARMOR_1, ArmorItem.Type.LEGGINGS, new Item.Properties());
        ModItems.pirateBoots_1 = new ItemPirateGear("pirate_boots", ItemPirateGear.PIRATE_ARMOR_1, ArmorItem.Type.BOOTS, new Item.Properties());

        ModItems.pirateHelmet_2 = new ItemPirateGear("pirate_cap", ItemPirateGear.PIRATE_ARMOR_2, ArmorItem.Type.HELMET, new Item.Properties());
        ModItems.pirateChest_2 = new ItemPirateGear("pirate_chest", ItemPirateGear.PIRATE_ARMOR_2, ArmorItem.Type.CHESTPLATE, new Item.Properties());
        ModItems.pirateLegs_2 = new ItemPirateGear("pirate_legs", ItemPirateGear.PIRATE_ARMOR_2, ArmorItem.Type.LEGGINGS, new Item.Properties());
        ModItems.pirateBoots_2 = new ItemPirateGear("pirate_shoes", ItemPirateGear.PIRATE_ARMOR_2, ArmorItem.Type.BOOTS, new Item.Properties());

        ModItems.plateArmorHelmet = new ItemPlateArmor("plate_armor_helmet", ItemPlateArmor.PLATE_ARMOR, ArmorItem.Type.HELMET, new Item.Properties());
        ModItems.plateArmorChest = new ItemPlateArmor("plate_armor_chest", ItemPlateArmor.PLATE_ARMOR, ArmorItem.Type.CHESTPLATE, new Item.Properties());
        ModItems.plateArmorLegs = new ItemPlateArmor("plate_armor_legs", ItemPlateArmor.PLATE_ARMOR, ArmorItem.Type.LEGGINGS, new Item.Properties());
        ModItems.plateArmorBoots = new ItemPlateArmor("plate_armor_boots", ItemPlateArmor.PLATE_ARMOR, ArmorItem.Type.BOOTS, new Item.Properties());

        ModItems.sifterMeshString = new ItemSifterMesh("sifter_mesh_string", new Item.Properties().durability(500).setNoRepair());
        ModItems.sifterMeshFlint = new ItemSifterMesh("sifter_mesh_flint", new Item.Properties().durability(1000).setNoRepair());
        ModItems.sifterMeshIron = new ItemSifterMesh("sifter_mesh_iron", new Item.Properties().durability(1500).setNoRepair());
        ModItems.sifterMeshDiamond = new ItemSifterMesh("sifter_mesh_diamond", new Item.Properties().durability(2000).setNoRepair());

        ModItems.magicpotion = new ItemMagicPotion("magicpotion", new Item.Properties());
        ModItems.buildGoggles = new ItemBuildGoggles("build_goggles", new Item.Properties());
        ModItems.scanAnalyzer = new ItemScanAnalyzer("scan_analyzer", new Item.Properties());

        // Tier 1 Food Items
        ModItems.cheddar_cheese = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).build()), ModJobs.CHEF_ID.getPath(), 1);
        ModItems.feta_cheese = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).build()), ModJobs.CHEF_ID.getPath(), 1);
        ModItems.cooked_rice = new ItemBowlFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).build()), ModJobs.CHEF_ID.getPath(), 1);
        ModItems.tofu = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).build()), ModJobs.CHEF_ID.getPath(), 1);
        ModItems.flatbread = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(4).saturationMod(0.6F).build()), ModJobs.BAKER_ID.getPath(), 1);

        // Tier 2 Food Items
        ModItems.manchet_bread = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0F).build()), ModJobs.BAKER_ID.getPath(), 2);
        ModItems.lembas_scone = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0F).build()), ModJobs.BAKER_ID.getPath(), 2);
        ModItems.muffin = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0F).build()), ModJobs.BAKER_ID.getPath(), 2);
        ModItems.pottage = new ItemBowlFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0F).build()), ModJobs.CHEF_ID.getPath(), 2);
        ModItems.pasta_plain = new ItemBowlFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0F).build()), ModJobs.CHEF_ID.getPath(), 2);

        // Tier 3 Food items
        ModItems.hand_pie = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(8).saturationMod(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3);

        // Cold Biomes
        // Tier 2
        ModItems.cabochis = new ItemBowlFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0F).build()), ModJobs.CHEF_ID.getPath(), 2);
        // Tier 3
        ModItems.lamb_stew = new ItemBowlFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(8).saturationMod(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3);

        // Hot Humid Biomes
        // Tier 2
        ModItems.rice_ball = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0F).build()), ModJobs.CHEF_ID.getPath(), 2);
        // Tier 3
        ModItems.sushi_roll = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(8).saturationMod(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3);

        // Temperate Biomes
        // Tier 2
        ModItems.pasta_tomato = new ItemBowlFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0F).build()), ModJobs.CHEF_ID.getPath(), 2);
        // Tier 3
        ModItems.eggplant_dolma = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(8).saturationMod(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3);
        ModItems.stuffed_pita = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(8).saturationMod(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3);

        // Hot Dry Biomes
        // Tier 2
        ModItems.pepper_hummus = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0F).build()), ModJobs.CHEF_ID.getPath(), 2);
        // Tier 3
        ModItems.pita_hummus = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(8).saturationMod(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3);

        // Require trading
        // Tier 2
        ModItems.congee = new ItemBowlFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(6).saturationMod(1.0F).build()), ModJobs.CHEF_ID.getPath(), 2);
        // Tier 3
        ModItems.stew_trencher = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(8).saturationMod(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3);
        ModItems.stuffed_pepper = new ItemFood((new Item.Properties()).food(new FoodProperties.Builder().nutrition(8).saturationMod(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3);

        // Just dough
        ModItems.muffin_dough = new Item((new Item.Properties()));
        ModItems.manchet_dough = new Item((new Item.Properties()));
        ModItems.raw_noodle = new Item((new Item.Properties()));
        ModItems.butter = new Item((new Item.Properties()));

        ModItems.large_empty_bottle = new ItemLargeBottle((new Item.Properties()));
        ModItems.large_milk_bottle = new ItemLargeBottle((new Item.Properties().craftRemainder(ModItems.large_empty_bottle)));
        ModItems.large_water_bottle = new ItemLargeBottle((new Item.Properties().craftRemainder(ModItems.large_empty_bottle)));
        ModItems.large_soy_milk_bottle = new ItemLargeBottle((new Item.Properties().craftRemainder(ModItems.large_empty_bottle)));

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "supplychestdeployer"), ModItems.supplyChest);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "scan_analyzer"), ModItems.scanAnalyzer);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "scepterpermission"), ModItems.permTool);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "scepterguard"), ModItems.scepterGuard);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "banner_rally_guards"), ModItems.bannerRallyGuards);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "supplycampdeployer"), ModItems.supplyCamp);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "ancienttome"), ModItems.ancientTome);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, CHIEFSWORD_NAME), ModItems.chiefSword);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "clipboard"), ModItems.clipboard);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "compost"), ModItems.compost);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "resourcescroll"), ModItems.resourceScroll);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, SCIMITAR_NAME), ModItems.scimitar);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "scepterlumberjack"), ModItems.scepterLumberjack);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pharaoscepter"), ModItems.pharaoscepter);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "firearrow"), ModItems.firearrow);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "scepterbeekeeper"), ModItems.scepterBeekeeper);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "mistletoe"), ModItems.mistletoe);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "spear"), ModItems.spear);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "questlog"), ModItems.questLog);

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "bread_dough"), ModItems.breadDough);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "cookie_dough"), ModItems.cookieDough);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "cake_batter"), ModItems.cakeBatter);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "raw_pumpkin_pie"), ModItems.rawPumpkinPie);

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "milky_bread"), ModItems.milkyBread);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "sugary_bread"), ModItems.sugaryBread);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "golden_bread"), ModItems.goldenBread);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "chorus_bread"), ModItems.chorusBread);

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "adventure_token"), ModItems.adventureToken);

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pirate_hat"), ModItems.pirateHelmet_1);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pirate_top"), ModItems.pirateChest_1);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pirate_leggins"), ModItems.pirateLegs_1);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pirate_boots"), ModItems.pirateBoots_1);

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pirate_cap"), ModItems.pirateHelmet_2);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pirate_chest"), ModItems.pirateChest_2);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pirate_legs"), ModItems.pirateLegs_2);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pirate_shoes"), ModItems.pirateBoots_2);

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "plate_armor_helmet"), ModItems.plateArmorHelmet);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "plate_armor_chest"), ModItems.plateArmorChest);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "plate_armor_legs"), ModItems.plateArmorLegs);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "plate_armor_boots"), ModItems.plateArmorBoots);


        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "santa_hat"), ModItems.santaHat);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, IRON_GATE), ModItems.irongate);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, WOODEN_GATE), ModItems.woodgate);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "colony_banner"), ModItems.flagBanner);


        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "sifter_mesh_string"), ModItems.sifterMeshString);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "sifter_mesh_flint"), ModItems.sifterMeshFlint);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "sifter_mesh_iron"), ModItems.sifterMeshIron);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "sifter_mesh_diamond"), ModItems.sifterMeshDiamond);

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "magicpotion"), ModItems.magicpotion);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "build_goggles"), ModItems.buildGoggles);

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "butter"), ModItems.butter);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "cabochis"), ModItems.cabochis);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "cheddar_cheese"), ModItems.cheddar_cheese);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "congee"), ModItems.congee);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "cooked_rice"), ModItems.cooked_rice);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "eggplant_dolma"), ModItems.eggplant_dolma);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "feta_cheese"), ModItems.feta_cheese);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "flatbread"), ModItems.flatbread);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "hand_pie"), ModItems.hand_pie);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "lamb_stew"), ModItems.lamb_stew);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "lembas_scone"), ModItems.lembas_scone);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "manchet_bread"), ModItems.manchet_bread);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "manchet_dough"), ModItems.manchet_dough);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "muffin"), ModItems.muffin);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "muffin_dough"), ModItems.muffin_dough);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pasta_plain"), ModItems.pasta_plain);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pasta_tomato"), ModItems.pasta_tomato);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pepper_hummus"), ModItems.pepper_hummus);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pita_hummus"), ModItems.pita_hummus);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pottage"), ModItems.pottage);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "raw_noodle"), ModItems.raw_noodle);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "rice_ball"), ModItems.rice_ball);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "stew_trencher"), ModItems.stew_trencher);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "stuffed_pepper"), ModItems.stuffed_pepper);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "stuffed_pita"), ModItems.stuffed_pita);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "sushi_roll"), ModItems.sushi_roll);
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "tofu"), ModItems.tofu);

        registry.register(new ResourceLocation(Constants.MOD_ID, "large_empty_bottle"), ModItems.large_empty_bottle);
        registry.register(new ResourceLocation(Constants.MOD_ID, "large_water_bottle"), ModItems.large_water_bottle);
        registry.register(new ResourceLocation(Constants.MOD_ID, "large_milk_bottle"), ModItems.large_milk_bottle);
        registry.register(new ResourceLocation(Constants.MOD_ID, "large_soy_milk_bottle"), ModItems.large_soy_milk_bottle);

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "barbarianegg"), new DeferredSpawnEggItem(() -> ModEntities.BARBARIAN,
          PRIMARY_COLOR_BARBARIAN,
          SECONDARY_COLOR_BARBARIAN,
          (new Item.Properties())));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "barbarcheregg"), new DeferredSpawnEggItem(() -> ModEntities.ARCHERBARBARIAN,
          PRIMARY_COLOR_BARBARIAN,
          SECONDARY_COLOR_BARBARIAN,
          (new Item.Properties())));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "barbchiefegg"), new DeferredSpawnEggItem(() -> ModEntities.CHIEFBARBARIAN,
          PRIMARY_COLOR_BARBARIAN,
          SECONDARY_COLOR_BARBARIAN,
          (new Item.Properties())));

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pirateegg"), new DeferredSpawnEggItem(() -> ModEntities.PIRATE,
          PRIMARY_COLOR_PIRATE,
          SECONDARY_COLOR_PIRATE,
          (new Item.Properties())));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "piratearcheregg"), new DeferredSpawnEggItem(() -> ModEntities.ARCHERPIRATE,
          PRIMARY_COLOR_PIRATE,
          SECONDARY_COLOR_PIRATE,
          (new Item.Properties())));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "piratecaptainegg"), new DeferredSpawnEggItem(() -> ModEntities.CHIEFPIRATE,
          PRIMARY_COLOR_PIRATE,
          SECONDARY_COLOR_PIRATE,
          (new Item.Properties())));

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "mummyegg"), new DeferredSpawnEggItem(() -> ModEntities.MUMMY, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties())));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "mummyarcheregg"), new DeferredSpawnEggItem(() -> ModEntities.ARCHERMUMMY,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          (new Item.Properties())));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "pharaoegg"), new DeferredSpawnEggItem(() -> ModEntities.PHARAO, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties())));

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "shieldmaidenegg"), new DeferredSpawnEggItem(() -> ModEntities.SHIELDMAIDEN,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          (new Item.Properties())));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "norsemenarcheregg"), new DeferredSpawnEggItem(() -> ModEntities.NORSEMEN_ARCHER,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          (new Item.Properties())));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "norsemenchiefegg"), new DeferredSpawnEggItem(() -> ModEntities.NORSEMEN_CHIEF,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          (new Item.Properties())));

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "amazonegg"), new DeferredSpawnEggItem(() -> ModEntities.AMAZON, PRIMARY_COLOR_EG, SECONDARY_COLOR_EG, (new Item.Properties())));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "amazonspearmanegg"), new DeferredSpawnEggItem(() -> ModEntities.AMAZONSPEARMAN,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          new Item.Properties()));
        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "amazonchiefegg"), new DeferredSpawnEggItem(() -> ModEntities.AMAZONCHIEF,
          PRIMARY_COLOR_EG,
          SECONDARY_COLOR_EG,
          (new Item.Properties())));

        Registry.register(registry, new ResourceLocation(Constants.MOD_ID, "mercegg"), new DeferredSpawnEggItem(() -> ModEntities.MERCENARY,
          PRIMARY_COLOR_MERC,
          SECONDARY_COLOR_MERC,
          (new Item.Properties())));
    }
}
