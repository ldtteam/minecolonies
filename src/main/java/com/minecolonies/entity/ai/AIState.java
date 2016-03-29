package com.minecolonies.entity.ai;

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

    /*
###FISHERMAN###
     */
    /**
     * The fisherman is looking for water
     */
    FISHERMAN_SEARCHING_WATER,
    /**
     * The fisherman has found water and can start fishing
     */
    FISHERMAN_WATER_FOUND,
    FISHERMAN_CHECK_WATER,
    FISHERMAN_START_FISHING,
}
