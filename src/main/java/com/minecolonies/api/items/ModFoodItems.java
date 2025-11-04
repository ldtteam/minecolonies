package com.minecolonies.api.items;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Class handling the registering of the mod items.
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
public final class ModFoodItems
{
    public static final DeferredRegister.Items DEFERRED_REGISTER = DeferredRegister.createItems(Constants.MOD_ID);

    /**
     * The list of food ingredients.
     */
    public static final List<DeferredItem<Item>> INGREDIENTS = new ArrayList<>();

    /**
     * The list of food.
     */
    public static final List<DeferredItem<ItemFood>> FOODS = new ArrayList<>();

    /**
     * Ingredients.
     */
    public static final DeferredItem<Item> bread_dough     = registerIngredient("bread_dough", Item::new, new Item.Properties());
    public static final DeferredItem<Item> cookie_dough    = registerIngredient("cookie_dough", Item::new, new Item.Properties());
    public static final DeferredItem<Item> cake_batter     = registerIngredient("cake_batter", Item::new, new Item.Properties());
    public static final DeferredItem<Item> raw_pumpkin_pie = registerIngredient("raw_pumpkin_pie", Item::new, new Item.Properties());
    public static final DeferredItem<Item> muffin_dough    = registerIngredient("muffin_dough", Item::new, new Item.Properties());
    public static final DeferredItem<Item> manchet_dough   = registerIngredient("manchet_dough", Item::new, new Item.Properties());
    public static final DeferredItem<Item> raw_noodle      = registerIngredient("raw_noodle", Item::new, new Item.Properties());
    public static final DeferredItem<Item> butter          = registerIngredient("butter", Item::new, new Item.Properties());
    public static final DeferredItem<Item> cornmeal        = registerIngredient("cornmeal", Item::new, new Item.Properties());
    public static final DeferredItem<Item> creamcheese     = registerIngredient("creamcheese", Item::new, new Item.Properties());
    public static final DeferredItem<Item> soysauce        = registerIngredient("soysauce", Item::new, new Item.Properties());

    //<editor-fold desc="All Biomes Food">

