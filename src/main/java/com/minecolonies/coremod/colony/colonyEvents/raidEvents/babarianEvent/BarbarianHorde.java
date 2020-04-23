package com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent;

import net.minecraft.nbt.CompoundNBT;

import static com.minecolonies.api.util.constant.ColonyConstants.*;

/**
 * Class representing a horde of barbarians.
 */
public class BarbarianHorde
{
    /**
     * NBT Tags
     */
    public static final String TAG_NUMBEROFRAIDERS = "horde_numberOfRaiders";
    public static final String TAG_NUMBEROFARCHERS = "horde_numberOfArchers";
    public static final String TAG_NUMBEROFBOSSES  = "horde_numberOfBosses";
    public static final String TAG_HORDESIZE       = "horde_hordeSize";

    /**
     * Amount of melee barbarians
     */
    public int numberOfRaiders;

    /**
     * Amount of archer barbarians
     */
    public int numberOfArchers;

    /**
     * Amount of boss barbarians
     */
    public int numberOfBosses;

    /**
     * The total amount of raiders
     */
    public int hordeSize;

    /**
     * Create a new horde.
     *
     * @param hordeSize total amount of raiders.
     */
    public BarbarianHorde(final int hordeSize)
    {
        numberOfBosses = Math.max(1, (int) (hordeSize * CHIEF_BARBARIANS_MULTIPLIER));
        numberOfArchers = Math.max(1, (int) (hordeSize * ARCHER_BARBARIANS_MULTIPLIER));
        numberOfRaiders = Math.max(hordeSize - numberOfBosses - numberOfArchers, 0);

        this.hordeSize = numberOfArchers + numberOfRaiders + numberOfBosses;
    }

    /**
     * Gets the barbarian message id
     *
     * @return
     */
    public int getMessageID()
    {
        int id = HUGE_HORDE_MESSAGE_ID;
        if (hordeSize < SMALL_HORDE_SIZE)
        {
            id = SMALL_HORDE_MESSAGE_ID;
        }
        else if (hordeSize < MEDIUM_HORDE_SIZE)
        {
            id = MEDIUM_HORDE_MESSAGE_ID;
        }
        else if (hordeSize < BIG_HORDE_SIZE)
        {
            id = BIG_HORDE_MESSAGE_ID;
        }
        return id;
    }

    /**
     * Write the horde to the given nbt compound
     *
     * @param compound the compound to write it to.
     */
    public void writeToNbt(final CompoundNBT compound)
    {
        compound.putInt(TAG_NUMBEROFRAIDERS, numberOfRaiders);
        compound.putInt(TAG_NUMBEROFARCHERS, numberOfArchers);
        compound.putInt(TAG_NUMBEROFBOSSES, numberOfBosses);
        compound.putInt(TAG_HORDESIZE, hordeSize);
    }

    /**
     * Create a new horde from the nbt compound
     *
     * @param compound the compound to load it from.
     * @return the loaded horde.
     */
    public static BarbarianHorde loadFromNbt(final CompoundNBT compound)
    {
        if (!compound.contains(TAG_HORDESIZE))
        {
            return null;
        }

        BarbarianHorde horde = new BarbarianHorde(compound.getInt(TAG_HORDESIZE));
        horde.numberOfRaiders = compound.getInt(TAG_NUMBEROFRAIDERS);
        horde.numberOfArchers = compound.getInt(TAG_NUMBEROFARCHERS);
        horde.numberOfBosses = compound.getInt(TAG_NUMBEROFBOSSES);
        horde.hordeSize = horde.numberOfArchers + horde.numberOfRaiders + horde.numberOfBosses;
        return horde;
    }
}
