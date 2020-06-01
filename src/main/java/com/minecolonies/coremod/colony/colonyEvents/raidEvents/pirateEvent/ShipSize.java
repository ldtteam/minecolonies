package com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent;

import com.minecolonies.api.util.constant.ColonyConstants;

/**
 * Enum for ship sizes.
 */
public enum ShipSize
{
    SMALL(ColonyConstants.SMALL_HORDE_SIZE, ColonyConstants.SMALL_SHIP, ColonyConstants.SMALL_HORDE_MESSAGE_ID, 2, 2, 0, 0),
    MEDIUM(ColonyConstants.MEDIUM_HORDE_SIZE,
      ColonyConstants.MEDIUM_SHIP,
      ColonyConstants.MEDIUM_HORDE_MESSAGE_ID,
      4,
      1, 1, 1),
    BIG(ColonyConstants.BIG_HORDE_SIZE,
      ColonyConstants.BIG_SHIP,
      ColonyConstants.BIG_HORDE_MESSAGE_ID,
      11,
      2, 2, 2);

    /**
     * The ships raidlevel
     */
    public final int raidLevel;

    /**
     * Structure schematic prefix
     */
    public final String schematicPrefix;

    /**
     * Raid message id
     */
    public final int messageID;

    /**
     * Amount of spawners for the ship
     */
    public final int spawnerCount;

    /**
     * The amount of barbarians up to which a small pirate ship spawns
     */
    private static final int SMALL_SHIP_SIZE_AMOUNT = 5;

    /**
     * The amount of barbarians up to which a medium pirate ship spawns
     */
    private static final int MEDIUM_SHIP_SIZE_AMOUNT = 18;


    /**
     * Array of pirates which are spawned for landing, one wave.
     */
    public final int normal;
    public final int archer;
    public final int boss;

    ShipSize(final int raidLevel, final String schematicName, final int messageID, final int spawnerCount, final int normal, final int archer, final int boss)
    {
        this.raidLevel = raidLevel;
        this.schematicPrefix = schematicName;
        this.messageID = messageID;
        this.spawnerCount = spawnerCount;
        this.normal = normal;
        this.archer = archer;
        this.boss = boss;
    }

    /**
     * Returns the right shipsize for the given raidlevel
     *
     * @param raidLevel the raid level.
     * @return the ship size.
     */
    public static ShipSize getShipForRaidLevel(final int raidLevel)
    {
        ShipSize shipSize;
        if (raidLevel <= SMALL_SHIP_SIZE_AMOUNT)
        {
            shipSize = SMALL;
        }
        else if (raidLevel < MEDIUM_SHIP_SIZE_AMOUNT)
        {
            shipSize = MEDIUM;
        }
        else
        {
            shipSize = BIG;
        }
        return BIG;
    }
}
