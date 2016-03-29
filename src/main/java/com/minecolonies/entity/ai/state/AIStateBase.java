package com.minecolonies.entity.ai.state;

/**
 * Basic state enclosing states all ai's use.
 * Please extend this class with the states your ai needs.
 * And please document each state on what it does.
 */
public enum AIStateBase
{
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
    NEEDS_SHOVEL,
    NEEDS_AXE,
    NEEDS_HOE,
    NEEDS_ROD,
    NEEDS_PICKAXE,
    INVENTORY_FULL,
    /**
     * The ai needs some items it is waiting for.
     */
    NEEDS_ITEM
}
