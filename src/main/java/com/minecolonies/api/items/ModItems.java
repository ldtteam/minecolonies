package com.minecolonies.api.items;

import com.ldtteam.blockui.Color;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.interfaces.IMinecoloniesBlock;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings("unused")
public final class ModItems
{
    public static final DeferredRegister.Items DEFERRED_REGISTER = DeferredRegister.createItems(Constants.MOD_ID);

    /**
     * Building blocks.
     */
    public static final ItemBlockHut blockItemHutTownHall      = registerBlock(ModBlocks.blockHutTownHall);
    public static final ItemBlockHut blockItemHutCitizen       = registerBlock(ModBlocks.blockHutCitizen);
    public static final ItemBlockHut blockItemHutMiner         = registerBlock(ModBlocks.blockHutMiner);
    public static final ItemBlockHut blockItemHutLumberjack    = registerBlock(ModBlocks.blockHutLumberjack);
    public static final ItemBlockHut blockItemHutBaker         = registerBlock(ModBlocks.blockHutBaker);
    public static final ItemBlockHut blockItemHutBuilder       = registerBlock(ModBlocks.blockHutBuilder);
    public static final ItemBlockHut blockItemHutDeliveryman   = registerBlock(ModBlocks.blockHutDeliveryman);
    public static final ItemBlockHut blockItemHutBlacksmith    = registerBlock(ModBlocks.blockHutBlacksmith);
    public static final ItemBlockHut blockItemHutStonemason    = registerBlock(ModBlocks.blockHutStonemason);
    public static final ItemBlockHut blockItemHutFarmer        = registerBlock(ModBlocks.blockHutFarmer);
    public static final ItemBlockHut blockItemHutFisherman     = registerBlock(ModBlocks.blockHutFisherman);
    public static final ItemBlockHut blockItemHutGuardTower    = registerBlock(ModBlocks.blockHutGuardTower);
    public static final ItemBlockHut blockItemHutWareHouse     = registerBlock(ModBlocks.blockHutWareHouse);
    public static final ItemBlockHut blockItemHutShepherd      = registerBlock(ModBlocks.blockHutShepherd);
    public static final ItemBlockHut blockItemHutCowboy        = registerBlock(ModBlocks.blockHutCowboy);
    public static final ItemBlockHut blockItemHutSwineHerder   = registerBlock(ModBlocks.blockHutSwineHerder);
    public static final ItemBlockHut blockItemHutChickenHerder = registerBlock(ModBlocks.blockHutChickenHerder);
    public static final ItemBlockHut blockItemHutBarracks      = registerBlock(ModBlocks.blockHutBarracks);
    public static final ItemBlockHut blockItemHutBarracksTower = registerBlock(ModBlocks.blockHutBarracksTower);
    public static final ItemBlockHut blockItemHutCook          = registerBlock(ModBlocks.blockHutCook);
    public static final ItemBlockHut blockItemHutSmeltery      = registerBlock(ModBlocks.blockHutSmeltery);
    public static final ItemBlockHut blockItemHutComposter     = registerBlock(ModBlocks.blockHutComposter);
    public static final ItemBlockHut blockItemHutLibrary       = registerBlock(ModBlocks.blockHutLibrary);
    public static final ItemBlockHut blockItemHutArchery       = registerBlock(ModBlocks.blockHutArchery);
    public static final ItemBlockHut blockItemHutCombatAcademy = registerBlock(ModBlocks.blockHutCombatAcademy);
    public static final ItemBlockHut blockItemHutSawmill       = registerBlock(ModBlocks.blockHutSawmill);
    public static final ItemBlockHut blockItemHutStoneSmeltery = registerBlock(ModBlocks.blockHutStoneSmeltery);
    public static final ItemBlockHut blockItemHutCrusher       = registerBlock(ModBlocks.blockHutCrusher);
    public static final ItemBlockHut blockItemHutSifter        = registerBlock(ModBlocks.blockHutSifter);
    public static final ItemBlockHut blockItemHutFlorist       = registerBlock(ModBlocks.blockHutFlorist);
    public static final ItemBlockHut blockItemHutEnchanter     = registerBlock(ModBlocks.blockHutEnchanter);
    public static final ItemBlockHut blockItemHutUniversity    = registerBlock(ModBlocks.blockHutUniversity);
    public static final ItemBlockHut blockItemHutHospital      = registerBlock(ModBlocks.blockHutHospital);
    public static final ItemBlockHut blockItemHutSchool        = registerBlock(ModBlocks.blockHutSchool);
    public static final ItemBlockHut blockItemHutGlassblower   = registerBlock(ModBlocks.blockHutGlassblower);
    public static final ItemBlockHut blockItemHutDyer          = registerBlock(ModBlocks.blockHutDyer);
    public static final ItemBlockHut blockItemHutFletcher      = registerBlock(ModBlocks.blockHutFletcher);
    public static final ItemBlockHut blockItemHutMechanic      = registerBlock(ModBlocks.blockHutMechanic);
    public static final ItemBlockHut blockItemHutPlantation    = registerBlock(ModBlocks.blockHutPlantation);
    public static final ItemBlockHut blockItemHutTavern        = registerBlock(ModBlocks.blockHutTavern);
    public static final ItemBlockHut blockItemHutRabbitHutch   = registerBlock(ModBlocks.blockHutRabbitHutch);
    public static final ItemBlockHut blockItemHutConcreteMixer = registerBlock(ModBlocks.blockHutConcreteMixer);
    public static final ItemBlockHut blockItemHutBeekeeper     = registerBlock(ModBlocks.blockHutBeekeeper);
    public static final ItemBlockHut blockItemHutMysticalSite  = registerBlock(ModBlocks.blockHutMysticalSite);
    public static final ItemBlockHut blockItemHutGraveyard     = registerBlock(ModBlocks.blockHutGraveyard);
    public static final ItemBlockHut blockItemHutNetherWorker  = registerBlock(ModBlocks.blockHutNetherWorker);
    public static final ItemBlockHut blockItemHutSimpleQuarry  = registerBlock(ModBlocks.blockHutSimpleQuarry);
    public static final ItemBlockHut blockItemHutMediumQuarry  = registerBlock(ModBlocks.blockHutMediumQuarry);
    //public static final ItemBlockHut blockItemHutLargeQuarry  = registerBlock(ModBlocks.blockHutLargeQuarry>);
    public static final ItemBlockHut blockItemHutAlchemist     = registerBlock(ModBlocks.blockHutAlchemist);
    public static final ItemBlockHut blockItemHutKitchen       = registerBlock(ModBlocks.blockHutKitchen);
    public static final ItemBlockHut blockItemHutGateHouse     = registerBlock(ModBlocks.blockHutGateHouse);

