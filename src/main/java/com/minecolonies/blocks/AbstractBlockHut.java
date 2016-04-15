package com.minecolonies.blocks;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.TileEntityColonyBuilding;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Abstract class for all minecolonies blocks.
 * The method {@link AbstractBlockHut#getName()} is abstract
 * All AbstractBlockHut[something] should extend this class
 */
public abstract class AbstractBlockHut extends Block implements ITileEntityProvider
{
    protected               int     workingRange;
    private   static final  float   RESISTANCE      = 10F;

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
        this.setBlockName(Constants.MOD_ID.toLowerCase() + "." + getName());
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        //Blast resistance for creepers etc. makes them explosion proof
        setResistance((float)Integer.MAX_VALUE);
        //Hardness of 10 takes a long time to mine to not loose progress
        setHardness(RESISTANCE);
        GameRegistry.registerBlock(this,this.getName());
    }

    /**
     * Method to return the name of the block
     *
     * @return          Name of the block.
     */
    public abstract String getName();

    @Override
    public void onBlockPlacedBy(final World worldIn,
                                final int x,
                                final int y,
                                final int z,
                                final EntityLivingBase placer,
                                final ItemStack stack)
    {
        /*
        Only work on server side
        */
        if(worldIn.isRemote)
        {
            return;
        }

        TileEntity tileEntity = worldIn.getTileEntity(x,y,z);
        if(placer instanceof EntityPlayer && tileEntity instanceof TileEntityColonyBuilding)
        {
            TileEntityColonyBuilding hut = (TileEntityColonyBuilding) tileEntity;
            Colony colony = ColonyManager.getColony(worldIn, hut.getPosition());

            if (colony != null)
            {
                colony.addNewBuilding(hut);
            }
        }    }

    @Override
    public boolean onBlockActivated(final World worldIn,
                                    final int x,
                                    final int y,
                                    final int z,
                                    final EntityPlayer player,
                                    final int side,
                                    final float hitX,
                                    final float hitY,
                                    final float hitZ)
    {
        /*
        If the world is client, open the gui of the building
         */
        if(worldIn.isRemote)
        {
            Building.View building = ColonyManager.getBuildingView(worldIn, x,y,z);

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
