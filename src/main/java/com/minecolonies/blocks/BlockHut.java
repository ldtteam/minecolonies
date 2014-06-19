package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.lib.Constants;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.lib.IColony;
import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.tileentities.TileEntityHutWorker;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if(world.isRemote) return;

        if(entityLivingBase instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entityLivingBase;
            TileEntityHut hut = (TileEntityHut) world.getTileEntity(x, y, z);
            if(hut instanceof TileEntityTownHall)
            {
                TileEntityTownHall townhall = (TileEntityTownHall) hut;
                townhall.onBlockAdded();
                townhall.addOwner(player.getUniqueID());
                townhall.setCityName(LanguageHandler.format("com.minecolonies.gui.townhall.defaultName", player.getDisplayName()));
                PlayerProperties.get(player).placeTownhall(x, y, z);
            }
            else
            {
                TileEntityTownHall townhall = Utils.getTownhallByOwner(world, player);

                hut.setTownHall(townhall);
                townhall.addHut(hut.xCoord, hut.yCoord, hut.zCoord);

                if(hut instanceof TileEntityHutWorker)
                {
                    ((TileEntityHutWorker) hut).addJoblessCitizens(townhall);
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float px, float py, float pz)
    {
        if(world.getTileEntity(x, y, z) instanceof TileEntityHut && !player.isSneaking())
        {
            TileEntityHut tileEntityHut = (TileEntityHut) world.getTileEntity(x, y, z);

            if(tileEntityHut.isPlayerOwner(player))
            {
                int guiID = EnumGUI.getGuiIdByInstance(tileEntityHut);
                player.openGui(MineColonies.instance, guiID, world, x, y, z);
            }
            else if(tileEntityHut.getTownHall() != null)
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoPermission", tileEntityHut.getTownHall().getCityName());
            }
            return true;
        }
        return false;
    }
}
