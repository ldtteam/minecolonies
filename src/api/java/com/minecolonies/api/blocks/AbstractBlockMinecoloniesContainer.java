package com.minecolonies.api.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public abstract class AbstractBlockMinecoloniesContainer<B extends AbstractBlockMinecoloniesContainer<B>> extends AbstractBlockMinecolonies<B>
{
    public AbstractBlockMinecoloniesContainer(final Properties properties)
    {
        super(properties);
    }
}
