package com.minecolonies.blocks;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Hut for the Farmer.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutFarmer extends AbstractBlockHut
{
    protected BlockHutFarmer()
    {
        //No different from Abstract parent
        super();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        /*
        Only work on server side
        */
        if(worldIn.isRemote)
        {
            return;
        }

        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(placer instanceof EntityPlayer && tileEntity instanceof TileEntityColonyBuilding)
        {
            TileEntityColonyBuilding hut = (TileEntityColonyBuilding) tileEntity;
            Colony colony = ColonyManager.getColony(worldIn, hut.getPosition());

            if (colony != null)
            {
                colony.addNewBuilding(hut);
            }
        }
    }

    @Override
    public String getName()
    {
        return "blockHutFarmer";
    }
}
