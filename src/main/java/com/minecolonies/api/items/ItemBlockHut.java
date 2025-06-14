package com.minecolonies.api.items;

import com.minecolonies.api.blocks.AbstractBaseBlockHut;
import com.minecolonies.api.blocks.AbstractBlockHut;
import net.minecraft.world.item.BlockItem;

/**
 * A custom item class for hut blocks.
 */
public class ItemBlockHut extends BlockItem
{
    /**
     * This items block.
     */
    private AbstractBaseBlockHut<?> block;

    /**
     * Creates a new ItemBlockHut representing the item form of the given {@link AbstractBlockHut}.
     * 
     * @param block   the {@link AbstractBlockHut} this item represents.
     * @param builder the item properties to use.
     */
    public ItemBlockHut(AbstractBaseBlockHut<?> block, Properties builder)
    {
        super(block, builder);
        this.block = block;
    }
}