    /**
     * Postbox & Stash.
     */
    public static final BlockItem blockItemPostBox = registerBlock(ModBlocks.blockPostBox);
    public static final BlockItem blockItemStash   = registerBlock(ModBlocks.blockStash);

    /**
     * Crop blocks.
     */
    public static final ItemCrop blockItemBellPepper      = registerBlock(ModBlocks.blockBellPepper);
    public static final ItemCrop blockItemCabbage         = registerBlock(ModBlocks.blockCabbage);
    public static final ItemCrop blockItemChickpea        = registerBlock(ModBlocks.blockChickpea);
    public static final ItemCrop blockItemDurum           = registerBlock(ModBlocks.blockDurum);
    public static final ItemCrop blockItemEggplant        = registerBlock(ModBlocks.blockEggplant);
    public static final ItemCrop blockItemGarlic          = registerBlock(ModBlocks.blockGarlic);
    public static final ItemCrop blockItemOnion           = registerBlock(ModBlocks.blockOnion);
    public static final ItemCrop blockItemSoyBean         = registerBlock(ModBlocks.blockSoyBean);
    public static final ItemCrop blockItemTomato          = registerBlock(ModBlocks.blockTomato);
    public static final ItemCrop blockItemButternutSquash = registerBlock(ModBlocks.blockButternutSquash);
    public static final ItemCrop blockItemCorn            = registerBlock(ModBlocks.blockCorn);
    public static final ItemCrop blockItemMint            = registerBlock(ModBlocks.blockMint);
    public static final ItemCrop blockItemNetherPepper    = registerBlock(ModBlocks.blockNetherPepper);
    public static final ItemCrop blockItemPeas            = registerBlock(ModBlocks.blockPeas);
    public static final ItemCrop blockItemRice            = registerBlock(ModBlocks.blockRice);

