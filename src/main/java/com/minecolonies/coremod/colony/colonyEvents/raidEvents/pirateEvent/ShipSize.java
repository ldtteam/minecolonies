package com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent;

import com.minecolonies.api.util.constant.ColonyConstants;
import net.minecraft.entity.EntityType;

import static com.minecolonies.api.entity.ModEntities.*;

/**
 * Enum for ship sizes.
 */
public enum ShipSize
{
    SMALL(ColonyConstants.SMALL_HORDE_SIZE, ColonyConstants.SMALL_PIRATE_SHIP, ColonyConstants.SMALL_HORDE_MESSAGE_ID, 1, PIRATE, PIRATE),
    MEDIUM(ColonyConstants.MEDIUM_HORDE_SIZE,
      ColonyConstants.MEDIUM_PIRATE_SHIP,
      ColonyConstants.MEDIUM_HORDE_MESSAGE_ID,
      3,
      PIRATE,
      ARCHERPIRATE,
      CHIEFPIRATE),
    BIG(ColonyConstants.BIG_HORDE_SIZE,
      ColonyConstants.BIG_PIRATE_SHIP,
      ColonyConstants.BIG_HORDE_MESSAGE_ID,
      11,
      PIRATE,
      PIRATE,
      ARCHERPIRATE,
      ARCHERPIRATE,
      CHIEFPIRATE);

    /**
     * The ships raidlevel
     */
    public final int raidLevel;

    /**
     * Structure schematic name
     */
    public final String schematicName;

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
    public final EntityType<?>[] pirates;

    ShipSize(final int raidLevel, final String schematicName, final int messageID, final int spawnerCount, final EntityType<?>... pirates)
    {
        this.raidLevel = raidLevel;
        this.schematicName = schematicName;
        this.messageID = messageID;
        this.spawnerCount = spawnerCount;
        this.pirates = pirates;
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
        return shipSize;
    }
}
