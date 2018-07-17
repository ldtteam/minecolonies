package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import org.jetbrains.annotations.NotNull;

public class BlockHutComposter extends AbstractBlockHut<BlockHutComposter>
{
    public BlockHutComposter()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName(){return "blockHutComposter";}
}
