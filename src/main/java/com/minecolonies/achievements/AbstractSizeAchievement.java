package com.minecolonies.achievements;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

/**
 * Abstract
 * 
 * @author Isfirs
 * @since 0.1
 */
public abstract class AbstractSizeAchievement extends AbstractAchievement
{
    
    /**
     * The size of the required colony size
     */
    public final int size;
    
    /**
     * Inherit parent constructor
     * 
     * @see AbstractAchievement#AbstractAchievement(String, String, int, int, Item, Achievement)
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     * @param itemIcon
     * @param parent
     * @param size
     */
    public AbstractSizeAchievement(String id, String name, int offsetX, int offsetY, Item itemIcon, Achievement parent, int size)
    {
        super(id, name, offsetX, offsetY, itemIcon, parent);

        this.size = size;
    }
    
    /**
     * Inherit parent constructor
     * 
     * @see AbstractAchievement#AbstractAchievement(String, String, int, int, Block, Achievement)
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     * @param blockIcon
     * @param parent
     * @param size
     */
    public AbstractSizeAchievement(String id, String name, int offsetX, int offsetY, Block blockIcon, Achievement parent, int size)
    {
        super(id, name, offsetX, offsetY, blockIcon, parent);

        this.size = size;
    }
    
    /**
     * Inherit parent constructor
     * 
     * @see AbstractAchievement#AbstractAchievement(String, String, int, int, ItemStack, Achievement)
     * 
     * @param id
     * @param name
     * @param offsetX
     * @param offsetY
     * @param itemStackIcon
     * @param parent
     * @param size
     */
    public AbstractSizeAchievement(String id, String name, int offsetX, int offsetY, ItemStack itemStackIcon, Achievement parent, int size)
    {
        super(id, name, offsetX, offsetY, itemStackIcon, parent);

        this.size = size;
    }
    
    /**
     * Checks the condition and triggers the achievement
     * 
     * @param player
     * @param size
     */
    public void triggerAchievement(EntityPlayer player, int size)
    {
        if (this.compare(size))
        {
            player.triggerAchievement(this);
        }
    }
    
    /**
     * Checks if the param matches the set size
     * 
     * @param compare
     * @return
     */
    protected boolean compare(int compare)
    {
        return compare >= this.size;
    }

}
