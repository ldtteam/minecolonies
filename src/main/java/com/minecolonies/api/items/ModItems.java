package com.minecolonies.api.items;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

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
    public static Item scepterGuard;
    public static Item bannerRallyGuards;
    public static Item supplyCamp;
    public static Item ancientTome;
    public static Item chiefSword;
    public static Item scimitar;
    public static Item scepterLumberjack;
    public static Item pharaoscepter;
    public static Item firearrow;
    public static Item questLog;
    public static Item scepterBeekeeper;
    public static Item mistletoe;
    public static Item spear;

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

    public static Item plateArmorHelmet;
    public static Item plateArmorChest;
    public static Item plateArmorLegs;
    public static Item plateArmorBoots;

    public static Item santaHat;

    public static Item flagBanner;
    public static Item irongate;
    public static Item woodgate;

    public static Item breadDough;
    public static Item cookieDough;
    public static Item cakeBatter;
    public static Item rawPumpkinPie;

    public static Item milkyBread;
    public static Item sugaryBread;
    public static Item goldenBread;
    public static Item chorusBread;

    public static Item adventureToken;

    public static Item scrollColonyTP;
    public static Item scrollColonyAreaTP;
    public static Item scrollBuff;
    public static Item scrollGuardHelp;
    public static Item scrollHighLight;

    public static Item sifterMeshString;
    public static Item sifterMeshFlint;
    public static Item sifterMeshIron;
    public static Item sifterMeshDiamond;

    public static Item magicpotion;

    public static Item buildGoggles;
    public static Item scanAnalyzer;

    public static Item butter;
    public static Item cabochis;
    public static Item cheddar_cheese;
    public static Item congee;
    public static Item cooked_rice;
    public static Item eggplant_dolma;
    public static Item feta_cheese;
    public static Item flatbread;
    public static Item hand_pie;
    public static Item lamb_stew;
    public static Item lembas_scone;
    public static Item manchet_bread;
    public static Item manchet_dough;
    public static Item muffin;
    public static Item muffin_dough;
    public static Item pasta_plain;
    public static Item pasta_tomato;
    public static Item pepper_hummus;
    public static Item pita_hummus;
    public static Item pottage;
    public static Item raw_noodle;
    public static Item rice_ball;
    public static Item stew_trencher;
    public static Item stuffed_pepper;
    public static Item stuffed_pita;
    public static Item sushi_roll;
    public static Item tofu;

    public static Item cornmeal;
    public static Item creamcheese;
    public static Item soysauce;
    public static Item cheese_ravioli;
    public static Item chicken_broth;
    public static Item corn_chowder;
    public static Item spicy_grilled_chicken;
    public static Item kebab;
    public static Item meat_ravioli;
    public static Item mint_jelly;
    public static Item mint_tea;
    public static Item pea_soup;
    public static Item polenta;
    public static Item potato_soup;
    public static Item squash_soup;
    public static Item veggie_ravioli;
    public static Item yogurt;
    public static Item baked_salmon;
    public static Item eggdrop_soup;
    public static Item fish_n_chips;
    public static Item kimchi;
    public static Item pierogi;
    public static Item veggie_quiche;
    public static Item veggie_soup;
    public static Item yogurt_with_berries;
    public static Item borscht;
    public static Item fish_dinner;
    public static Item mutton_dinner;
    public static Item ramen;
    public static Item schnitzel;
    public static Item steak_dinner;
    public static Item tacos;
    public static Item tortillas;
    public static Item apple_pie;
    public static Item cheese_pizza;
    public static Item mushroom_pizza;
    public static Item plain_cheesecake;
    public static Item mintchoco_cheesecake;
    public static Item spicy_eggplant;

    public static Item large_water_bottle;
    public static Item large_milk_bottle;
    public static Item large_soy_milk_bottle;
    public static Item large_empty_bottle;

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModItems()
    {
        /*
         * Intentionally left empty.
         */
    }

    @NotNull
    public static Item[] getAllIngredients()
    {
        return new Item[] {
          muffin_dough,
          manchet_dough,
          raw_noodle,
          butter,
          cornmeal,
          creamcheese,
          soysauce,
        };
    }

    @NotNull
    public static Item[] getAllFoods()
    {
        return new Item[] {
          // Tier 1 Food
          cheddar_cheese,
          feta_cheese,
          cooked_rice,
          tofu,
          flatbread,
          cheese_ravioli,
          chicken_broth,
          meat_ravioli,
          mint_jelly,
          mint_tea,
          polenta,
          potato_soup,
          veggie_ravioli,
          yogurt,
          squash_soup,
          pea_soup,
          corn_chowder,
          tortillas,
          spicy_grilled_chicken,

          // Tier 2 Food
          manchet_bread,
          lembas_scone,
          muffin,
          pottage,
          pasta_plain,
          apple_pie,
          plain_cheesecake,
          baked_salmon,
          eggdrop_soup,
          fish_n_chips,
          pierogi,
          veggie_soup,
          yogurt_with_berries,
          cabochis,
          veggie_quiche,
          rice_ball,
          mutton_dinner,
          pasta_tomato,
          cheese_pizza,
          pepper_hummus,
          kebab,
          congee,
          kimchi,

          // Tier 3 Food
          hand_pie,
          mintchoco_cheesecake,
          borscht,
          schnitzel,
          steak_dinner,
          lamb_stew,
          fish_dinner,
          sushi_roll,
          ramen,
          eggplant_dolma,
          stuffed_pita,
          mushroom_pizza,
          pita_hummus,
          spicy_eggplant,
          stew_trencher,
          stuffed_pepper,
          tacos
        };
    }
}
