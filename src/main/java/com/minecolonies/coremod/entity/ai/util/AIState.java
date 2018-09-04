package com.minecolonies.coremod.entity.ai.util;

/**
 * Basic state enclosing states all ai's use.
 * Please extend this class with the states your ai needs.
 * And please document each state on what it does.
 */
public enum AIState
{

    /*
###GENERAL###
     */
    /**
     * this is the idle state for the ai.
     * From here on it will start working.
     * Use this state in your ai to start your code.
     */
    IDLE,
    /**
     * This state is only used on ai initialization.
     * It checks if any important things are null.
     */
    INIT,
    /**
     * The ai needs one of the tools.
     */
    NEEDS_TOOL,
    /**
     * Inventory has to be dumped.
     */
    INVENTORY_FULL,
    /**
     * Check for all required items.
     */
    PREPARING,
    /**
     * Start working by walking to the building.
     */
    START_WORKING,
    /**
     * The ai needs some items it is waiting for.
     */
    NEEDS_ITEM,
    /**
     * Start building a Structure.
     */
    START_BUILDING,
    /**
     * Clears the building area.
     */
    CLEAR_STEP,
    /**
     * Cleans the building area.
     */
    REMOVE_STEP,
    /**
     * Requests materials.
     */
    REQUEST_MATERIALS,
    /**
     * Creates the solid structure.
     */
    BUILDING_STEP,
    /**
     * Sets decorative blocks.
     */
    DECORATION_STEP,
    /**
     * Spawns all entities.
     */
    SPAWN_STEP,
    /**
     * Completes the building.
     */
    COMPLETE_BUILD,
    /**
     * Pick up left over items after building.
     */
    PICK_UP_RESIDUALS,
    /**
     * Go search food.
     */
    HUNGRY,
    /**
     * Decide what AIstate to go to next.
     */
    DECIDE,
    /*
###FISHERMAN###
     */
    /**
     * The fisherman is looking for water.
     */
    FISHERMAN_SEARCHING_WATER,
    /**
     * The fisherman has found water and can start fishing.
     */
    FISHERMAN_WALKING_TO_WATER,
    FISHERMAN_CHECK_WATER,
    FISHERMAN_START_FISHING,

    /*
###Lumberjack###
     */
    /**
     * The lumberjack is looking for trees.
     */
    LUMBERJACK_SEARCHING_TREE,
    /**
     * The lumberjack has found trees and can start cutting them.
     */
    LUMBERJACK_CHOP_TREE,
    /**
     * The Lumberjack is gathering saplings.
     */
    LUMBERJACK_GATHERING,
    /**
     * There are no trees in his search range.
     */
    LUMBERJACK_NO_TREES_FOUND,

    /*
###Miner###
     */
    /**
     * Check if there is a mineshaft.
     */
    MINER_CHECK_MINESHAFT,
    /**
     * The Miner searches for the ladder.
     */
    MINER_SEARCHING_LADDER,
    /**
     * The Miner walks to the ladder.
     */
    MINER_WALKING_TO_LADDER,
    /**
     * The Miner mines his shaft.
     */
    MINER_MINING_SHAFT,
    /**
     * The Miner builds his shaft.
     */
    MINER_BUILDING_SHAFT,
    /**
     * The Miner mines one node.
     */
    MINER_MINING_NODE,

    /*
###Builder###
     */

    /**
     * Clears the building area.
     */
    BUILDER_CLEAR_STEP,
    /**
     * Requests materials.
     */
    BUILDER_REQUEST_MATERIALS,
    /**
     * Creates the building structure.
     */
    BUILDER_STRUCTURE_STEP,
    /**
     * Sets decorative blocks.
     */
    BUILDER_DECORATION_STEP,
    /**
     * Completes the building.
     */
    BUILDER_COMPLETE_BUILD,
    /**
     * Pick up all materials he might need.
     */
    PICK_UP,

    /*
###FARMER###
    */

    /**
     * Check if the fields need any work.
     */
    FARMER_CHECK_FIELDS,

    /**
     * Hoe the field.
     */
    FARMER_HOE,

    /**
     * Plant the seeds.
     */
    FARMER_PLANT,

    /**
     * Harvest the crops.
     */
    FARMER_HARVEST,

    /**
     * Looks at the field.
     */
    FARMER_OBSERVE,

      /*
###Guard###
    */

    /**
     * Let the guard search for targets.
     */
    GUARD_SEARCH_TARGET,

    /**
     * Choose a target.
     */
    GUARD_GET_TARGET,

    /**
     *Guard attack target.
     */
    GUARD_ATTACK,

    /**
     * Physically attack the target.
     */
    GUARD_ATTACK_PHYSICAL,

    /**
     * Use a ranged attack against the target.
     */
    GUARD_ATTACK_RANGED,

    /**
     * Allow the guard to protect himself.
     */
    GUARD_ATTACK_PROTECT,

    /**
     * Patrol through the village.
     */
    GUARD_PATROL,

    /**
     * Go back to the hut to "restock".
     */
    GUARD_RESTOCK,

    /**
     * Gather dropped items after kill.
     */
    GUARD_GATHERING,

    /*
###Deliveryman###
    */

    /**
     * Get stuff from the warehouse.
     */
    PREPARE_DELIVERY,

    /**
     * Delivery required items or tools.
     */
    DELIVERY,

    /**
     * Gather not needed items and tools from others.
     */
    GATHERING,

    /**
     * Gather all required items from the warehouse.
     */
    GATHER_IN_WAREHOUSE,

    /**
     * Dump inventory over chests in warehouse.
     */
    DUMPING,

     /*
###Baker###
    */

    /**
     * Knead the dough.
     */
    BAKER_KNEADING,

    /**
     * Bake the dough.
     */
    BAKER_BAKING,

    /**
     * Finish up the product.
     */
    BAKER_FINISHING,

    /**
     * Take the product out of the oven.
     */
    BAKER_TAKE_OUT_OF_OVEN,

    /*
###Furnace users###
     */

    /**
     * smelter smelts ore until its a bar.
     */
    START_USING_FURNACE,

    /**
     * Gathering ore from his building.
     */
    GATHERING_REQUIRED_MATERIALS,

    /**
     * Retrieve the ore from the furnace.
     */
    RETRIEVING_END_PRODUCT_FROM_FURNACE,

    /*
###Cook###
     */

    /**
     * Serve food to the citizen inside the building.
     */
    COOK_SERVE_FOOD_TO_CITIZEN,

    /*
    ###Smelter###
     */
    /**
     * Smelt stuff he finds in his hut to ingots.
     */
    SMELTER_SMELTING_ITEMS,

    /*
### Herders ###
     */

    /**
     * Breed two animals together.
     */
    HERDER_BREED,

    /**
     * Butcher an animal.
     */
    HERDER_BUTCHER,

    /**
     * Pickup items within area.
     */
    HERDER_PICKUP,

    /*
### Cowboy ###
     */

    /**
     * Milk cows!
     */
    COWBOY_MILK,

    /*
### Shepherd ###
     */

    /**
     * Shear a sheep!
     */
    SHEPHERD_SHEAR,

    /*
### Composter ###
     */

    /**
     * Fill up the barrels
     */
    COMPOSTER_FILL,

    /**
     * Take the compost from the barrels
     */
    COMPOSTER_HARVEST,

    /**
     * Gather materials from the building
     */
    GET_MATERIALS,

    /*
### Student ###
     */

    STUDY,
}
