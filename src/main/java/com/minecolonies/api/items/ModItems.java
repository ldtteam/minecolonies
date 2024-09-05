package com.minecolonies.api.items;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444"})
public final class ModItems
{
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(Constants.MOD_ID);

    public static final DeferredItem<ItemAdventureToken>      adventureToken    = custom("adventure_token", ItemAdventureToken::new);
    public static final DeferredItem<ItemAncientTome>         ancientTome       = custom("ancienttome", ItemAncientTome::new);
    public static final DeferredItem<ItemBannerRallyGuards>   bannerRallyGuards = custom("banner_rally_guards", ItemBannerRallyGuards::new);
    public static final DeferredItem<ItemBuildGoggles>        buildGoggles      = custom("build_goggles", ItemBuildGoggles::new);
    public static final DeferredItem<ItemChiefSword>          chiefSword        = custom("chiefsword", ItemChiefSword::new);
    public static final DeferredItem<ItemClipboard>           clipboard         = custom("clipboard", ItemClipboard::new);
    public static final DeferredItem<ItemCompost>             compost           = custom("compost", ItemCompost::new);
    public static final DeferredItem<ItemFireArrow>           firearrow         = custom("firearrow", ItemFireArrow::new);
    public static final DeferredItem<ItemColonyFlagBanner>    flagBanner        = custom("colony_banner", prop -> new ItemColonyFlagBanner(ModBlocks.blockColonyBanner.get(), ModBlocks.blockColonyWallBanner.get(), prop.stacksTo(1)));
    public static final DeferredItem<ItemMagicPotion>         magicpotion       = custom("magicpotion", ItemMagicPotion::new);
    public static final DeferredItem<ItemMistletoe>           mistletoe         = custom("mistletoe", ItemMistletoe::new);
    public static final DeferredItem<ItemScepterPermission>   permTool          = custom("scepterpermission", ItemScepterPermission::new);
    public static final DeferredItem<ItemPharaoScepter>       pharaoscepter     = custom("pharaoscepter", ItemPharaoScepter::new);
    public static final DeferredItem<ItemQuestLog>            questLog          = custom("questlog", ItemQuestLog::new);
    public static final DeferredItem<ItemResourceScroll>      resourceScroll    = custom("resourcescroll", ItemResourceScroll::new);
    public static final DeferredItem<ItemSantaHead>           santaHat          = custom("santa_hat", prop -> new ItemSantaHead(ModArmorMaterials.SANTA_HAT, ArmorItem.Type.HELMET, prop));
    public static final DeferredItem<ItemScanAnalyzer>        scanAnalyzer      = custom("scan_analyzer", ItemScanAnalyzer::new);
    public static final DeferredItem<ItemScepterBeekeeper>    scepterBeekeeper  = custom("scepterbeekeeper", ItemScepterBeekeeper::new);
    public static final DeferredItem<ItemScepterGuard>        scepterGuard      = custom("scepterguard", ItemScepterGuard::new);
    public static final DeferredItem<ItemScepterLumberjack>   scepterLumberjack = custom("scepterlumberjack", ItemScepterLumberjack::new);
    public static final DeferredItem<ItemIronScimitar>        scimitar          = custom("iron_scimitar", ItemIronScimitar::new);
    public static final DeferredItem<ItemSpear>               spear             = custom("spear", ItemSpear::new);
    public static final DeferredItem<ItemSupplyCampDeployer>  supplyCamp        = custom("supplycampdeployer", ItemSupplyCampDeployer::new);
    public static final DeferredItem<ItemSupplyChestDeployer> supplyChest       = custom("supplychestdeployer", ItemSupplyChestDeployer::new);

    public static final DeferredItem<ItemPirateGear> pirateHelmet_1 = custom("pirate_hat", prop -> new ItemPirateGear(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.HELMET, prop));
    public static final DeferredItem<ItemPirateGear> pirateChest_1  = custom("pirate_top", prop -> new ItemPirateGear(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.CHESTPLATE, prop));
    public static final DeferredItem<ItemPirateGear> pirateLegs_1   = custom("pirate_leggins", prop -> new ItemPirateGear(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.LEGGINGS, prop));
    public static final DeferredItem<ItemPirateGear> pirateBoots_1  = custom("pirate_boots", prop -> new ItemPirateGear(ModArmorMaterials.PIRATE_ARMOR_1, ArmorItem.Type.BOOTS, prop));

    public static final DeferredItem<ItemPirateGear> pirateHelmet_2 = custom("pirate_cap", prop -> new ItemPirateGear(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.HELMET, prop));
    public static final DeferredItem<ItemPirateGear> pirateChest_2  = custom("pirate_chest", prop -> new ItemPirateGear(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.CHESTPLATE, prop));
    public static final DeferredItem<ItemPirateGear> pirateLegs_2   = custom("pirate_legs", prop -> new ItemPirateGear(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.LEGGINGS, prop));
    public static final DeferredItem<ItemPirateGear> pirateBoots_2  = custom("pirate_shoes", prop -> new ItemPirateGear(ModArmorMaterials.PIRATE_ARMOR_2, ArmorItem.Type.BOOTS, prop));

