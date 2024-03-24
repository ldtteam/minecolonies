package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;

public abstract class AbstractBlockMinecoloniesDirectional<B extends AbstractBlockMinecoloniesDirectional<B>> extends DirectionalBlock implements IBlockMinecolonies<B>
{
    public AbstractBlockMinecoloniesDirectional(final Properties properties)
    {
        super(properties);
    }

    @Override
    public void registerBlockItem(final Registry<Item> registry, final Item.Properties properties)
    {
        Registry.register(registry, getRegistryName(), new BlockItem(this, properties));
    }

    @Override
    public B registerBlock(final Registry<Block> registry)
    {
        Registry.register(registry, getRegistryName(), this);
        return (B) this;
    }
}
