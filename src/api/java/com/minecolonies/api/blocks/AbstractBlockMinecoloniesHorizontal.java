package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.api.util.constant.Suppression;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class AbstractBlockMinecoloniesHorizontal<B extends AbstractBlockMinecoloniesHorizontal<B>> extends HorizontalBlock implements IBlockMinecolonies<B>
{
    public AbstractBlockMinecoloniesHorizontal(final Material blockMaterialIn)
    {
        super(blockMaterialIn);
    }

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     * @return the block itself.
     */
    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return (B) this;
    }

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     */
    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry)
    {
        registry.register((new BlockItem(this)).setRegistryName(this.getRegistryName()));
    }
}
