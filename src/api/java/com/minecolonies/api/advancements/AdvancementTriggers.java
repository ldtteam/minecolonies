package com.minecolonies.api.advancements;

import com.minecolonies.api.advancements.building_add_recipe.BuildingAddRecipeTrigger;
import com.minecolonies.api.advancements.citizen_eat_food.CitizenEatFoodTrigger;
import com.minecolonies.api.advancements.click_gui_button.ClickGuiButtonTrigger;
import com.minecolonies.api.advancements.colony_population.ColonyPopulationTrigger;
import com.minecolonies.api.advancements.complete_build_request.CompleteBuildRequestTrigger;
import com.minecolonies.api.advancements.create_build_request.CreateBuildRequestTrigger;
import com.minecolonies.api.advancements.open_gui_window.OpenGuiWindowTrigger;
import com.minecolonies.api.advancements.place_structure.PlaceStructureTrigger;
import com.minecolonies.api.advancements.place_supply.PlaceSupplyTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public class AdvancementTriggers
{
    public static final PlaceSupplyTrigger PLACE_SUPPLY = new PlaceSupplyTrigger();
    public static final PlaceStructureTrigger PLACE_STRUCTURE = new PlaceStructureTrigger();
    public static final CreateBuildRequestTrigger CREATE_BUILD_REQUEST = new CreateBuildRequestTrigger();
    public static final OpenGuiWindowTrigger OPEN_GUI_WINDOW = new OpenGuiWindowTrigger();
    public static final ClickGuiButtonTrigger CLICK_GUI_BUTTON = new ClickGuiButtonTrigger();
    public static final CitizenEatFoodTrigger CITIZEN_EAT_FOOD = new CitizenEatFoodTrigger();
    public static final BuildingAddRecipeTrigger BUILDING_ADD_RECIPE = new BuildingAddRecipeTrigger();
    public static final CompleteBuildRequestTrigger COMPLETE_BUILD_REQUEST = new CompleteBuildRequestTrigger();
    public static final ColonyPopulationTrigger COLONY_POPULATION = new ColonyPopulationTrigger();

    public static void preInit()
    {
        CriteriaTriggers.register(PLACE_SUPPLY);
        CriteriaTriggers.register(PLACE_STRUCTURE);
        CriteriaTriggers.register(CREATE_BUILD_REQUEST);
        CriteriaTriggers.register(OPEN_GUI_WINDOW);
        CriteriaTriggers.register(CLICK_GUI_BUTTON);
        CriteriaTriggers.register(CITIZEN_EAT_FOOD);
        CriteriaTriggers.register(BUILDING_ADD_RECIPE);
        CriteriaTriggers.register(COMPLETE_BUILD_REQUEST);
        CriteriaTriggers.register(COLONY_POPULATION);
    }

}
