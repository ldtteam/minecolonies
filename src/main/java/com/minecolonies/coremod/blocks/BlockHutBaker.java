package com.minecolonies.coremod.blocks;

import com.minecolonies.coremod.tileentities.TileEntityBakerBuilding;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the baker.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutBaker extends AbstractBlockHut
{
    protected BlockHutBaker()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBaker";
    }

    @NotNull
    @Override
    public TileEntity createNewTileEntity(final World world, final int meta)
    {
        //Creates a tile entity for our building
        return new TileEntityBakerBuilding();
    }
}
