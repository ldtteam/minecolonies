package com.minecolonies.api.colony.permissions;

import com.minecolonies.api.util.Utils;

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
     * Whether the rank is a colony manager (can perform certain actions)
     */
    private boolean isColonyManager;

    /**
     * Whether the rank is hostile (can attack and be attacked)
     */
    private boolean isHostile;

    /**
     * Holds all bits indicating given permissions
     */
    private long permissionData = 0;

    /**
     * Rank constructor
     *
     * @param id           the id of the rank
     * @param name         the name of the rank
     * @param isSubscriber whether the rank is a subscriber
     * @param isInitial    whether the rank is an initial rank
     */
    public Rank(int id, long permissionData, String name, boolean isSubscriber, boolean isInitial, boolean isColonyManager, boolean isHostile)
    {
        this.id = id;
        this.permissionData = permissionData;
        this.name = name;
        this.isSubscriber = isSubscriber;
        this.isInitial = isInitial;
        this.isColonyManager = isColonyManager;
        this.isHostile = isHostile;
    }

    public Rank(int id, String name, boolean isSubscriber, boolean isInitial)
    {
        this(id, 0L, name, isSubscriber, isInitial, false, false);
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

    /**
     * Get whether this rank is a colony manager
     * @return true if so
     */
    public boolean isColonyManager() { return isColonyManager; }

    /**
     * Get whether this rank is hostile
     * @return true if so
     */
    public boolean isHostile() { return isHostile; }

    /**
     * Set whether this rank is a colony manager
     * @param isColonyManager whether the rank is a colony manager
     */
    public void setColonyManager(boolean isColonyManager)
    {
        this.isColonyManager = isColonyManager;
    }

    /**
     * Set whether this rank is hostile
     * @param isHostile whether the rank is hostile
     */
    public void setHostile(boolean isHostile)
    {
        this.isHostile = isHostile;
    }

    /**
     * Set whether this rank is a subscriber (receives certain colony events)
     * @param isSubscriber whether the rank is a subscriber
     */
    public void setSubscriber(boolean isSubscriber) { this.isSubscriber = isSubscriber; }

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

    public int compareTo(Rank rank)
    {
        return this.getId() - rank.getId();
    }

    /**
     * Adds the given action permission if it is not set yet
     *
     * @param action
     * @return
     */
    public boolean addPermission(final Action action)
    {
        if (!Utils.testFlag(permissionData, action.getFlag()))
        {
            permissionData = Utils.setFlag(permissionData, action.getFlag());
            return true;
        }

        return false;
    }

    /**
     * Adds the given action permission if it is not set yet
     *
     * @param action
     * @return
     */
    public boolean removePermission(final Action action)
    {
        if (Utils.testFlag(permissionData, action.getFlag()))
        {
            permissionData = Utils.unsetFlag(permissionData, action.getFlag());
            return true;
        }

        return false;
    }

    public long getPermissions()
    {
        return permissionData;
    }
}