    /**
     * Utility blocks.
     */
    public static final BlockItem            blockItemConstructionTape      = registerBlock(ModBlocks.blockConstructionTape);
    public static final BlockItem            blockItemRack                  = registerBlock(ModBlocks.blockRack);
    public static final BlockItem            blockItemGrave                 = registerBlock(ModBlocks.blockGrave);
    public static final BlockItem            blockItemNamedGrave            = registerBlock(ModBlocks.blockNamedGrave);
    public static final BlockItem            blockItemWayPoint              = registerBlock(ModBlocks.blockWayPoint);
    public static final BlockItem            blockItemBarrel                = registerBlock(ModBlocks.blockBarrel);
    public static final BlockItem            blockItemDecorationPlaceholder = registerBlock(ModBlocks.blockDecorationPlaceholder);
    public static final BlockItem            blockItemScarecrow             = registerBlock(ModBlocks.blockScarecrow);
    public static final BlockItem            blockItemPlantationField       = registerBlock(ModBlocks.blockPlantationField);
    public static final BlockItem            blockItemCompostedDirt         = registerBlock(ModBlocks.blockCompostedDirt);
    public static final ItemColonyFlagBanner blockItemFlagBanner            = registerItem("colony_banner", new ItemColonyFlagBanner(new Item.Properties()));
    public static final ItemGate             blockItemIronGate              = registerBlock(ModBlocks.blockIronGate);
    public static final ItemGate             blockItemWoodenGate            = registerBlock(ModBlocks.blockWoodenGate);
    public static final BlockItem            blockItemFarmland              = registerBlock(ModBlocks.blockFarmland);
    public static final BlockItem            blockItemFloodedFarmland       = registerBlock(ModBlocks.blockFloodedFarmland);
    public static final ItemColonySign       blockItemColonySign            = registerBlock(ModBlocks.blockColonySign);

    /**
     * Colony tools.
     */
    public static final ItemClipboard         clipboard         = registerItem("clipboard", new ItemClipboard(new Item.Properties()));
    public static final ItemResourceScroll    resourceScroll    = registerItem("resourcescroll", new ItemResourceScroll(new Item.Properties()));
    public static final ItemQuestLog          questLog          = registerItem("questlog", new ItemQuestLog(new Item.Properties()));
    public static final ItemColonyMap         colonyMap         = registerItem("colonymap", new ItemColonyMap(new Item.Properties()));
    public static final ItemBannerRallyGuards bannerRallyGuards = registerItem("banner_rally_guards", new ItemBannerRallyGuards(new Item.Properties()));
    public static final ItemBuildGoggles      buildGoggles      = registerItem("build_goggles", new ItemBuildGoggles(new Item.Properties()));
    public static final ItemScanAnalyzer      scanAnalyzer      = registerItem("scan_analyzer", new ItemScanAnalyzer(new Item.Properties()));