    public static final DeferredItem<ItemPlateArmor> plateArmorHelmet = custom("plate_armor_helmet", prop -> new ItemPlateArmor(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.HELMET, prop));
    public static final DeferredItem<ItemPlateArmor> plateArmorChest  = custom("plate_armor_chest", prop -> new ItemPlateArmor(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.CHESTPLATE, prop));
    public static final DeferredItem<ItemPlateArmor> plateArmorLegs   = custom("plate_armor_legs", prop -> new ItemPlateArmor(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.LEGGINGS, prop));
    public static final DeferredItem<ItemPlateArmor> plateArmorBoots  = custom("plate_armor_boots", prop -> new ItemPlateArmor(ModArmorMaterials.PLATE_ARMOR, ArmorItem.Type.BOOTS, prop));

    public static final DeferredItem<ItemBreadDough>    breadDough    = custom("bread_dough", ItemBreadDough::new);
    public static final DeferredItem<ItemCookieDough>   cookieDough   = custom("cookie_dough", ItemCookieDough::new);
    public static final DeferredItem<ItemCakeBatter>    cakeBatter    = custom("cake_batter", ItemCakeBatter::new);
    public static final DeferredItem<ItemRawPumpkinPie> rawPumpkinPie = custom("raw_pumpkin_pie", ItemRawPumpkinPie::new);

    public static final DeferredItem<ItemMilkyBread>  milkyBread  = custom("milky_bread", ItemMilkyBread::new);
    public static final DeferredItem<ItemSugaryBread> sugaryBread = custom("sugary_bread", ItemSugaryBread::new);
    public static final DeferredItem<ItemGoldenBread> goldenBread = custom("golden_bread", ItemGoldenBread::new);
    public static final DeferredItem<ItemChorusBread> chorusBread = custom("chorus_bread", ItemChorusBread::new);

    public static final DeferredItem<ItemScrollColonyTP>     scrollColonyTP     = custom("scroll_tp", prop -> new ItemScrollColonyTP(prop.stacksTo(16)));
    public static final DeferredItem<ItemScrollColonyAreaTP> scrollColonyAreaTP = custom("scroll_area_tp", prop -> new ItemScrollColonyAreaTP(prop.stacksTo(16)));
    public static final DeferredItem<ItemScrollBuff>         scrollBuff         = custom("scroll_buff", prop -> new ItemScrollBuff(prop.stacksTo(16)));
    public static final DeferredItem<ItemScrollGuardHelp>    scrollGuardHelp    = custom("scroll_guard_help", prop -> new ItemScrollGuardHelp(prop.stacksTo(16)));
    public static final DeferredItem<ItemScrollHighlight>    scrollHighLight    = custom("scroll_highlight", prop -> new ItemScrollHighlight(prop.stacksTo(16)));

    public static final DeferredItem<ItemSifterMesh> sifterMeshString  = custom("sifter_mesh_string", prop -> new ItemSifterMesh(prop.durability(500).setNoRepair()));
    public static final DeferredItem<ItemSifterMesh> sifterMeshFlint   = custom("sifter_mesh_flint", prop -> new ItemSifterMesh(prop.durability(1000).setNoRepair()));
    public static final DeferredItem<ItemSifterMesh> sifterMeshIron    = custom("sifter_mesh_iron", prop -> new ItemSifterMesh(prop.durability(1500).setNoRepair()));
    public static final DeferredItem<ItemSifterMesh> sifterMeshDiamond = custom("sifter_mesh_diamond", prop -> new ItemSifterMesh(prop.durability(2000).setNoRepair()));

