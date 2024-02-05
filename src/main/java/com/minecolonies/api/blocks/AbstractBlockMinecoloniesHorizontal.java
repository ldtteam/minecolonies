package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.neoforged.neoforge.registries.IForgeRegistry;

public abstract class AbstractBlockMinecoloniesHorizontal<B extends AbstractBlockMinecoloniesHorizontal<B>> extends HorizontalDirectionalBlock implements IBlockMinecolonies<B>
{
    public AbstractBlockMinecoloniesHorizontal(final Properties properties)
    {
        super(properties);
    }

    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry, final Item.Properties properties)
    {
        registry.register(getRegistryName(), new BlockItem(this, properties));
    }

    @Override
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(getRegistryName(), this);
        return (B) this;
    }
}
