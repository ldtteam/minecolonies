package com.minecolonies.coremod.achievements;

import net.minecraft.advancements.Advancement;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * This class is the superclass of all our achievements.
 * <p>
 * Constructors exist to make creating achievements easy.
 *
 * @since 0.2
 */
public class MineColoniesAchievement implements IMineColoniesAchievement
{
    //todo this will make new advancements at some point
    /**
     * Create a new Achievement.
     *  @param id       the unique id this achievement should have
     * @param offsetX  X position placement on the board
     * @param offsetY  Y position placement on the board
     * @param itemIcon The icon to show
     * @param parent   the parent achievement
     */
    public MineColoniesAchievement(final String id, final int offsetX, final int offsetY, final Block itemIcon, final IMineColoniesAchievement parent)
    {
        //super(Constants.MOD_ID + "." + id, id, offsetX, offsetY, itemIcon, parent);
    }

    /**
     * Create a new Achievement.
     *  @param id        the unique id this achievement should have
     * @param offsetX   X position placement on the board
     * @param offsetY   Y position placement on the board
     * @param blockIcon The icon to show
     * @param parent    the parent achievement
     */
    public MineColoniesAchievement(final String id, final int offsetX, final int offsetY, final Item blockIcon, final IMineColoniesAchievement parent)
    {
        //super(Constants.MOD_ID + "." + id, id, offsetX, offsetY, blockIcon, parent);
    }

    /**
     * Create a new Achievement.
     *
     * @param id            the unique id this achievement should have
     * @param offsetX       X position placement on the board
     * @param offsetY       Y position placement on the board
     * @param itemStackIcon The icon to show
     * @param parent        the parent achievement
     */
    public MineColoniesAchievement(final String id, final int offsetX, final int offsetY, final ItemStack itemStackIcon, final Advancement parent)
    {
        //super(Constants.MOD_ID + "." + id, id, offsetX, offsetY, itemStackIcon, parent);
    }
}