    public static final ItemAssistantHammer assistantHammerGold    = registerItem("assistanthammer_gold", new ItemAssistantHammer(new Item.Properties().durability(200), 1));
    public static final ItemAssistantHammer assistantHammerIron    = registerItem("assistanthammer_iron", new ItemAssistantHammer(new Item.Properties().durability(400), 2));
    public static final ItemAssistantHammer assistantHammerDiamond = registerItem("assistanthammer_diamond", new ItemAssistantHammer(new Item.Properties().durability(1000), 3));

    public static final ItemScepterLumberjack scepterLumberjack = registerItem("scepterlumberjack", new ItemScepterLumberjack(new Item.Properties()));
    public static final ItemScepterPermission permTool          = registerItem("scepterpermission", new ItemScepterPermission(new Item.Properties()));
    public static final ItemScepterGuard      scepterGuard      = registerItem("scepterguard", new ItemScepterGuard(new Item.Properties()));
    public static final ItemScepterBeekeeper  scepterBeekeeper  = registerItem("scepterbeekeeper", new ItemScepterBeekeeper(new Item.Properties()));

    /**
     * Supply camps.
     */
    public static final ItemSupplyChestDeployer supplyChest = registerItem("supplychestdeployer", new ItemSupplyChestDeployer(new Item.Properties()));
    public static final ItemSupplyCampDeployer  supplyCamp  = registerItem("supplycampdeployer", new ItemSupplyCampDeployer(new Item.Properties()));

    /**
     * Weapons.
     */
    public static final ItemChiefSword    chiefSword    = registerItem("chiefsword", new ItemChiefSword(new Item.Properties().durability(1500)));
    public static final ItemIronScimitar  scimitar      = registerItem("iron_scimitar", new ItemIronScimitar(new Item.Properties().durability(250)));
    public static final ItemPharaoScepter pharaoScepter = registerItem("pharaoscepter", new ItemPharaoScepter(new Item.Properties().durability(400)));
    public static final ItemFireArrow     fireArrow     = registerItem("firearrow", new ItemFireArrow(new Item.Properties()));
    public static final ItemSpear         spear         = registerItem("spear", new ItemSpear(new Item.Properties()));

    /**
     * Scrolls.
     */
    public static final ItemScrollColonyTP     scrollColonyTP     = registerItem("scroll_tp", new ItemScrollColonyTP(new Item.Properties().stacksTo(16)));
    public static final ItemScrollColonyAreaTP scrollColonyAreaTP = registerItem("scroll_area_tp", new ItemScrollColonyAreaTP(new Item.Properties().stacksTo(16)));
    public static final ItemScrollBuff         scrollBuff         = registerItem("scroll_buff", new ItemScrollBuff(new Item.Properties().stacksTo(16)));
    public static final ItemScrollGuardHelp    scrollGuardHelp    = registerItem("scroll_guard_help", new ItemScrollGuardHelp(new Item.Properties().stacksTo(16)));
    public static final ItemScrollHighlight    scrollHighLight    = registerItem("scroll_highlight", new ItemScrollHighlight(new Item.Properties().stacksTo(16)));

