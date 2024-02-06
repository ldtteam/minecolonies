package com.minecolonies.api.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesContainer;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public abstract class AbstractBlockMinecoloniesDefault<B extends AbstractBlockMinecoloniesDefault<B>> extends AbstractBlockMinecoloniesContainer<B>
{
    /**
     * The position it faces.
     */
    public static final DirectionProperty FACING           = HorizontalDirectionalBlock.FACING;
    /**
     * Hardness of the block.
     */
    public static final float             HARDNESS         = 10F;
    /**
     * Resistance of the block.
     */
    public static final float             RESISTANCE       = 10F;
    /**
     * Start of the collision box at y.
     */
    public static final double            BOTTOM_COLLISION = 0.0;
    /**
     * Start of the collision box at x and z.
     */
    public static final double            START_COLLISION  = 0.1;
    /**
     * End of the collision box.
     */
    public static final double            END_COLLISION    = 0.9;
    /**
     * Height of the collision box.
     */
    public static final double            HEIGHT_COLLISION = 2.2;
    /**
     * Registry name for this block.
     */
    public static final String            REGISTRY_NAME    = "blockhutfield";

    public AbstractBlockMinecoloniesDefault(final Properties properties)
    {
        super(properties);
    }

    @Override
    public B registerBlock(final Registry<Block> registry)
    {
        Registry.register(registry, getRegistryName(), this);
        return (B) this;
    }

    @Override
    public void registerBlockItem(final Registry<Item> registry, final Item.Properties properties)
    {
        Registry.register(registry, getRegistryName(), new BlockItem(this, properties));
    }
}
