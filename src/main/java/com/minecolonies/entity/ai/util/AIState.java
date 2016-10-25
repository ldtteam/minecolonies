package com.minecolonies.entity.ai.util;

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
    NEEDS_SHOVEL,
    NEEDS_AXE,
    NEEDS_HOE,
    NEEDS_PICKAXE,
    NEEDS_WEAPON,
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
     * Hunt the target down.
     */
    GUARD_HUNT_DOWN_TARGET,

    /**
     * Patrol through the village.
     */
    GUARD_PATROL,

    /**
     * Go back to the hut to "restock".
     */
    GUARD_RESTOCK
}
