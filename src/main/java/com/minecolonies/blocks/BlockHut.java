package com.minecolonies.blocks;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.lib.Constants;
import com.minecolonies.lib.IColony;
import com.minecolonies.tileentities.TileEntityBuildable;
import com.minecolonies.tileentities.TileEntityHutWorker;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public abstract class BlockHut extends Block implements IColony, ITileEntityProvider
{
    protected int workingRange;

    private IIcon[] icons = new IIcon[6];// 0 = top, 1 = bot, 2-5 = sides;

    public BlockHut()
    {
        super(Material.wood);
        setBlockName(getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setResistance(1000f);
        GameRegistry.registerBlock(this, getName());
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        icons[0] = iconRegister.registerIcon(Constants.MODID + ":" + getName() + "Top");
        icons[1] = icons[0];
        for(int i = 2; i <= 5; i++)
        {
            icons[i] = iconRegister.registerIcon(Constants.MODID + ":" + "sideChest");
        }
    }

    @Override
    public IIcon getIcon(int side, int meta)
    {
        return icons[side];
    }

    /**
     * Sets the TileEntities townhall to the closest townhall
     *
     * @param world world
     * @param x     x coordinate
     * @param y     y coordinate
     * @param z     z coordinate
     */
    public void addClosestTownhall(World world, int x, int y, int z)
    {
        TileEntityTownHall tileEntityTownHall = Utils.getClosestTownHall(world, x, y, z);
        if(tileEntityTownHall != null)
        {
            if(world.getTileEntity(x, y, z) instanceof TileEntityBuildable)
            {
                TileEntityBuildable tileEntityBuildable = (TileEntityBuildable) world.getTileEntity(x, y, z);
                tileEntityBuildable.setTownHall(tileEntityTownHall); //TODO, check for owner first
                if(world.getTileEntity(x, y, z) instanceof TileEntityHutWorker)
                {
                    ((TileEntityHutWorker) world.getTileEntity(x, y, z)).attemptToAddIdleCitizen(tileEntityTownHall);
                }
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if(world.isRemote) return;

        if(entityLivingBase instanceof EntityPlayer && !(world.getTileEntity(x, y, z) instanceof TileEntityTownHall))
        {
            TileEntityBuildable tileEntityBuildable = (TileEntityBuildable) world.getTileEntity(x, y, z);
            TileEntityTownHall tileEntityTownHall = Utils.getTownhallByOwner(world, (EntityPlayer) entityLivingBase);
            if(tileEntityTownHall == null || Utils.getDistanceToTileEntity(x, y, z, tileEntityTownHall) > Configurations.workingRangeTownhall)
            {
                if(tileEntityTownHall == null)
                    LanguageHandler.sendPlayerLocalizedMessage((EntityPlayer) entityLivingBase, "tile.blockHut.messageNoTownhall");
                else
                    LanguageHandler.sendPlayerLocalizedMessage((EntityPlayer) entityLivingBase, "tile.blockHut.messageTooFarFromTownhall");
                world.setBlockToAir(x, y, z);
                return;
            }
            tileEntityBuildable.setTownHall(tileEntityTownHall);
            tileEntityTownHall.addHut(tileEntityBuildable.xCoord, tileEntityBuildable.yCoord, tileEntityBuildable.zCoord);
            if(world.getTileEntity(x, y, z) instanceof TileEntityHutWorker)
            {
                ((TileEntityHutWorker) world.getTileEntity(x, y, z)).attemptToAddIdleCitizen(tileEntityTownHall);
            }
        }
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
        if(world.isRemote) return false;

        if(this.canPlayerDestroy(world, x, y, z, player))
        {
            if(world.getTileEntity(x, y, z) instanceof TileEntityTownHall)
            {
                TileEntityTownHall tileEntityTownHall = (TileEntityTownHall) world.getTileEntity(x, y, z);
                for(Object o : world.loadedEntityList)
                {
                    if(o instanceof EntityCitizen)
                    {
                        EntityCitizen citizen = (EntityCitizen) o;
                        if(tileEntityTownHall.getCitizens().contains(citizen.getUniqueID()))
                        {
                            citizen.setDead();
                            tileEntityTownHall.removeCitizen(citizen);//TODO move to citizen onDeath
                        }
                    }
                }
                PlayerProperties.get(player).removeTownhall();
            }
            return super.removedByPlayer(world, player, x, y, z);
        }
        return false;
    }

    public boolean canPlayerDestroy(World world, int x, int y, int z, Entity entity)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        EntityPlayer entityPlayer = (EntityPlayer) entity;
        if(tileEntity instanceof TileEntityBuildable)
        {
            return ((TileEntityBuildable) tileEntity).isPlayerOwner(entityPlayer);
        }
        return false;
    }

    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        if(world.isRemote) return;

        if(!(world.getTileEntity(x, y, z) instanceof TileEntityTownHall))
        {
            TileEntityBuildable tileEntityBuildable = (TileEntityBuildable) world.getTileEntity(x, y, z);
            TileEntityTownHall tileEntityTownHall = tileEntityBuildable.getTownHall();
            tileEntityTownHall.removeHut(tileEntityBuildable.xCoord, tileEntityBuildable.yCoord, tileEntityBuildable.zCoord);
            if(world.getTileEntity(x, y, z) instanceof TileEntityHutWorker)
            {
                ((TileEntityHutWorker) world.getTileEntity(x, y, z)).removeWorker(tileEntityTownHall);
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
