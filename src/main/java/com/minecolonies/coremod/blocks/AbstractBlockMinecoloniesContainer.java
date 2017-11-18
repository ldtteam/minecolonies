package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import net.minecraftforge.fml.common.registry.IForgeRegistry;

public abstract class AbstractBlockMinecoloniesContainer extends BlockContainer
{
    public AbstractBlockMinecoloniesContainer(final Material blockMaterialIn, final MapColor blockMapColorIn)
    {
        super(blockMaterialIn, blockMapColorIn);
    }

    public AbstractBlockMinecoloniesContainer(final Material materialIn)
    {
        super(materialIn);
    }

    /**
     * Registery block at gameregistry.
     * @param registry the registry to use.
     * @return the block itself.
     */
    public AbstractBlockMinecoloniesContainer registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return this;
    }

    /**
     * Registery block at gameregistry.
     * @param registry the registry to use.
     */
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
    }
}