    /**
     * Armors.
     */
    public static final ArmorItem santaHat = registerItem("santa_hat", new ArmorItem(ModArmorMaterials.SANTA_HAT, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final ArmorItem pirateHelmet_1 =
        registerItem("pirate_hat", new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.HELMET, new Item.Properties().durability(350)));
    public static final ArmorItem pirateChest_1  =
        registerItem("pirate_top", new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(550)));
    public static final ArmorItem pirateLegs_1   =
        registerItem("pirate_leggins", new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(500)));
    public static final ArmorItem pirateBoots_1  =
        registerItem("pirate_boots", new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.BOOTS, new Item.Properties().durability(400)));

    public static final ArmorItem pirateHelmet_2 =
        registerItem("pirate_cap", new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.HELMET, new Item.Properties().durability(200)));
    public static final ArmorItem pirateChest_2  =
        registerItem("pirate_chest", new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(350)));
    public static final ArmorItem pirateLegs_2   =
        registerItem("pirate_legs", new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(300)));
    public static final ArmorItem pirateBoots_2  =
        registerItem("pirate_shoes", new ArmorItem(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.BOOTS, new Item.Properties().durability(250)));

    public static final ArmorItem plateArmorHelmet =
        registerItem("plate_armor_helmet", new ArmorItem(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.HELMET, new Item.Properties().durability(350)));
    public static final ArmorItem plateArmorChest  =
        registerItem("plate_armor_chest", new ArmorItem(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(500)));
    public static final ArmorItem plateArmorLegs   =
        registerItem("plate_armor_legs", new ArmorItem(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.LEGGINGS, new Item.Properties().durability(450)));
    public static final ArmorItem plateArmorBoots  =
        registerItem("plate_armor_boots", new ArmorItem(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.BOOTS, new Item.Properties().durability(400)));

    /**
     * Spawn eggs.
     */
    public static final SpawnEggItem campBarbarianSpawnEgg           = registerSpawnEgg("barbarianegg", ModEntities.CAMP_BARBARIAN, getColorSafe("orange"), getColorSafe("black"));
    public static final SpawnEggItem campBarbarianArcherSpawnEgg     =
        registerSpawnEgg("barbarcheregg", ModEntities.CAMP_ARCHERBARBARIAN, getColorSafe("orange"), getColorSafe("green"));
    public static final SpawnEggItem campBarbarianChiefSpawnEgg      =
        registerSpawnEgg("barbchiefegg", ModEntities.CAMP_CHIEFBARBARIAN, getColorSafe("orange"), getColorSafe("yellow"));
    public static final SpawnEggItem campPirateSpawnEgg              = registerSpawnEgg("pirateegg", ModEntities.CAMP_PIRATE, getColorSafe("red"), getColorSafe("white"));
    public static final SpawnEggItem campPirateArcherSpawnEgg        =
        registerSpawnEgg("piratearcheregg", ModEntities.CAMP_ARCHERPIRATE, getColorSafe("red"), getColorSafe("green"));
    public static final SpawnEggItem campPirateChiefSpawnEgg         =
        registerSpawnEgg("piratecaptainegg", ModEntities.CAMP_CHIEFPIRATE, getColorSafe("red"), getColorSafe("yellow"));
    public static final SpawnEggItem campMummySpawnEgg               = registerSpawnEgg("mummyegg", ModEntities.CAMP_MUMMY, getColorSafe("yellow"), getColorSafe("white"));
    public static final SpawnEggItem campMummyArcherSpawnEgg         =
        registerSpawnEgg("mummyarcheregg", ModEntities.CAMP_ARCHERMUMMY, getColorSafe("yellow"), getColorSafe("green"));
    public static final SpawnEggItem campPharaoSpawnEgg              = registerSpawnEgg("pharaoegg", ModEntities.CAMP_PHARAO, getColorSafe("yellow"), getColorSafe("yellow"));
    public static final SpawnEggItem campShieldmaidenSpawnEgg        =
        registerSpawnEgg("shieldmaidenegg", ModEntities.CAMP_SHIELDMAIDEN, getColorSafe("black"), getColorSafe("white"));
    public static final SpawnEggItem campNorsemenArcherSpawnEgg      =
        registerSpawnEgg("norsemenarcheregg", ModEntities.CAMP_NORSEMEN_ARCHER, getColorSafe("black"), getColorSafe("green"));
    public static final SpawnEggItem campNorsemenChiefSpawnEgg       =
        registerSpawnEgg("norsemenchiefegg", ModEntities.CAMP_NORSEMEN_CHIEF, getColorSafe("black"), getColorSafe("yellow"));
    public static final SpawnEggItem campAmazonSpawnEgg              = registerSpawnEgg("amazonegg", ModEntities.CAMP_AMAZON, getColorSafe("green"), getColorSafe("white"));
    public static final SpawnEggItem campAmazonSpearmanSpawnEgg      =
        registerSpawnEgg("amazonspearmanegg", ModEntities.CAMP_AMAZONSPEARMAN, getColorSafe("green"), getColorSafe("green"));
    public static final SpawnEggItem campAmazonChiefSpawnEgg         =
        registerSpawnEgg("amazonchiefegg", ModEntities.CAMP_AMAZONCHIEF, getColorSafe("green"), getColorSafe("yellow"));
    public static final SpawnEggItem campDrownedPirateSpawnEgg       =
        registerSpawnEgg("drownedpirateegg", ModEntities.CAMP_DROWNED_PIRATE, getColorSafe("blue"), getColorSafe("white"));
    public static final SpawnEggItem campDrownedPirateArcherSpawnEgg =
        registerSpawnEgg("drownedpiratearcheregg", ModEntities.CAMP_DROWNED_ARCHERPIRATE, getColorSafe("blue"), getColorSafe("green"));
    public static final SpawnEggItem campDrownedPirateChiefSpawnEgg  =
        registerSpawnEgg("drownedpiratecaptainegg", ModEntities.CAMP_DROWNED_CHIEFPIRATE, getColorSafe("blue"), getColorSafe("yellow"));

    /**
     * Other items.
     */
    public static final ItemAncientTome    ancientTome    = registerItem("ancienttome", new ItemAncientTome(new Item.Properties()));
    public static final ItemCompost        compost        = registerItem("compost", new ItemCompost(new Item.Properties()));
    public static final Item               mistletoe      = registerItem("mistletoe", new Item(new Item.Properties()));
    public static final Item               magicPotion    = registerItem("magicpotion", new Item(new Item.Properties().stacksTo(16)));
    public static final ItemAdventureToken adventureToken = registerItem("adventure_token", new ItemAdventureToken(new Item.Properties()));

    public static final Item sifterMeshString  = registerItem("sifter_mesh_string", new Item(new Item.Properties().durability(500).setNoRepair()));
    public static final Item sifterMeshFlint   = registerItem("sifter_mesh_flint", new Item(new Item.Properties().durability(1000).setNoRepair()));
    public static final Item sifterMeshIron    = registerItem("sifter_mesh_iron", new Item(new Item.Properties().durability(1500).setNoRepair()));
    public static final Item sifterMeshDiamond = registerItem("sifter_mesh_diamond", new Item(new Item.Properties().durability(2000).setNoRepair()));

    public static final ItemLargeBottle largeEmptyBottle   = registerItem("large_empty_bottle", new ItemLargeBottle(new Item.Properties()));
    public static final ItemLargeBottle largeMilkBottle    =
        registerItem("large_water_bottle", new ItemLargeBottle(new Item.Properties().craftRemainder(ModItems.largeEmptyBottle)));
    public static final ItemLargeBottle largeWaterBottle   =
        registerItem("large_milk_bottle", new ItemLargeBottle(new Item.Properties().craftRemainder(ModItems.largeEmptyBottle)));
    public static final ItemLargeBottle largeSoyMilkBottle =
        registerItem("large_soy_milk_bottle", new ItemLargeBottle(new Item.Properties().craftRemainder(ModItems.largeEmptyBottle)));

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModItems()
    {
    }

    private static SpawnEggItem registerSpawnEgg(final String id, final EntityType<? extends Mob> entityType, final int backgroundColor, final int highlightColor)
    {
        return registerItem(id, new DeferredSpawnEggItem(() -> entityType, backgroundColor, highlightColor, new Item.Properties()));
    }

    private static <T extends Item> T registerItem(final String id, final T item)
    {
        DEFERRED_REGISTER.register(id, () -> item);
        return item;
    }

    private static <T extends BlockItem> T registerBlock(final IMinecoloniesBlock<T> block)
    {
        return registerItem(block.getRegistryName().getPath(), block.createBlockItem());
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