    /**
     * Tier 1 food.
     */
    public static final DeferredItem<ItemFood> cheddar_cheese =
        registerFood("cheddar_cheese", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> feta_cheese    =
        registerFood("feta_cheese", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> cooked_rice    =
        registerFood("cooked_rice", 1, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> tofu           =
        registerFood("tofu", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> flatbread      =
        registerFood("flatbread", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> cheese_ravioli =
        registerFood("cheese_ravioli", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> chicken_broth  =
        registerFood("chicken_broth", 1, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> meat_ravioli   =
        registerFood("meat_ravioli", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> mint_jelly     =
        registerFood("mint_jelly", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> mint_tea       =
        registerFood("mint_tea", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> polenta        =
        registerFood("polenta", 1, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> potato_soup    =
        registerFood("potato_soup", 1, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> veggie_ravioli =
        registerFood("veggie_ravioli", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> yogurt         =
        registerFood("yogurt", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));

    /**
     * Tier 2 food.
     */
    public static final DeferredItem<ItemFood> manchet_bread       =
        registerFood("manchet_bread", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> lembas_scone        =
        registerFood("lembas_scone", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> muffin              =
        registerFood("muffin", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> pottage             =
        registerFood("pottage", 2, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> pasta_plain         =
        registerFood("pasta_plain", 2, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> apple_pie           =
        registerFood("apple_pie", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> plain_cheesecake    =
        registerFood("plain_cheesecake", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> baked_salmon        =
        registerFood("baked_salmon", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> eggdrop_soup        =
        registerFood("eggdrop_soup", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).usingConvertsTo(Items.BOWL).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> fish_n_chips        =
        registerFood("fish_n_chips", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> pierogi             =
        registerFood("pierogi", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> veggie_soup         =
        registerFood("veggie_soup", 2, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> yogurt_with_berries =
        registerFood("yogurt_with_berries", 2, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()));

    /**
     * Tier 3 food.
     */
    public static final DeferredItem<ItemFood> hand_pie             =
        registerFood("hand_pie", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> mintchoco_cheesecake =
        registerFood("mintchoco_cheesecake", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> borscht              =
        registerFood("borscht", 3, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> schnitzel            =
        registerFood("schnitzel", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> steak_dinner         =
        registerFood("steak_dinner", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));

    //</editor-fold>

    //<editor-fold desc="Temperate Biomes Food">

    /**
     * Tier 1 food.
     */
    public static final DeferredItem<ItemFood> corn_chowder =
        registerFood("corn_chowder", 1, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemFood> tortillas    =
        registerFood("tortillas", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));

    /**
     * Tier 2 food.
     */
    public static final DeferredItem<ItemFood> pasta_tomato =
        registerFood("pasta_tomato", 2, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> cheese_pizza =
        registerFood("cheese_pizza", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));

    /**
     * Tier 3 food.
     */
    public static final DeferredItem<ItemFood> eggplant_dolma =
        registerFood("eggplant_dolma", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> stuffed_pita   =
        registerFood("stuffed_pita", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> mushroom_pizza =
        registerFood("mushroom_pizza", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));

    //</editor-fold>

    //<editor-fold desc="Cold Biomes Food">

    /**
     * Tier 1 food.
     */
    public static final DeferredItem<ItemFood> squash_soup =
        registerFood("squash_soup", 1, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()));

    /**
     * Tier 2 food.
     */
    public static final DeferredItem<ItemFood> cabochis      =
        registerFood("cabochis", 2, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> veggie_quiche =
        registerFood("veggie_quiche", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));

    /**
     * Tier 3 food.
     */
    public static final DeferredItem<ItemFood> lamb_stew   =
        registerFood("lamb_stew", 3, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> fish_dinner =
        registerFood("fish_dinner", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));

    //</editor-fold>

    //<editor-fold desc="Hot Humid Biomes Food">

    /**
     * Tier 1 food.
     */
    public static final DeferredItem<ItemFood> pea_soup =
        registerFood("pea_soup", 1, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(4).saturationModifier(0.6F).build()));

    /**
     * Tier 2 food.
     */
    public static final DeferredItem<ItemFood> rice_ball     =
        registerFood("rice_ball", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> mutton_dinner =
        registerFood("mutton_dinner", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));

    /**
     * Tier 3 food.
     */
    public static final DeferredItem<ItemFood> sushi_roll =
        registerFood("sushi_roll", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> ramen      =
        registerFood("ramen", 3, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> fried_rice =
        registerFood("fried_rice", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));

    //</editor-fold>

    //<editor-fold desc="Hot Dry Biomes Food">

    /**
     * Tier 1 food.
     */
    public static final DeferredItem<ItemFood> spicy_grilled_chicken =
        registerFood("spicy_grilled_chicken", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6F).build()));

    /**
     * Tier 2 food.
     */
    public static final DeferredItem<ItemFood> pepper_hummus =
        registerFood("pepper_hummus", 2, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> kebab         =
        registerFood("kebab", 1, new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).build()));

    /**
     * Tier 3 food.
     */
    public static final DeferredItem<ItemFood> pita_hummus    =
        registerFood("pita_hummus", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> spicy_eggplant =
        registerFood("spicy_eggplant", 3, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(8).saturationModifier(1.2F).build()));

    //</editor-fold>

    //<editor-fold desc="Requires Trading Food">

    /**
     * Tier 2 food.
     */
    public static final DeferredItem<ItemFood> congee =
        registerFood("congee", 2, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()));
    public static final DeferredItem<ItemFood> kimchi =
        registerFood("kimchi", 2, new Item.Properties().food(new FoodProperties.Builder().usingConvertsTo(Items.BOWL).nutrition(6).saturationModifier(1.0F).build()));

    /**
     * Tier 3 food.
     */
    public static final DeferredItem<ItemFood> stew_trencher  =
        registerFood("stew_trencher", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> stuffed_pepper =
        registerFood("stuffed_pepper", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));
    public static final DeferredItem<ItemFood> tacos          =
        registerFood("tacos", 3, new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.2F).build()));

    //</editor-fold>

    /**
     * Special items.
     */
    public static final DeferredItem<ItemMilkyBread>  milkyBread  =
        registerItem("milky_bread", ItemMilkyBread::new, new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemSugaryBread> sugaryBread = registerItem("sugary_bread",
        ItemSugaryBread::new,
        new Item.Properties().food(new FoodProperties.Builder().nutrition(6)
            .saturationModifier(0.7F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600), 1.0F)
            .build()));
    public static final DeferredItem<ItemGoldenBread> goldenBread =
        registerItem("golden_bread", ItemGoldenBread::new, new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build()));
    public static final DeferredItem<ItemChorusBread> chorusBread =
        registerItem("chorus_bread", ItemChorusBread::new, new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(2.0F).alwaysEdible().build()));

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModFoodItems()
    {
    }

    private static DeferredItem<Item> registerIngredient(final String id, final Function<Item.Properties, Item> itemBuilder, final Item.Properties properties)
    {
        final DeferredItem<Item> item = registerItem(id, itemBuilder, properties);
        INGREDIENTS.add(item);
        return item;
    }

    private static <T extends Item> DeferredItem<T> registerItem(final String id, final Function<Item.Properties, T> itemBuilder, final Item.Properties properties)
    {
        return DEFERRED_REGISTER.registerItem(id, itemBuilder, properties);
    }

    private static DeferredItem<ItemFood> registerFood(final String id, final int tier, final Item.Properties properties)
    {
        final DeferredItem<ItemFood> item = registerItem(id, p -> new ItemFood(p, tier), properties);
        FOODS.add(item);
        return item;
    }
}
