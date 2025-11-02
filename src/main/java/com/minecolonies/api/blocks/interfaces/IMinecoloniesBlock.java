package com.minecolonies.api.blocks.interfaces;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;

public interface IMinecoloniesBlock<T extends BlockItem>
{
    /**
     * Get the registry name of the block.
     *
     * @return the registry name.
     */
    ResourceLocation getRegistryName();

    /**
     * Creates the block item class used for this block.
     *
     * @return the block item instance.
     */
    T createBlockItem();
}
