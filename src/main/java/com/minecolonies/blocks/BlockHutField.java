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

public class BlockHutField extends AbstractBlockHut
{
    protected BlockHutField()
    {
        super();
    }

    @Override
    public String getName() {
        return "blockHutField";
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

        if(placer instanceof EntityPlayer)
        {
            Colony colony = ColonyManager.getColony(worldIn, pos);

            if (colony != null)
            {
                colony.addNewField(pos);
            }
        }
    }
}
