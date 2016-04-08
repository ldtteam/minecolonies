package com.minecolonies.blocks;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import com.minecolonies.lib.Literals;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
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
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Abstract class for all minecolonies blocks.
 * The method {@link AbstractBlockHut#getName()} is abstract
 * All AbstractBlockHut[something] should extend this class
 */
public abstract class AbstractBlockHut extends Block implements ITileEntityProvider
{
    protected           int     workingRange;
    private     final   float   RESISTANCE      = 10F;
    /* 0 is top, 1 is bot, 2-5 are sides */
    private             IIcon[] icons           = new IIcon[Literals.SIDES_TEXTURES];

    /**
     * Constructor for a block using the minecolonies mod.
     * Registers the block, sets the creative tab, as well as the resistance and the hardness.
     */
    public AbstractBlockHut()
    {
        super(Material.wood);
        initBlock();
    }

    private void initBlock()
    {
        setBlockName(getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        //Blast resistance for creepers etc. makes them explosion proof
        setResistance((float)Integer.MAX_VALUE);
        //Hardness of 10 takes a long time to mine to not loose progress
        setHardness(RESISTANCE);
        GameRegistry.registerBlock(this, getName());
    }

    /**
     * Method to return the name of the block
     *
     * @return          Name of the block.
     */
    public abstract String getName();

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        /*
        Registers all icons for a block.
        Saves in icon array.
        Icons are called with [minecolonies:block[Top/sideChest]].
        Bottom is same as top.
         */
        icons[ForgeDirection.UP.ordinal()] = iconRegister.registerIcon(Constants.MOD_ID + ":" + getName() + "Top");
        icons[ForgeDirection.DOWN.ordinal()] = icons[ForgeDirection.UP.ordinal()];
        for(int i = Literals.FIRST_INDEX_SIDES; i <= Literals.LAST_INDEX_SIDES; i++)
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
        /*
        Only work on server side
        */
        if(world.isRemote)
        {
            return;
        }

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if(entityLivingBase instanceof EntityPlayer && tileEntity instanceof TileEntityColonyBuilding)
        {
            TileEntityColonyBuilding hut = (TileEntityColonyBuilding) tileEntity;
            Colony colony = ColonyManager.getColony(world, hut.getPosition());

            if (colony != null)
            {
                colony.addNewBuilding(hut);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float px, float py, float pz)
    {
        /*
        If the world is client, open the gui of the building
         */
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
    public TileEntity createNewTileEntity(World world, int meta)
    {
        //Creates a tile entity for our building
        return new TileEntityColonyBuilding();
    }
}
