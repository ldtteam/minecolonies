package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.CreativeTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class BlockInformator extends Block implements IColoniesBlock, ITileEntityProvider
{
    protected int workingRange;

    @SideOnly(Side.CLIENT)
    private IIcon[] icons = new IIcon[6];// 0 = top, 1 = bot, 2-5 = sides;

    public BlockInformator(Material material)
    {
        super(material);
        setCreativeTab(CreativeTab.mineColoniesTab);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        if(world.isRemote)
            return true;
        entityPlayer.openGui(MineColonies.instance, 0, world, x, y, z);
        return true;
    }

    @SuppressWarnings("UnusedDeclaration") //TODO Check for uses (Inherited from old mod)
    protected int findTopGround(World world, int x, int z)
    {
        //TODO
        return 1;
    }

    @SuppressWarnings("UnusedDeclaration") //TODO Check for uses (Inherited from old mod)
    protected Vec3 scanForBlockNearPoint(World world, Block block, int x, int y, int z, int radiusX, int radiusY, int radiusZ)
    {
        Vec3 entityVec = Vec3.createVectorHelper(x, y, z);

        Vec3 closestVec = null;
        double minDistance = 999999999;

        for(int i = x - radiusX; i <= x + radiusX; i++)
        {
            for(int j = y - radiusY; j <= y + radiusY; j++)
            {
                for(int k = z - radiusZ; k <= z + radiusZ; k++)
                {
                    if(world.getBlock(i, j, k) == block)
                    {
                        Vec3 tempVec = Vec3.createVectorHelper(i, j, k);

                        if(closestVec == null || tempVec.distanceTo(entityVec) < minDistance)
                        {
                            closestVec = tempVec;
                            minDistance = closestVec.distanceTo(entityVec);
                        }
                    }
                }
            }
        }
        return closestVec;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        icons[0] = iconRegister.registerIcon(Constants.MODID.toLowerCase() + ":" + getName() + "top");
        icons[1] = iconRegister.registerIcon(Constants.MODID.toLowerCase() + ":" + getName() + "bot");
        for(int i = 2; i <= 5; i++)
        {
            icons[i] = iconRegister.registerIcon(Constants.MODID.toLowerCase() + ":" + "sideChest");
        }
    }

    @Override
    public IIcon getIcon(int side, int meta)
    {
        return icons[side];
    }
}
