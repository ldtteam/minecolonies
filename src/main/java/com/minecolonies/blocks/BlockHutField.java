package com.minecolonies.blocks;

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
        if(worldIn.isRemote)
        {
            return;
        }
        TileEntity te = worldIn.getTileEntity(pos);


        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
}
