package com.minecolonies.blocks;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.lib.Constants;
import com.minecolonies.lib.IColony;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.LanguageHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public abstract class BlockHut extends Block implements IColony, ITileEntityProvider
{
    protected int workingRange;//TODO unused

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
        icons[0] = iconRegister.registerIcon(Constants.MOD_ID + ":" + getName() + "Top");
        icons[1] = icons[0];
        for(int i = 2; i <= 5; i++)
        {
            icons[i] = iconRegister.registerIcon(Constants.MOD_ID + ":" + "sideChest");
        }
    }

    @Override
    public IIcon getIcon(int side, int meta)
    {
        return icons[side];
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if(world.isRemote) return;

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(entityLivingBase instanceof EntityPlayer && tileEntity instanceof TileEntityColonyBuilding)
        {
            EntityPlayer player = (EntityPlayer) entityLivingBase;
            TileEntityColonyBuilding hut = (TileEntityColonyBuilding) tileEntity;

            Colony colony = ColonyManager.getColonyByCoord(world, hut.getPosition());

            if(this instanceof BlockHutTownHall)
            {
                if (colony == null)
                {
                    String colonyName = LanguageHandler.format("com.minecolonies.gui.townhall.defaultName", player.getDisplayName());
                    colony = ColonyManager.createColony(world, hut.getPosition());
                    colony.setName(colonyName);
                    colony.getPermissions().setPlayerRank(player.getGameProfile().getId(), Permissions.Rank.OWNER);

                    //  TODO: Deprecate this code?
                    PlayerProperties.get(player).placeTownhall(x, y, z);
                }
                else if (colony.getTownhall() != null)
                {
                    throw new NullPointerException("TownHall placed in colony with an existing townhall");
                }
            }

            if (colony == null)
            {
                throw new NullPointerException("No colony to place block");
            }

            //hut.setColony(colony);
            colony.addNewBuilding(hut);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float px, float py, float pz)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if (tileEntity instanceof TileEntityColonyBuilding)
        {
            if(!world.isRemote)
            {
                Building building = ((TileEntityColonyBuilding)tileEntity).getBuilding();

                if (building != null)
                {
                    building.openGui(player);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityColonyBuilding();
    }
}
