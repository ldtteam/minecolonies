package com.minecolonies.coremod.blocks;

import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the warehouse.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutDeliveryman extends AbstractBlockHut
{
    protected BlockHutDeliveryman()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutDeliveryman";
    }
}
