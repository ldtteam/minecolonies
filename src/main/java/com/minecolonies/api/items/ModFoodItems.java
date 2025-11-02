package com.minecolonies.api.items;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings("unused")
public final class ModFoodItems
{
    public static final DeferredRegister.Items DEFERRED_REGISTER = DeferredRegister.createItems(Constants.MOD_ID);

    /**
     * The list of food ingredients.
     */
    public static final List<Item> INGREDIENTS = new ArrayList<>();

    /**
     * The list of food.
     */
    public static final List<Item> FOODS = new ArrayList<>();

    /**
     * Ingredients.
     */
    public static final Item bread_dough     = registerIngredient("bread_dough", new Item(new Item.Properties()));
    public static final Item cookie_dough    = registerIngredient("cookie_dough", new Item(new Item.Properties()));
    public static final Item cake_batter     = registerIngredient("cake_batter", new Item(new Item.Properties()));
    public static final Item raw_pumpkin_pie = registerIngredient("raw_pumpkin_pie", new Item(new Item.Properties()));
    public static final Item muffin_dough    = registerIngredient("muffin_dough", new Item(new Item.Properties()));
    public static final Item manchet_dough   = registerIngredient("manchet_dough", new Item(new Item.Properties()));
    public static final Item raw_noodle      = registerIngredient("raw_noodle", new Item(new Item.Properties()));
    public static final Item butter          = registerIngredient("butter", new Item(new Item.Properties()));
    public static final Item cornmeal        = registerIngredient("cornmeal", new Item(new Item.Properties()));
    public static final Item creamcheese     = registerIngredient("creamcheese", new Item(new Item.Properties()));
    public static final Item soysauce        = registerIngredient("soysauce", new Item(new Item.Properties()));

    //<editor-fold desc="All Biomes Food">

