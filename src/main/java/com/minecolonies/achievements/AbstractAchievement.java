package com.minecolonies.achievements;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

/**
 * Abstraction of the {@link Achievement} class.
 *
 * @author Isfirs
 * @since 0.1
 */
public class AbstractAchievement extends Achievement
{
    public AbstractAchievement(final String id,
                               final String name,
                               final int offsetX,
                               final int offsetY,
                               final Item itemIcon,
                               final Achievement parent)
    {
        super(id, name, offsetX, offsetY, itemIcon, parent);
    }

    public AbstractAchievement(final String id,
                               final String name,
                               final int offsetX,
                               final int offsetY,
                               final Block blockIcon,
                               final Achievement parent)
    {
        super(id, name, offsetX, offsetY, blockIcon, parent);
    }

    public AbstractAchievement(final String id,
                               final String name,
                               final int offsetX,
                               final int offsetY,
                               final ItemStack itemStackIcon,
                               final Achievement parent)
    {
        super(id, name, offsetX, offsetY, itemStackIcon, parent);
    }
}
