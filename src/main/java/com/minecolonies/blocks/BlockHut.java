package com.minecolonies.blocks;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
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

/**
 * Abstract class for all minecolonies blocks.
 * The method {@link com.minecolonies.blocks.BlockHut#getName()} is abstract
 */
public abstract class BlockHut extends Block implements ITileEntityProvider
{
    protected int workingRange;//TODO unused

    private IIcon[] icons = new IIcon[6];// 0 = top, 1 = bot, 2-5 = sides;

    /**
     * Constructor for a block using the minecolonies mod.
     * Registers the block, sets the creative tab, as well as the resistance and the hardness
     */
    public BlockHut()
    {
        super(Material.wood);
        setBlockName(getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        //Blast resistance for creepers etc. makes them explosion proof
        setResistance(6000000000f);
        //Hardness of 10 takes a long time to mine to not loose progress
        setHardness(10f);
        GameRegistry.registerBlock(this, getName());
    }

    /**
     * Method to return the name of the block
     *
     * @return  Name of the block
     */
    public abstract String getName();

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        /*
        Registers all icons for a block.
        Saves in icon array.
        Icons are called with [minecolonies:block[Top/sideChest]]
        Bottom is same as top
         */
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

    //Todo, does (colony.getTownhall() != null) really have to be a null pointer, or can we return, and inform the player
    //Todo, (colony == null) checks if the colony is made already. Seems like there is no check for > 1 colony
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if(world.isRemote) return;

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(entityLivingBase instanceof EntityPlayer &&
                tileEntity instanceof TileEntityColonyBuilding)
        {
            EntityPlayer player = (EntityPlayer) entityLivingBase;
            TileEntityColonyBuilding hut = (TileEntityColonyBuilding) tileEntity;

            Colony colony = ColonyManager.getColony(world, hut.getPosition());

            if(this instanceof BlockHutTownHall)
            {
                /*
                True if you try to place a BlockHutTownHall, and there is no colony at your location yet.
                Creates a new colony
                 */
                if (colony == null)
                {
                    colony = ColonyManager.createColony(world, hut.getPosition());
                    String colonyName = LanguageHandler.format("com.minecolonies.gui.townhall.defaultName", player.getDisplayName());
                    colony.setName(colonyName);
                    colony.getPermissions().setPlayerRank(player.getGameProfile().getId(), Permissions.Rank.OWNER);
                }
                /*
                Placing a townhall where a colony is already.
                 */
                else if (colony.getTownhall() != null)
                {
                    throw new NullPointerException("TownHall placed in colony with an existing townhall");
                }
            }

            if (colony == null) //there is no colony, and you attempted to place a non-townhall block.
            {
                throw new NullPointerException("No colony to place block");
            }

            colony.addNewBuilding(hut);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float px, float py, float pz)
    {
        if(world.isRemote)
        {
            Building.View building = ColonyManager.getBuildingView(world, x, y, z);

            if (building != null)
            {
                building.openGui();
            }
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityColonyBuilding();
    }
}
