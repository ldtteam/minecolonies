package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.IForgeRegistry;

public interface IBlockMinecolonies<B extends IBlockMinecolonies<B>>
{
    /**
     * Registery block at gameregistry.
     * @param registry the registry to use.
     * @return the block itself.
     */
    B registerBlock(final IForgeRegistry<Block> registry);

    /**
     * Registery block at gameregistry.
     * @param registry the registry to use.
     */
    void registerItemBlock(final IForgeRegistry<Item> registry);
}
