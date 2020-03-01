package com.minecolonies.api.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Disease storage class.
 */
public class Disease
{
    /**
     * The name string.
     */
    private final String name;

    /**
     * The rarity modifier.
     */
    private final int rarity;

    /**
     * The cure.
     */
    private final List<ItemStack> cure;

    /**
     * Create a disease.
     * @param name the name of it.
     * @param rarity its rarity.
     * @param cure the cure.
     */
    public Disease(final String name, final int rarity, final List<ItemStack> cure)
    {
        this.name = name;
        this.rarity = rarity;
        this.cure = cure;
    }

    /**
     * Get the name of the disease.
     * @return the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the rarity modifier of the disease.
     * @return the rarity.
     */
    public int getRarity()
    {
        return rarity;
    }

    /**
     * Get the cure list.
     * @return the cure.
     */
    public List<ItemStack> getCure()
    {
        return ImmutableList.copyOf(cure);
    }

    /**
     * The Cure String.
     * @return the cure string.
     */
    public String getCureString()
    {
        StringBuilder cureString = new StringBuilder();
        for (final ItemStack cureStack : cure)
        {
            cureString.append(cureStack.getDisplayName().getString());
            cureString.append("+");
        }
        cureString.deleteCharAt(cureString.length()-1);
        return cureString.toString();
    }
}
