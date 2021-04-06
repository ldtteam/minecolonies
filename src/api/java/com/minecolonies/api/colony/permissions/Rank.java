package com.minecolonies.api.colony.permissions;

import java.util.Objects;

public class Rank
{
    private boolean isSubscriber;
    private String name;
    private int id;
    private boolean isInitial;

    public Rank(int id, String name, boolean isSubscriber, boolean isInitial)
    {
        this.id = id;
        this.name = name;
        this.isSubscriber = isSubscriber;
        this.isInitial = isInitial;
    }

    public int getId()
    {
        return id;
    }

    public boolean isSubscriber()
    {
        return isSubscriber;
    }

    public String getName()
    {
        return name;
    }

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
