package com.minecolonies.api.colony.permissions;

import java.util.UUID;

/**
 * Player within a colony.
 */
public class ColonyPlayer
{
    private final UUID   id;
    private final String name;
    private       Rank   rank;

    /**
     * Instantiates our own player object.
     *
     * @param id   id of the player.
     * @param name name of the player
     * @param rank rank of the player.
     */
    public ColonyPlayer(final UUID id, final String name, final Rank rank)
    {
        this.id = id;
        this.name = name;
        this.rank = rank;
    }

    /**
     * @return The UUID of the player.
     */
    public UUID getID()
    {
        return id;
    }

    /**
     * @return The player's current name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return The player's current rank.
     */
    public Rank getRank()
    {
        return rank;
    }

    /**
     * Setter for the Rank of the player.
     *
     * @param rank The new Rank.
     */
    public void setRank(final Rank rank)
    {
        this.rank = rank;
    }
}
