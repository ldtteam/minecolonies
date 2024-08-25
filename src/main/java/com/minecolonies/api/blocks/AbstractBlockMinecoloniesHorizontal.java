package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public abstract class AbstractBlockMinecoloniesHorizontal<B extends AbstractBlockMinecoloniesHorizontal<B>> extends HorizontalDirectionalBlock implements IBlockMinecolonies<B>
{
    public AbstractBlockMinecoloniesHorizontal(final Properties properties)
    {
        super(properties);
    }
}
