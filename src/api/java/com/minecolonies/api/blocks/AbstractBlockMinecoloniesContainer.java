package com.minecolonies.api.blocks;

public abstract class AbstractBlockMinecoloniesContainer<B extends AbstractBlockMinecoloniesContainer<B>> extends AbstractBlockMinecolonies<B>
{
    public AbstractBlockMinecoloniesContainer(final Properties properties)
    {
        super(properties);
    }
}
