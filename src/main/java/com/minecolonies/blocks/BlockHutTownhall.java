package com.minecolonies.blocks;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.LanguageHandler;
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
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if(world.isRemote)
        {
            return;
        }
        TileEntity te = world.getTileEntity(x, y, z);

        if(entityLivingBase instanceof EntityPlayer && te instanceof TileEntityColonyBuilding)
        {

            EntityPlayer player = (EntityPlayer)entityLivingBase;
            TileEntityColonyBuilding hut = (TileEntityColonyBuilding) te;
            ColonyManager.createColony(world, hut.getPosition(), player);
        }
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
    }
}
