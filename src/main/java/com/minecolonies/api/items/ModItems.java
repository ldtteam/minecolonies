package com.minecolonies.api.items;

import com.ldtteam.blockui.Color;
import com.minecolonies.api.blocks.AbstractColonyBlock;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.blocks.BlockMinecoloniesCrop;
import com.minecolonies.core.items.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
public final class ModItems
{
    public static final DeferredRegister.Items DEFERRED_REGISTER = DeferredRegister.createItems(Constants.MOD_ID);

    /**
     * Building blocks.
     */
    public static final DeferredItem<BlockItem> blockItemHutTownHall      = registerBlock(ModBlocks.blockHutTownHall, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutCitizen       = registerBlock(ModBlocks.blockHutCitizen, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutMiner         = registerBlock(ModBlocks.blockHutMiner, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutLumberjack    = registerBlock(ModBlocks.blockHutLumberjack, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutBaker         = registerBlock(ModBlocks.blockHutBaker, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutBuilder       = registerBlock(ModBlocks.blockHutBuilder, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutDeliveryman   = registerBlock(ModBlocks.blockHutDeliveryman, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutBlacksmith    = registerBlock(ModBlocks.blockHutBlacksmith, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutStonemason    = registerBlock(ModBlocks.blockHutStonemason, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutFarmer        = registerBlock(ModBlocks.blockHutFarmer, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutFisherman     = registerBlock(ModBlocks.blockHutFisherman, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutGuardTower    = registerBlock(ModBlocks.blockHutGuardTower, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutWareHouse     = registerBlock(ModBlocks.blockHutWareHouse, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutShepherd      = registerBlock(ModBlocks.blockHutShepherd, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutCowboy        = registerBlock(ModBlocks.blockHutCowboy, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutSwineHerder   = registerBlock(ModBlocks.blockHutSwineHerder, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutChickenHerder = registerBlock(ModBlocks.blockHutChickenHerder, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutBarracks      = registerBlock(ModBlocks.blockHutBarracks, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutBarracksTower = registerBlock(ModBlocks.blockHutBarracksTower, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutCook          = registerBlock(ModBlocks.blockHutCook, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutSmeltery      = registerBlock(ModBlocks.blockHutSmeltery, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutComposter     = registerBlock(ModBlocks.blockHutComposter, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutLibrary       = registerBlock(ModBlocks.blockHutLibrary, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutArchery       = registerBlock(ModBlocks.blockHutArchery, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutCombatAcademy = registerBlock(ModBlocks.blockHutCombatAcademy, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutSawmill       = registerBlock(ModBlocks.blockHutSawmill, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutStoneSmeltery = registerBlock(ModBlocks.blockHutStoneSmeltery, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutCrusher       = registerBlock(ModBlocks.blockHutCrusher, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutSifter        = registerBlock(ModBlocks.blockHutSifter, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutFlorist       = registerBlock(ModBlocks.blockHutFlorist, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutEnchanter     = registerBlock(ModBlocks.blockHutEnchanter, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutUniversity    = registerBlock(ModBlocks.blockHutUniversity, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutHospital      = registerBlock(ModBlocks.blockHutHospital, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutSchool        = registerBlock(ModBlocks.blockHutSchool, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutGlassblower   = registerBlock(ModBlocks.blockHutGlassblower, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutDyer          = registerBlock(ModBlocks.blockHutDyer, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutFletcher      = registerBlock(ModBlocks.blockHutFletcher, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutMechanic      = registerBlock(ModBlocks.blockHutMechanic, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutPlantation    = registerBlock(ModBlocks.blockHutPlantation, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutTavern        = registerBlock(ModBlocks.blockHutTavern, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutRabbitHutch   = registerBlock(ModBlocks.blockHutRabbitHutch, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutConcreteMixer = registerBlock(ModBlocks.blockHutConcreteMixer, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutBeekeeper     = registerBlock(ModBlocks.blockHutBeekeeper, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutMysticalSite  = registerBlock(ModBlocks.blockHutMysticalSite, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutGraveyard     = registerBlock(ModBlocks.blockHutGraveyard, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutNetherWorker  = registerBlock(ModBlocks.blockHutNetherWorker, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutSimpleQuarry  = registerBlock(ModBlocks.blockHutSimpleQuarry, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutMediumQuarry  = registerBlock(ModBlocks.blockHutMediumQuarry, new Item.Properties());
    //public static final DeferredItem<BlockItem> blockItemHutLargeQuarry  = registerBlockHut(ModBlocks.blockHutLargeQuarry>, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutAlchemist     = registerBlock(ModBlocks.blockHutAlchemist, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutKitchen       = registerBlock(ModBlocks.blockHutKitchen, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemHutGateHouse     = registerBlock(ModBlocks.blockHutGateHouse, new Item.Properties());

    /**
     * Postbox & Stash.
     */
    public static final DeferredItem<BlockItem> blockItemPostBox = registerBlock(ModBlocks.blockPostBox, new Item.Properties());
    public static final DeferredItem<BlockItem> blockItemStash   = registerBlock(ModBlocks.blockStash, new Item.Properties());

    /**
     * Crop blocks.
     */
    public static final DeferredItem<ItemCrop> blockItemBellPepper      = registerCropBlock(ModBlocks.blockBellPepper, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemCabbage         = registerCropBlock(ModBlocks.blockCabbage, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemChickpea        = registerCropBlock(ModBlocks.blockChickpea, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemDurum           = registerCropBlock(ModBlocks.blockDurum, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemEggplant        = registerCropBlock(ModBlocks.blockEggplant, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemGarlic          = registerCropBlock(ModBlocks.blockGarlic, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemOnion           = registerCropBlock(ModBlocks.blockOnion, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemSoyBean         = registerCropBlock(ModBlocks.blockSoyBean, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemTomato          = registerCropBlock(ModBlocks.blockTomato, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemButternutSquash = registerCropBlock(ModBlocks.blockButternutSquash, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemCorn            = registerCropBlock(ModBlocks.blockCorn, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemMint            = registerCropBlock(ModBlocks.blockMint, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemNetherPepper    = registerCropBlock(ModBlocks.blockNetherPepper, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemPeas            = registerCropBlock(ModBlocks.blockPeas, new Item.Properties());
    public static final DeferredItem<ItemCrop> blockItemRice            = registerCropBlock(ModBlocks.blockRice, new Item.Properties());

    /**
     * Utility blocks.
     */
    public static final DeferredItem<BlockItem>            blockItemConstructionTape      = registerBlock(ModBlocks.blockConstructionTape, BlockItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemRack                  = registerBlock(ModBlocks.blockRack, BlockItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemGrave                 = registerBlock(ModBlocks.blockGrave, BlockItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemNamedGrave            = registerBlock(ModBlocks.blockNamedGrave, BlockItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemWayPoint              = registerBlock(ModBlocks.blockWayPoint, BlockItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemBarrel                = registerBlock(ModBlocks.blockBarrel, BlockItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemDecorationPlaceholder =
        registerBlock(ModBlocks.blockDecorationPlaceholder, BlockItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemScarecrow             = registerBlock(ModBlocks.blockScarecrow, BlockItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemPlantationField       = registerBlock(ModBlocks.blockPlantationField, BlockItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemCompostedDirt         = registerBlock(ModBlocks.blockCompostedDirt, BlockItem::new, new Item.Properties());
    public static final DeferredItem<ItemColonyFlagBanner> blockItemFlagBanner            = registerItem("colony_banner",
        p -> new ItemColonyFlagBanner(ModBlocks.blockColonyBanner.get(), ModBlocks.blockColonyWallBanner.get(), p),
        new Item.Properties().stacksTo(1));
    public static final DeferredItem<ItemGate>             blockItemIronGate              = registerBlock(ModBlocks.blockIronGate, ItemGate::new, new Item.Properties());
    public static final DeferredItem<ItemGate>             blockItemWoodenGate            = registerBlock(ModBlocks.blockWoodGate, ItemGate::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemFarmland              = registerBlock(ModBlocks.blockFarmland, BlockItem::new, new Item.Properties());
    public static final DeferredItem<BlockItem>            blockItemFloodedFarmland       = registerBlock(ModBlocks.blockFloodedFarmland, BlockItem::new, new Item.Properties());
    public static final DeferredItem<ItemColonySign>       blockItemColonySign            = registerBlock(ModBlocks.blockColonySign, ItemColonySign::new, new Item.Properties());

    /**
     * Colony tools.
     */
    public static final DeferredItem<ItemClipboard>         clipboard         = registerItem("clipboard", ItemClipboard::new, new Item.Properties());
    public static final DeferredItem<ItemResourceScroll>    resourceScroll    = registerItem("resourcescroll", ItemResourceScroll::new, new Item.Properties());
    public static final DeferredItem<ItemQuestLog>          questLog          = registerItem("questlog", ItemQuestLog::new, new Item.Properties());
    public static final DeferredItem<ItemColonyMap>         colonyMap         = registerItem("colonymap", ItemColonyMap::new, new Item.Properties());
    public static final DeferredItem<ItemBannerRallyGuards> bannerRallyGuards = registerItem("banner_rally_guards", ItemBannerRallyGuards::new, new Item.Properties());
    public static final DeferredItem<ItemBuildGoggles>      buildGoggles      = registerItem("build_goggles", ItemBuildGoggles::new, new Item.Properties());
    public static final DeferredItem<ItemScanAnalyzer>      scanAnalyzer      = registerItem("scan_analyzer", ItemScanAnalyzer::new, new Item.Properties());

    public static final DeferredItem<ItemAssistantHammer> assistantHammerGold    =
        registerItem("assistanthammer_gold", p -> new ItemAssistantHammer(p, 1), new Item.Properties().durability(200));
    public static final DeferredItem<ItemAssistantHammer> assistantHammerIron    =
        registerItem("assistanthammer_iron", p -> new ItemAssistantHammer(p, 2), new Item.Properties().durability(400));
    public static final DeferredItem<ItemAssistantHammer> assistantHammerDiamond =
        registerItem("assistanthammer_diamond", p -> new ItemAssistantHammer(p, 3), new Item.Properties().durability(1000));

    public static final DeferredItem<ItemScepterLumberjack> scepterLumberjack = registerItem("scepterlumberjack", ItemScepterLumberjack::new, new Item.Properties());
    public static final DeferredItem<ItemScepterPermission> permTool          = registerItem("scepterpermission", ItemScepterPermission::new, new Item.Properties());
    public static final DeferredItem<ItemScepterGuard>      scepterGuard      = registerItem("scepterguard", ItemScepterGuard::new, new Item.Properties());
    public static final DeferredItem<ItemScepterBeekeeper>  scepterBeekeeper  = registerItem("scepterbeekeeper", ItemScepterBeekeeper::new, new Item.Properties());

    /**
     * Supply camps.
     */
    public static final DeferredItem<ItemSupplyChestDeployer> supplyChest = registerItem("supplychestdeployer", ItemSupplyChestDeployer::new, new Item.Properties());
    public static final DeferredItem<ItemSupplyCampDeployer>  supplyCamp  = registerItem("supplycampdeployer", ItemSupplyCampDeployer::new, new Item.Properties());

    /**
     * Weapons.
     */
    public static final DeferredItem<ItemChiefSword>    chiefSword    = registerItem("chiefsword", ItemChiefSword::new, new Item.Properties().durability(1500));
    public static final DeferredItem<ItemIronScimitar>  scimitar      = registerItem("iron_scimitar", ItemIronScimitar::new, new Item.Properties().durability(250));
    public static final DeferredItem<ItemPharaoScepter> pharaoScepter = registerItem("pharaoscepter", ItemPharaoScepter::new, new Item.Properties().durability(400));
    public static final DeferredItem<ItemFireArrow>     fireArrow     = registerItem("firearrow", ItemFireArrow::new, new Item.Properties());
    public static final DeferredItem<ItemSpear>         spear         = registerItem("spear", ItemSpear::new, new Item.Properties());

    /**
     * Scrolls.
     */
    public static final DeferredItem<ItemScrollColonyTP>     scrollColonyTP     = registerItem("scroll_tp", ItemScrollColonyTP::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<ItemScrollColonyAreaTP> scrollColonyAreaTP = registerItem("scroll_area_tp", ItemScrollColonyAreaTP::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<ItemScrollBuff>         scrollBuff         = registerItem("scroll_buff", ItemScrollBuff::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<ItemScrollGuardHelp>    scrollGuardHelp    = registerItem("scroll_guard_help", ItemScrollGuardHelp::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<ItemScrollHighlight>    scrollHighLight    = registerItem("scroll_highlight", ItemScrollHighlight::new, new Item.Properties().stacksTo(16));

    /**
     * Armors.
     */
    public static final DeferredItem<ArmorItem> santaHat =
        registerItem("santa_hat", p -> new ArmorItem(ModArmorMaterials.SANTA_HAT, ArmorItem.Type.HELMET, p), new Item.Properties());

    public static final DeferredItem<ArmorItem> pirateHelmet_1 =
        registerItem("pirate_hat", p -> new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.HELMET, p), new Item.Properties().durability(350));
    public static final DeferredItem<ArmorItem> pirateChest_1  =
        registerItem("pirate_top", p -> new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.CHESTPLATE, p), new Item.Properties().durability(550));
    public static final DeferredItem<ArmorItem> pirateLegs_1   =
        registerItem("pirate_leggins", p -> new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.LEGGINGS, p), new Item.Properties().durability(500));
    public static final DeferredItem<ArmorItem> pirateBoots_1  =
        registerItem("pirate_boots", p -> new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.BOOTS, p), new Item.Properties().durability(400));

    public static final DeferredItem<ArmorItem> pirateHelmet_2 =
        registerItem("pirate_cap", p -> new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.HELMET, p), new Item.Properties().durability(200));
    public static final DeferredItem<ArmorItem> pirateChest_2  =
        registerItem("pirate_chest", p -> new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.CHESTPLATE, p), new Item.Properties().durability(350));
    public static final DeferredItem<ArmorItem> pirateLegs_2   =
        registerItem("pirate_legs", p -> new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.LEGGINGS, p), new Item.Properties().durability(300));
    public static final DeferredItem<ArmorItem> pirateBoots_2  =
        registerItem("pirate_shoes", p -> new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.BOOTS, p), new Item.Properties().durability(250));

    public static final DeferredItem<ArmorItem> plateArmorHelmet =
        registerItem("plate_armor_helmet", p -> new ArmorItem(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.HELMET, p), new Item.Properties().durability(350));
    public static final DeferredItem<ArmorItem> plateArmorChest  =
        registerItem("plate_armor_chest", p -> new ArmorItem(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.CHESTPLATE, p), new Item.Properties().durability(500));
    public static final DeferredItem<ArmorItem> plateArmorLegs   =
        registerItem("plate_armor_legs", p -> new ArmorItem(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.LEGGINGS, p), new Item.Properties().durability(450));
    public static final DeferredItem<ArmorItem> plateArmorBoots  =
        registerItem("plate_armor_boots", p -> new ArmorItem(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.BOOTS, p), new Item.Properties().durability(400));

    /**
     * Spawn eggs.
     */
    public static final DeferredItem<SpawnEggItem> campBarbarianSpawnEgg           =
        registerSpawnEgg("barbarianegg", ModEntities.CAMP_BARBARIAN, getColorSafe("orange"), getColorSafe("black"));
    public static final DeferredItem<SpawnEggItem> campBarbarianArcherSpawnEgg     =
        registerSpawnEgg("barbarcheregg", ModEntities.CAMP_ARCHERBARBARIAN, getColorSafe("orange"), getColorSafe("green"));
    public static final DeferredItem<SpawnEggItem> campBarbarianChiefSpawnEgg      =
        registerSpawnEgg("barbchiefegg", ModEntities.CAMP_CHIEFBARBARIAN, getColorSafe("orange"), getColorSafe("yellow"));
    public static final DeferredItem<SpawnEggItem> campPirateSpawnEgg              =
        registerSpawnEgg("pirateegg", ModEntities.CAMP_PIRATE, getColorSafe("red"), getColorSafe("white"));
    public static final DeferredItem<SpawnEggItem> campPirateArcherSpawnEgg        =
        registerSpawnEgg("piratearcheregg", ModEntities.CAMP_ARCHERPIRATE, getColorSafe("red"), getColorSafe("green"));
    public static final DeferredItem<SpawnEggItem> campPirateChiefSpawnEgg         =
        registerSpawnEgg("piratecaptainegg", ModEntities.CAMP_CHIEFPIRATE, getColorSafe("red"), getColorSafe("yellow"));
    public static final DeferredItem<SpawnEggItem> campMummySpawnEgg               =
        registerSpawnEgg("mummyegg", ModEntities.CAMP_MUMMY, getColorSafe("yellow"), getColorSafe("white"));
    public static final DeferredItem<SpawnEggItem> campMummyArcherSpawnEgg         =
        registerSpawnEgg("mummyarcheregg", ModEntities.CAMP_ARCHERMUMMY, getColorSafe("yellow"), getColorSafe("green"));
    public static final DeferredItem<SpawnEggItem> campPharaoSpawnEgg              =
        registerSpawnEgg("pharaoegg", ModEntities.CAMP_PHARAO, getColorSafe("yellow"), getColorSafe("yellow"));
    public static final DeferredItem<SpawnEggItem> campShieldmaidenSpawnEgg        =
        registerSpawnEgg("shieldmaidenegg", ModEntities.CAMP_SHIELDMAIDEN, getColorSafe("black"), getColorSafe("white"));
    public static final DeferredItem<SpawnEggItem> campNorsemenArcherSpawnEgg      =
        registerSpawnEgg("norsemenarcheregg", ModEntities.CAMP_NORSEMEN_ARCHER, getColorSafe("black"), getColorSafe("green"));
    public static final DeferredItem<SpawnEggItem> campNorsemenChiefSpawnEgg       =
        registerSpawnEgg("norsemenchiefegg", ModEntities.CAMP_NORSEMEN_CHIEF, getColorSafe("black"), getColorSafe("yellow"));
    public static final DeferredItem<SpawnEggItem> campAmazonSpawnEgg              =
        registerSpawnEgg("amazonegg", ModEntities.CAMP_AMAZON, getColorSafe("green"), getColorSafe("white"));
    public static final DeferredItem<SpawnEggItem> campAmazonSpearmanSpawnEgg      =
        registerSpawnEgg("amazonspearmanegg", ModEntities.CAMP_AMAZONSPEARMAN, getColorSafe("green"), getColorSafe("green"));
    public static final DeferredItem<SpawnEggItem> campAmazonChiefSpawnEgg         =
        registerSpawnEgg("amazonchiefegg", ModEntities.CAMP_AMAZONCHIEF, getColorSafe("green"), getColorSafe("yellow"));
    public static final DeferredItem<SpawnEggItem> campDrownedPirateSpawnEgg       =
        registerSpawnEgg("drownedpirateegg", ModEntities.CAMP_DROWNED_PIRATE, getColorSafe("blue"), getColorSafe("white"));
    public static final DeferredItem<SpawnEggItem> campDrownedPirateArcherSpawnEgg =
        registerSpawnEgg("drownedpiratearcheregg", ModEntities.CAMP_DROWNED_ARCHERPIRATE, getColorSafe("blue"), getColorSafe("green"));
    public static final DeferredItem<SpawnEggItem> campDrownedPirateChiefSpawnEgg  =
        registerSpawnEgg("drownedpiratecaptainegg", ModEntities.CAMP_DROWNED_CHIEFPIRATE, getColorSafe("blue"), getColorSafe("yellow"));

    /**
     * Other items.
     */
    public static final DeferredItem<ItemAncientTome>    ancientTome    = registerItem("ancienttome", ItemAncientTome::new, new Item.Properties());
    public static final DeferredItem<ItemCompost>        compost        = registerItem("compost", ItemCompost::new, new Item.Properties());
    public static final DeferredItem<Item>               mistletoe      = registerItem("mistletoe", Item::new, new Item.Properties());
    public static final DeferredItem<Item>               magicPotion    = registerItem("magicpotion", Item::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<ItemAdventureToken> adventureToken = registerItem("adventure_token", ItemAdventureToken::new, new Item.Properties());

    public static final DeferredItem<Item> sifterMeshString  = registerItem("sifter_mesh_string", Item::new, new Item.Properties().durability(500).setNoRepair());
    public static final DeferredItem<Item> sifterMeshFlint   = registerItem("sifter_mesh_flint", Item::new, new Item.Properties().durability(1000).setNoRepair());
    public static final DeferredItem<Item> sifterMeshIron    = registerItem("sifter_mesh_iron", Item::new, new Item.Properties().durability(1500).setNoRepair());
    public static final DeferredItem<Item> sifterMeshDiamond = registerItem("sifter_mesh_diamond", Item::new, new Item.Properties().durability(2000).setNoRepair());

    public static final DeferredItem<ItemLargeBottle> largeEmptyBottle   = registerItem("large_empty_bottle", ItemLargeBottle::new, new Item.Properties());
    public static final DeferredItem<ItemLargeBottle> largeMilkBottle    = registerItem("large_water_bottle", ItemLargeBottle::new, new Item.Properties());
    public static final DeferredItem<ItemLargeBottle> largeWaterBottle   = registerItem("large_milk_bottle", ItemLargeBottle::new, new Item.Properties());
    public static final DeferredItem<ItemLargeBottle> largeSoyMilkBottle = registerItem("large_soy_milk_bottle", ItemLargeBottle::new, new Item.Properties());

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModItems()
    {
    }

    private static DeferredItem<SpawnEggItem> registerSpawnEgg(final String id, final EntityType<? extends Mob> entityType, final int backgroundColor, final int highlightColor)
    {
        return registerItem(id, p -> new DeferredSpawnEggItem(() -> entityType, backgroundColor, highlightColor, p), new Item.Properties());
    }

    private static <T extends Item> DeferredItem<T> registerItem(final String id, final Function<Item.Properties, T> item, final Item.Properties properties)
    {
        return DEFERRED_REGISTER.registerItem(id, item, properties);
    }

    private static DeferredItem<ItemCrop> registerCropBlock(final DeferredBlock<BlockMinecoloniesCrop> block, final Item.Properties properties)
    {
        return registerBlock(block, (b, p) -> new ItemCrop(b, p, block.value().getPreferredBiome()), properties);
    }

    private static <B extends Block, T extends BlockItem> DeferredItem<T> registerBlock(
        final DeferredBlock<B> block,
        final BiFunction<B, Item.Properties, T> createBlockItem,
        final Item.Properties properties)
    {
        return registerItem(block.unwrapKey().orElseThrow().location().getPath(), (p) -> createBlockItem.apply(block.value(), p), properties);
    }

    private static DeferredItem<BlockItem> registerBlock(final DeferredBlock<? extends AbstractColonyBlock> block, final Item.Properties properties)
    {
        return registerBlock(block, BlockItem::new, properties);
    }

    private static int getColorSafe(final String name)
    {
        final Integer byName = Color.getByName(name);
        if (byName == null)
        {
            Log.getLogger().error("Could not fetch color by name '{}', defaulting to black.", name);
            return 0;
        }
        return byName;
    }
}
