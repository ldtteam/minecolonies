package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.world.level.block.DirectionalBlock;

public abstract class AbstractBlockMinecoloniesDirectional<B extends AbstractBlockMinecoloniesDirectional<B>> extends DirectionalBlock implements IBlockMinecolonies<B>
{
    public AbstractBlockMinecoloniesDirectional(final Properties properties)
    {
        super(properties);
    }
}
