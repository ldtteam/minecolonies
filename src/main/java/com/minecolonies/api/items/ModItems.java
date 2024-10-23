package com.minecolonies.api.items;

import com.minecolonies.core.items.ItemFood;
import net.minecraft.world.item.Item;

import java.util.List;

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

    public static ItemFood cabochis;
    public static ItemFood cheddar_cheese;
    public static ItemFood congee;
    public static ItemFood cooked_rice;
    public static ItemFood eggplant_dolma;
    public static ItemFood feta_cheese;
    public static ItemFood flatbread;
    public static ItemFood hand_pie;
    public static ItemFood lamb_stew;
    public static ItemFood lembas_scone;
    public static ItemFood manchet_bread;
    public static ItemFood muffin;
    public static ItemFood pasta_plain;
    public static ItemFood pasta_tomato;
    public static ItemFood pepper_hummus;
    public static ItemFood pita_hummus;
    public static ItemFood pottage;
    public static ItemFood rice_ball;
    public static ItemFood stew_trencher;
    public static ItemFood stuffed_pepper;
    public static ItemFood stuffed_pita;
    public static ItemFood sushi_roll;
    public static ItemFood tofu;

    public static Item muffin_dough;
    public static Item manchet_dough;
    public static Item raw_noodle;
    public static Item butter;

    public static Item large_water_bottle;
    public static Item large_milk_bottle;
    public static Item large_soy_milk_bottle;
    public static Item large_empty_bottle;

    /**
     * Get a list of all possible food items.
     *
     * @return a list of food items.
     */
    public static List<ItemFood> getFoodItems()
    {
        return List.of(cabochis,
          cheddar_cheese,
          congee,
          cooked_rice,
          eggplant_dolma,
          feta_cheese,
          flatbread,
          hand_pie,
          lamb_stew,
          lembas_scone,
          manchet_bread,
          muffin,
          pasta_plain,
          pasta_tomato,
          pepper_hummus,
          pita_hummus,
          pottage,
          rice_ball,
          stew_trencher,
          stuffed_pepper,
          stuffed_pita,
          sushi_roll,
          tofu);
    }

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModItems()
    {
        /*
         * Intentionally left empty.
         */
    }
}
