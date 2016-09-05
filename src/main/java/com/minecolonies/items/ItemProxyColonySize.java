package com.minecolonies.items;

/**
 * This is a multi used proxy class for the size related achievements.
 * 
 * @since 0.3
 */
public class ItemProxyColonySize extends AbstractItemMinecolonies
{

    private String name;

    public ItemProxyColonySize(String name)
    {
        super(name);
    }

    @Override
    public String getName()
    {
        return name;
    }

}