    // Tier 1 Food Items
    public static final DeferredItem<ItemFood> cheddar_cheese = custom("cheddar_cheese", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), ModJobs.CHEF_ID.getPath(), 1));
    public static final DeferredItem<ItemFood> feta_cheese    = custom("feta_cheese", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), ModJobs.CHEF_ID.getPath(), 1));
    public static final DeferredItem<ItemFood> cooked_rice    = custom("cooked_rice", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).usingConvertsTo(Items.BOWL).build()), ModJobs.CHEF_ID.getPath(), 1));
    public static final DeferredItem<ItemFood> tofu           = custom("tofu", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), ModJobs.CHEF_ID.getPath(), 1));
    public static final DeferredItem<ItemFood> flatbread      = custom("flatbread", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), ModJobs.BAKER_ID.getPath(), 1));

    // Tier 2 Food Items
    public static final DeferredItem<ItemFood> manchet_bread = custom("manchet_bread", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), ModJobs.BAKER_ID.getPath(), 2));
    public static final DeferredItem<ItemFood> lembas_scone  = custom("lembas_scone", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), ModJobs.BAKER_ID.getPath(), 2));
    public static final DeferredItem<ItemFood> muffin        = custom("muffin", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), ModJobs.BAKER_ID.getPath(), 2));
    public static final DeferredItem<ItemFood> pottage       = custom("pottage", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).usingConvertsTo(Items.BOWL).build()), ModJobs.CHEF_ID.getPath(), 2));
    public static final DeferredItem<ItemFood> pasta_plain   = custom("pasta_plain", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).usingConvertsTo(Items.BOWL).build()), ModJobs.CHEF_ID.getPath(), 2));

    // Tier 3 Food items
    public static final DeferredItem<ItemFood> hand_pie = custom("hand_pie", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3));

    // Cold Biomes
    // Tier 2
    public static final DeferredItem<ItemFood> cabochis = custom("cabochis", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).usingConvertsTo(Items.BOWL).build()), ModJobs.CHEF_ID.getPath(), 2));
    // Tier 3
    public static final DeferredItem<ItemFood> lamb_stew = custom("lamb_stew", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).usingConvertsTo(Items.BOWL).build()), ModJobs.CHEF_ID.getPath(), 3));

    // Hot Humid Biomes
    // Tier 2
    public static final DeferredItem<ItemFood> rice_ball = custom("rice_ball", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), ModJobs.CHEF_ID.getPath(), 2));
    // Tier 3
    public static final DeferredItem<ItemFood> sushi_roll = custom("sushi_roll", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3));

    // Temperate Biomes
    // Tier 2
    public static final DeferredItem<ItemFood> pasta_tomato = custom("pasta_tomato", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).usingConvertsTo(Items.BOWL).build()), ModJobs.CHEF_ID.getPath(), 2));
    // Tier 3
    public static final DeferredItem<ItemFood> eggplant_dolma = custom("eggplant_dolma", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3));
    public static final DeferredItem<ItemFood> stuffed_pita   = custom("stuffed_pita", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3));

    // Hot Dry Biomes
    // Tier 2
    public static final DeferredItem<ItemFood> pepper_hummus = custom("pepper_hummus", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), ModJobs.CHEF_ID.getPath(), 2));
    // Tier 3
    public static final DeferredItem<ItemFood> pita_hummus = custom("pita_hummus", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3));

    // Require trading
    // Tier 2
    public static final DeferredItem<ItemFood> congee = custom("congee", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).usingConvertsTo(Items.BOWL).build()), ModJobs.CHEF_ID.getPath(), 2));
    // Tier 3
    public static final DeferredItem<ItemFood> stew_trencher  = custom("stew_trencher", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3));
    public static final DeferredItem<ItemFood> stuffed_pepper = custom("stuffed_pepper", prop -> new ItemFood(prop.food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), ModJobs.CHEF_ID.getPath(), 3));

    // Just dough
    public static final DeferredItem<Item> muffin_dough  = simple("muffin_dough");
    public static final DeferredItem<Item> manchet_dough = simple("manchet_dough");
    public static final DeferredItem<Item> raw_noodle    = simple("raw_noodle");
    public static final DeferredItem<Item> butter        = simple("butter");

    public static final DeferredItem<ItemLargeBottle> large_empty_bottle    = custom("large_empty_bottle", ItemLargeBottle::new);
    public static final DeferredItem<ItemLargeBottle> large_milk_bottle     = custom("large_milk_bottle", prop -> new ItemLargeBottle(prop.craftRemainder(large_empty_bottle.get())));
    public static final DeferredItem<ItemLargeBottle> large_water_bottle    = custom("large_water_bottle", prop -> new ItemLargeBottle(prop.craftRemainder(large_empty_bottle.get())));
    public static final DeferredItem<ItemLargeBottle> large_soy_milk_bottle = custom("large_soy_milk_bottle", prop -> new ItemLargeBottle(prop.craftRemainder(large_empty_bottle.get())));

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
     * Simply registers custom item, nothing else.
     */
    private static <I extends Item> DeferredItem<I> custom(final String name, final Function<Item.Properties, I> item)
    {
        return REGISTRY.register(name, () -> item.apply(new Item.Properties()));
    }

    /**
     * Simply registers item, nothing else.
     */
    private static DeferredItem<Item> simple(final String name)
    {
        return custom(name, Item::new);
    }

    /**
     * Registers spawn egg for given entity.
     */
    private static DeferredItem<DeferredSpawnEggItem> spawnEgg(final String name,
        final Supplier<? extends EntityType<? extends Mob>> entityType,
        final int backgroundColor,
        final int highlightColor)
    {
        return custom(name, prop -> new DeferredSpawnEggItem(entityType, backgroundColor, highlightColor, prop));
    }
}
