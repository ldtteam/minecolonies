package com.minecolonies.api.colony.permissions;

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
}
