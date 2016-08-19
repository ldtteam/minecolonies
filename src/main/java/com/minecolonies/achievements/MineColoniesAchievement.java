package com.minecolonies.achievements;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

/**
 * A subclass for this mod.
 *
 * May contain custom logic for achievements.
 *
 * @author Isfirs
 * @since 0.2
 */
public class MineColoniesAchievement extends Achievement
{

    /**
     * Inherit parent constructor.
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     * @param itemIcon
     * @param parent
     */
    public MineColoniesAchievement(final String id, final String name, final int offsetX, final int offsetY, final Item itemIcon, final Achievement parent)
    {
        super(id, name, offsetX, offsetY, itemIcon, parent);
    }

    /**
     * Parent constructor.
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     * @param blockIcon
     * @param parent
     */
    public MineColoniesAchievement(final String id, final String name, final int offsetX, final int offsetY, final Block blockIcon, final Achievement parent)
    {
        super(id, name, offsetX, offsetY, blockIcon, parent);
    }

    /**
     * Parent constructor.
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     * @param itemStackIcon
     * @param parent
     */
    public MineColoniesAchievement(final String id, final String name, final int offsetX, final int offsetY, final ItemStack itemStackIcon, final Achievement parent)
    {
        super(id, name, offsetX, offsetY, itemStackIcon, parent);
    }
}
