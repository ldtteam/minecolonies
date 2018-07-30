package com.minecolonies.coremod.blocks;

import com.minecolonies.coremod.tileentities.TileEntityCompostedDirt;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

//Todo: implement this class

/**
 * Block that if activated with BoneMeal or Compost by an AI will produce flowers by intervals until it deactivates
 */
public class BlockCompostedDirt extends AbstractBlockMinecolonies<BlockCompostedDirt> implements ITileEntityProvider
{
    public BlockCompostedDirt()
    {
        super(Material.GROUND);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(final World world, final int i)
    {
        return new TileEntityCompostedDirt();
    }
}
