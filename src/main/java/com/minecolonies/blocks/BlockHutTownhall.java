package com.minecolonies.blocks;

import com.minecolonies.colony.ColonyManager;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Hut for the town hall.
 * Sets the working range for the town hall in the constructor
 */
public class BlockHutTownhall extends AbstractBlockHut
{
    protected BlockHutTownhall()
    {
        super();
        //Sets the working range to whatever the config is set to
        this.workingRange = Configurations.workingRangeTownhall;
    }

    @Override
    public String getName()
    {
        return "blockHutTownhall";
    }

    @Override
    public void onBlockPlacedBy(final World worldIn, final int x, final int y, final int z, final EntityLivingBase placer, final ItemStack stack)
    {
        if(worldIn.isRemote)
        {
            return;
        }
        TileEntity te = worldIn.getTileEntity(x,y,z);

        if(placer instanceof EntityPlayer && te instanceof TileEntityColonyBuilding && ColonyManager.getColony(worldIn, x, y, z) == null)
        {

            EntityPlayer player = (EntityPlayer)placer;
            TileEntityColonyBuilding hut = (TileEntityColonyBuilding) te;
            ColonyManager.createColony(worldIn, hut.getPosition(), player);
        }
        super.onBlockPlacedBy(worldIn, x, y, z, placer, stack);    }


}
