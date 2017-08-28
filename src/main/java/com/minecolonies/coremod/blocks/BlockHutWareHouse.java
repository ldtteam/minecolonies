package com.minecolonies.coremod.blocks;

import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the warehouse.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutWareHouse extends AbstractBlockHut
{
    protected BlockHutWareHouse()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutWareHouse";
    }

    @NotNull
    @Override
    public TileEntity createNewTileEntity(final World world, final int meta)
    {
        //Creates a tile entity for our building
        return new TileEntityWareHouse();
    }
}
