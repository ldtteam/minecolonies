package com.minecolonies.achievements;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;

/**
 * Abstract
 * 
 * @author Isfirs
 * @since 0.2
 */
public abstract class AbstractSizeAchievement extends AbstractAchievement
{

    /**
     * Inherit parent constructor
     * 
     * @see AbstractAchievement#AbstractAchievement(String, String, int, int,
     *      Item, Achievement)
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     * @param itemIcon
     * @param parent
     * @param size
     */
    public AbstractSizeAchievement(String id, String name, int offsetX, int offsetY, Item itemIcon, Achievement parent)
    {
        super(id, name, offsetX, offsetY, itemIcon, parent);
    }

    /**
     * Inherit parent constructor
     * 
     * @see AbstractAchievement#AbstractAchievement(String, String, int, int,
     *      Block, Achievement)
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     * @param blockIcon
     * @param parent
     * @param size
     */
    public AbstractSizeAchievement(String id, String name, int offsetX, int offsetY, Block blockIcon, Achievement parent)
    {
        super(id, name, offsetX, offsetY, blockIcon, parent);
    }

    /**
     * Inherit parent constructor
     * 
     * @see AbstractAchievement#AbstractAchievement(String, String, int, int,
     *      ItemStack, Achievement)
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     * @param itemStackIcon
     * @param parent
     * @param size
     */
    public AbstractSizeAchievement(String id, String name, int offsetX, int offsetY, ItemStack itemStackIcon, Achievement parent)
    {
        super(id, name, offsetX, offsetY, itemStackIcon, parent);
    }

}