    /**
     * Tier 1 food.
     */
    public static final ItemFood cheddar_cheese =
        registerFood("cheddar_cheese", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood feta_cheese    =
        registerFood("feta_cheese", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood cooked_rice    = registerFood("cooked_rice",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood tofu           =
        registerFood("tofu", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood flatbread      =
        registerFood("flatbread", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood cheese_ravioli =
        registerFood("cheese_ravioli", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood chicken_broth  = registerFood("chicken_broth",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood meat_ravioli   =
        registerFood("meat_ravioli", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood mint_jelly     =
        registerFood("mint_jelly", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood mint_tea       =
        registerFood("mint_tea", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood polenta        = registerFood("polenta",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood potato_soup    = registerFood("potato_soup",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood veggie_ravioli =
        registerFood("veggie_ravioli", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood yogurt         =
        registerFood("yogurt", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));

    /**
     * Tier 2 food.
     */
    public static final ItemFood manchet_bread       =
        registerFood("manchet_bread", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood lembas_scone        =
        registerFood("lembas_scone", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood muffin              =
        registerFood("muffin", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood pottage             = registerFood("pottage",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood pasta_plain         = registerFood("pasta_plain",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood apple_pie           =
        registerFood("apple_pie", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood plain_cheesecake    =
        registerFood("plain_cheesecake", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood baked_salmon        =
        registerFood("baked_salmon", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood eggdrop_soup        = registerFood("eggdrop_soup",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).usingConvertsTo(Items.BOWL).saturationModifier(1.0F).build()), 2));
    public static final ItemFood fish_n_chips        =
        registerFood("fish_n_chips", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood pierogi             =
        registerFood("pierogi", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood veggie_soup         = registerFood("veggie_soup",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood yogurt_with_berries = registerFood("yogurt_with_berries",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()), 2));

    /**
     * Tier 3 food.
     */
    public static final ItemFood hand_pie             =
        registerFood("hand_pie", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood mintchoco_cheesecake =
        registerFood("mintchoco_cheesecake", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood borscht              = registerFood("borscht",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood schnitzel            =
        registerFood("schnitzel", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood steak_dinner         =
        registerFood("steak_dinner", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));

    //</editor-fold>

    //<editor-fold desc="Temperate Biomes Food">

    /**
     * Tier 1 food.
     */
    public static final ItemFood corn_chowder = registerFood("corn_chowder",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()), 1));
    public static final ItemFood tortillas    =
        registerFood("tortillas", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));

    /**
     * Tier 2 food.
     */
    public static final ItemFood pasta_tomato = registerFood("pasta_tomato",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood cheese_pizza =
        registerFood("cheese_pizza", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));

    /**
     * Tier 3 food.
     */
    public static final ItemFood eggplant_dolma =
        registerFood("eggplant_dolma", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood stuffed_pita   =
        registerFood("stuffed_pita", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood mushroom_pizza =
        registerFood("mushroom_pizza", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));

    //</editor-fold>

    //<editor-fold desc="Cold Biomes Food">

    /**
     * Tier 1 food.
     */
    public static final ItemFood squash_soup = registerFood("squash_soup",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()), 1));

    /**
     * Tier 2 food.
     */
    public static final ItemFood cabochis      = registerFood("cabochis",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood veggie_quiche =
        registerFood("veggie_quiche", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));

    /**
     * Tier 3 food.
     */
    public static final ItemFood lamb_stew   = registerFood("lamb_stew",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood fish_dinner =
        registerFood("fish_dinner", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));

    //</editor-fold>

    //<editor-fold desc="Hot Humid Biomes Food">

    /**
     * Tier 1 food.
     */
    public static final ItemFood pea_soup = registerFood("pea_soup",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()), 1));

    /**
     * Tier 2 food.
     */
    public static final ItemFood rice_ball     =
        registerFood("rice_ball", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood mutton_dinner =
        registerFood("mutton_dinner", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 3));

    /**
     * Tier 3 food.
     */
    public static final ItemFood sushi_roll =
        registerFood("sushi_roll", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood ramen      =
        registerFood("ramen", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood fried_rice =
        registerFood("fried_rice", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));

    //</editor-fold>

    //<editor-fold desc="Hot Dry Biomes Food">

    /**
     * Tier 1 food.
     */
    public static final ItemFood spicy_grilled_chicken =
        registerFood("spicy_grilled_chicken", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()), 1));

    /**
     * Tier 2 food.
     */
    public static final ItemFood pepper_hummus =
        registerFood("pepper_hummus", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood kebab         =
        registerFood("kebab", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()), 1));

    /**
     * Tier 3 food.
     */
    public static final ItemFood pita_hummus    =
        registerFood("pita_hummus", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood spicy_eggplant = registerFood("spicy_eggplant",
        new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(8).saturationModifier(1.2F).build()), 3));

    //</editor-fold>

    //<editor-fold desc="Requires Trading Food">

    /**
     * Tier 2 food.
     */
    public static final ItemFood congee =
        registerFood("congee", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()), 2));
    public static final ItemFood kimchi =
        registerFood("kimchi", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()), 2));

    /**
     * Tier 3 food.
     */
    public static final ItemFood stew_trencher  =
        registerFood("stew_trencher", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood stuffed_pepper =
        registerFood("stuffed_pepper", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));
    public static final ItemFood tacos          =
        registerFood("tacos", new ItemFood(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()), 3));

    //</editor-fold>

    /**
     * Special items.
     */
    public static final ItemMilkyBread  milkyBread  = registerItem("milky_bread", new ItemMilkyBread());
    public static final ItemSugaryBread sugaryBread = registerItem("sugary_bread", new ItemSugaryBread());
    public static final ItemGoldenBread goldenBread = registerItem("golden_bread", new ItemGoldenBread());
    public static final ItemChorusBread chorusBread = registerItem("chorus_bread", new ItemChorusBread());

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModFoodItems()
    {
    }

    private static <T extends Item> T registerIngredient(final String id, final T item)
    {
        INGREDIENTS.add(item);
        return registerItem(id, item);
    }

    private static <T extends Item> T registerItem(final String id, final T item)
    {
        DEFERRED_REGISTER.register(id, () -> item);
        return item;
    }

    private static <T extends ItemFood> T registerFood(final String id, final T item)
    {
        FOODS.add(item);
        return registerItem(id, item);
    }
}
