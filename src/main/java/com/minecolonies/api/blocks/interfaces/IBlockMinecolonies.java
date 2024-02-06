package com.minecolonies.api.blocks.interfaces;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface IBlockMinecolonies<B extends IBlockMinecolonies<B>>
{
    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     * @return the block itself.
     */
    B registerBlock(final Registry<Block> registry);

    /**
     * Registery block at gameregistry.
     *
     * @param registry   the registry to use.
     * @param properties the item properties.
     */
    void registerBlockItem(final Registry<Item> registry, final Item.Properties properties);

    /**
     * Get the registry name of the block.
     * @return the registry name.
     */
    ResourceLocation getRegistryName();
}
