package com.minecolonies.api.blocks;

import com.minecolonies.api.util.constant.Suppression;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.registries.IForgeRegistry;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Abstract class for minecolonies named graves.
 */
public abstract class AbstractBlockMinecoloniesNamedGrave<B extends AbstractBlockMinecoloniesNamedGrave<B>> extends AbstractBlockMinecolonies<B> implements EntityBlock
{
    /**
     * The direction the block is facing.
     */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AbstractBlockMinecoloniesNamedGrave(final Properties properties)
    {
        super(properties.noOcclusion());
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
    public void registerBlockItem(final IForgeRegistry<Item> registry, final Item.Properties properties)
    {
        registry.register((new BlockItem(this, properties)).setRegistryName(this.getRegistryName()));
    }
}
