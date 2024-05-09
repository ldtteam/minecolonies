package com.minecolonies.api.util.constant;

import com.minecolonies.api.colony.IColony;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;
import java.util.Random;

/**
 * Colony wide constants.
 */
public final class ColonyConstants
{
    /**
     * The colony data version
     */
    public static final int    DATA_VERSION     = 1;
    public static final String DATA_VERSION_TAG = "data_version";

    /**
     * Shared random
     */
    public static final Random rand = new Random();

    /**
     * Constant string id of our ticket type.
     */
    private static final String TICKET_ID = Constants.MOD_ID + ":" + "initial_chunkload";

    /**
     * Specific ticket type for minecolonies tickets.
     */
    public static final TicketType<ChunkPos> KEEP_LOADED_TYPE = TicketType.create(TICKET_ID, Comparator.comparingLong(ChunkPos::toLong));

    //  Settings

    /**
     * The colony name prefix. Use {@link IColony#getTeamName()} to access the full name.
     */
    public static final String TEAM_COLONY_NAME = "c";

    /**
     * Amount of ticks to wait until checking if a waypoint is still valid.
     */
    public static final int CHECK_WAYPOINT_EVERY = 100;

    /**
     * How often the subscribers get updated in ticks.
     */
    public static final int UPDATE_SUBSCRIBERS_INTERVAL = 20;

    /**
     * How often the travelers get updated in ticks.
     */
    public static final int UPDATE_TRAVELING_INTERVAL = 20;

    /**
     * How often the colony state gets updated in ticks.
     */
    public static final int UPDATE_STATE_INTERVAL   = 100;
    /**
     * How often the colony request system gets updated in ticks.
     */
    public static final int UPDATE_RS_INTERVAL      = 11;
    /**
     * How often the colony updates day/nighttime in ticks.
     */
    public static final int UPDATE_DAYTIME_INTERVAL = 20;

    /**
     * Max amount of permission/colony events to store in the colony.
     */
    public static final int MAX_COLONY_EVENTS = 100;

    /**
     * Barbarian Constants.
     */
    public static final double           ARCHER_BARBARIANS_MULTIPLIER = 0.30;
    public static final double           CHIEF_BARBARIANS_MULTIPLIER  = 0.1;

    /**
     * Different horde ids and their sizes.
     */
    public static final int SMALL_HORDE_MESSAGE_ID  = 1;
    public static final int MEDIUM_HORDE_MESSAGE_ID = 2;
    public static final int BIG_HORDE_MESSAGE_ID    = 3;
    public static final int HUGE_HORDE_MESSAGE_ID   = 4;
    public static final int SMALL_HORDE_SIZE        = 5;
    public static final int MEDIUM_HORDE_SIZE       = 10;
    public static final int BIG_HORDE_SIZE          = 20;

    /**
     * Pirate Constants.
     */
    public static final String SMALL_SHIP  = "small_";
    public static final String MEDIUM_SHIP = "medium_";
    public static final String BIG_SHIP    = "big_";

    public static final long ONE_HOUR_IN_MILLIS = 3600000;
    public static final int CHUNK_UNLOAD_DELAY = 20 * 60 * 10;

    /**
     * Private constructor to hide the implicit one.
     */
    private ColonyConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
