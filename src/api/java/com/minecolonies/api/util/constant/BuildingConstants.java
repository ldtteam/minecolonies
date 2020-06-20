package com.minecolonies.api.util.constant;

/**
 * Constants regarding buildings.
 */
public final class BuildingConstants
{
    /**
     * The default mac building level.
     */
    public static final int CONST_DEFAULT_MAX_BUILDING_LEVEL = 5;

    /**
     * Tag if the building has no workOrder.
     */
    public static final int NO_WORK_ORDER = -1;

    /**
     * Max priority of a building.
     */
    public static final int MAX_PRIO = 10;

    /**
     * Min slots required to be recognized as storage.
     */
    public static final int  MIN_SLOTS_FOR_RECOGNITION = 5;

    // --------------- Miner building constants ---------------//

    /**
     * The NBT Tag to store the starting level of the shaft.
     */
    public static final String TAG_STARTING_LEVEL = "newStartingLevelShaft";

    /**
     * The NBT Tag to store list of levels.
     */
    public static final String TAG_LEVELS = "levels";

    /**
     * The NBT Tag to store if the shaft has been cleared.
     */
    public static final String TAG_CLEARED = "clearedShaft";

    /**
     * The NBT Tag to store the location of the shaft.
     */
    public static final String TAG_SLOCATION = "shaftLocation";

    /**
     * The NBT Tag to store the vector-x of the shaft.
     */
    public static final String TAG_VECTORX = "vectorx";

    /**
     * The NBT Tag to store the vector-z of the shaft.
     */
    public static final String TAG_VECTORZ = "vectorz";

    /**
     * The NBT Tag to store the location of the cobblestone at the shaft.
     */
    public static final String TAG_CLOCATION = "cobblelocation";

    /**
     * The NBT Tag to store the active node the miner is working on.
     */
    public static final String TAG_ACTIVE = "activeNodeNode";

    /**
     * The NBT Tag to store the active node the miner is working on.
     */
    public static final String TAG_OLD = "oldNodeNode";

    /**
     * The NBT Tag to store the current level the miner is working in.
     */
    public static final String TAG_CURRENT_LEVEL = "currentLevel";

    /**
     * The NBT Tag to store the starting node.
     */
    public static final String TAG_SN = "StartingNode";

    /**
     * The NBT Tag to store the location of the ladder.
     */
    public static final String TAG_LLOCATION = "ladderlocation";

    /**
     * The NBT Tag to store if a ladder has been found yet.
     */
    public static final String TAG_LADDER = "found_ladder";

    /**
     * Max depth the miner reaches at level 0.
     */
    public static final int MAX_DEPTH_LEVEL_0 = 70;

    /**
     * Max depth the miner reaches at level 1.
     */
    public static final int MAX_DEPTH_LEVEL_1 = 50;

    /**
     * Max depth the miner reaches at level 2.
     */
    public static final int MAX_DEPTH_LEVEL_2 = 30;

    /**
     * Max depth the miner reaches at level 3.
     */
    public static final int MAX_DEPTH_LEVEL_3 = 5;

    /**
     * Florist flower list filter.
     */
    public static final String FLORIST_FLOWER_LIST = "flowers";

    /**
     * Private constructor to hide implicit public one.
     */
    private BuildingConstants()
    {
        /**
         * Intentionally left empty.
         */
    }
}
