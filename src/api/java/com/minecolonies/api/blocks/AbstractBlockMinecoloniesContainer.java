package com.minecolonies.api.blocks;

import com.minecolonies.api.util.constant.Suppression;
import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class AbstractBlockMinecoloniesContainer<B extends AbstractBlockMinecoloniesContainer<B>> extends AbstractBlockMinecolonies<B>
{
    public AbstractBlockMinecoloniesContainer(final Properties properties)
    {
        super(properties);
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
}
