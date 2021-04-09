package com.minecolonies.api.colony.permissions;

import java.util.Objects;

public class Rank
{

    /**
     * Whether the rank is automatically a subscriber to certain events
     */
    private boolean isSubscriber;
    /**
     * The name of the rank
     */
    private String name;
    /**
     * The id of the rank
     */
    private int id;
    /**
     * Whether the rank is one of the initial ranks which cannot be deleted
     */
    private boolean isInitial;

    /**
     * Rank constructor
     * @param id the id of the rank
     * @param name the name of the rank
     * @param isSubscriber whether the rank is a subscriber
     * @param isInitial whether the rank is an initial rank
     */
    public Rank(int id, String name, boolean isSubscriber, boolean isInitial)
    {
        this.id = id;
        this.name = name;
        this.isSubscriber = isSubscriber;
        this.isInitial = isInitial;
    }

    /**
     * Get the id of the rank
     * @return the id
     */
    public int getId()
    {
        return id;
    }

    /**
     * Get whether the rank is a subscriber to certain colony events
     * @return true if so
     */
    public boolean isSubscriber()
    {
        return isSubscriber;
    }

    /**
     * Get the name of the rank
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get whether this ranks is an initial rank which cannot be deleted
     * @return true if so
     */
    public boolean isInitial() { return isInitial; }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final Rank rank = (Rank) o;
        return id == rank.id && name.equals(rank.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, id);
    }
}
