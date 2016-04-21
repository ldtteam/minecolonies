package com.minecolonies.blocks;

import com.minecolonies.colony.ColonyManager;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Hut for the town hall.
 * Sets the working range for the town hall in the constructor
 */
public class BlockHutTownHall extends AbstractBlockHut
{
    protected BlockHutTownHall()
    {
        super();
        //Sets the working range to whatever the config is set to
        this.workingRange = Configurations.workingRangeTownHall;
    }

    @Override
    public String getName()
    {
        return "blockHutTownHall";
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if(worldIn.isRemote)
        {
            return;
        }
        TileEntity te = worldIn.getTileEntity(pos);

        if(placer instanceof EntityPlayer && te instanceof TileEntityColonyBuilding && ColonyManager.getColony(worldIn, pos) == null)
        {
            EntityPlayer player = (EntityPlayer)placer;
            TileEntityColonyBuilding hut = (TileEntityColonyBuilding) te;
            ColonyManager.createColony(worldIn, hut.getPosition(), player);
        }
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }
}
