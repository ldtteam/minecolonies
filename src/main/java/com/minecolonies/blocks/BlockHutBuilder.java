package com.minecolonies.blocks;

import com.minecolonies.lib.Constants;
import com.minecolonies.tilentities.TileEntityHutBuilder;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutBuilder extends BlockInformator
{
    public final String name = "blockHutBuilder";

    protected BlockHutBuilder()
    {
        super(Material.wood);
        setBlockName(getName());
        GameRegistry.registerBlock(this, getName());
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityHutBuilder();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        TileEntityHutBuilder tileEntityHutBuilder = (TileEntityHutBuilder) world.getTileEntity(x, y, z);
        if(Utils.getDistanceToClosestTownHall(world, x, y, z) < Constants.MAXDISTANCETOTOWNHALL)
        {
            tileEntityHutBuilder.findAndAddClosestTownhall();
            tileEntityHutBuilder.atemptToAddIdleCitizens(tileEntityHutBuilder);
        }
    }
}
