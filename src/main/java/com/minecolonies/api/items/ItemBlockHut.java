package com.minecolonies.api.items;

import com.minecolonies.api.blocks.AbstractBlockHut;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;

/**
 * A custom item class for hut blocks.
 */
public class ItemBlockHut extends BlockItem implements IMinecoloniesItem
{
    /**
     * The item it's tied block hut.
     */
    private final AbstractBlockHut block;

    /**
     * Creates a new ItemBlockHut representing the item form of the given {@link AbstractBlockHut}.
     *
     * @param block   the {@link AbstractBlockHut} this item represents.
     * @param builder the item properties to use.
     */
    public ItemBlockHut(AbstractBlockHut block, Properties builder)
    {
        super(block, builder);
        this.block = block;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return block.getRegistryName();
    }
}
