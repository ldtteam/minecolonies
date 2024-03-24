package com.minecolonies.api.advancements;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The collection of advancement triggers for minecolonies.
 * Each trigger may correspond to multiple advancements.
 */
public class AdvancementTriggers
{
    public static final DeferredRegister<CriterionTrigger<?>> DEFERRED_REGISTER = DeferredRegister.create(Registries.TRIGGER_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, AllTowersTrigger>            ALL_TOWERS             = DEFERRED_REGISTER.register(Constants.CRITERION_ALL_TOWERS, AllTowersTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, ArmyPopulationTrigger>       ARMY_POPULATION        = DEFERRED_REGISTER.register(Constants.CRITERION_ARMY_POPULATION, ArmyPopulationTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, BuildingAddRecipeTrigger>    BUILDING_ADD_RECIPE    = DEFERRED_REGISTER.register(Constants.CRITERION_BUILDING_ADD_RECIPE, BuildingAddRecipeTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, CitizenBuryTrigger>          CITIZEN_BURY           = DEFERRED_REGISTER.register(Constants.CRITERION_CITIZEN_BURY, CitizenBuryTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, CitizenEatFoodTrigger>       CITIZEN_EAT_FOOD       = DEFERRED_REGISTER.register(Constants.CRITERION_CITIZEN_EAT_FOOD, CitizenEatFoodTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, CitizenResurrectTrigger>     CITIZEN_RESURRECT      = DEFERRED_REGISTER.register(Constants.CRITERION_CITIZEN_RESURRECT, CitizenResurrectTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, ClickGuiButtonTrigger>       CLICK_GUI_BUTTON       = DEFERRED_REGISTER.register(Constants.CRITERION_CLICK_GUI_BUTTON, ClickGuiButtonTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, ColonyPopulationTrigger>     COLONY_POPULATION      = DEFERRED_REGISTER.register(Constants.CRITERION_COLONY_POPULATION, ColonyPopulationTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, CompleteBuildRequestTrigger> COMPLETE_BUILD_REQUEST = DEFERRED_REGISTER.register(Constants.CRITERION_COMPLETE_BUILD_REQUEST, CompleteBuildRequestTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, CreateBuildRequestTrigger>   CREATE_BUILD_REQUEST   = DEFERRED_REGISTER.register(Constants.CRITERION_CREATE_BUILD_REQUEST, CreateBuildRequestTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, DeepMineTrigger>             DEEP_MINE              = DEFERRED_REGISTER.register(Constants.CRITERION_DEEP_MINE, DeepMineTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, MaxFieldsTrigger>            MAX_FIELDS             = DEFERRED_REGISTER.register(Constants.CRITERION_MAX_FIELDS, MaxFieldsTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, OpenGuiWindowTrigger>        OPEN_GUI_WINDOW        = DEFERRED_REGISTER.register(Constants.CRITERION_OPEN_GUI_WINDOW, OpenGuiWindowTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PlaceStructureTrigger>       PLACE_STRUCTURE        = DEFERRED_REGISTER.register(Constants.CRITERION_STRUCTURE_PLACED, PlaceStructureTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PlaceSupplyTrigger>          PLACE_SUPPLY           = DEFERRED_REGISTER.register(Constants.CRITERION_SUPPLY_PLACED, PlaceSupplyTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, UndertakerTotemTrigger>      UNDERTAKER_TOTEM       = DEFERRED_REGISTER.register(Constants.CRITERION_UNDERTAKER_TOTEM, UndertakerTotemTrigger::new);
}
