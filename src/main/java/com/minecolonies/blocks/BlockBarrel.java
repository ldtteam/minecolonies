package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashMap;
import java.util.Random;

public class BlockBarrel extends Block
{
    private static final int MIN_FULLNESS           = 0;
    private static final int MAX_FULLNESS           = 16;
    private static final int BARRELSTATE_FILLING    = 0;
    private static final int BARRELSTATE_COMPOSTING = 1;
    private static final int BARRELSTATE_DONE       = 2;

    // todo: use a TileEntity to store state

    private static final PropertyInteger            BARRELSTATE    = PropertyInteger.create("BARRELSTATE", BARRELSTATE_FILLING, BARRELSTATE_DONE);
    /**
     * The hardness this block has.
     */
    private static final float                      BLOCK_HARDNESS = 5F;
    /**
     * This blocks name.
     */
    private static final String                     BLOCK_NAME     = "blockBarrel";
    /**
     * The resistance this block has.
     */
    private static final float                      RESISTANCE     = 1F;
    private static final HashMap<BlockPos, Integer> timers         = new HashMap<>();
    private static final HashMap<BlockPos, Integer> fillings       = new HashMap<>();

    public BlockBarrel()
    {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BARRELSTATE, BARRELSTATE_FILLING));

        this.setTickRandomly(true);
        initBlock();
    }

    /**
     * initialize the block
     */
    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        GameRegistry.registerBlock(this, BLOCK_NAME);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    public void AddItemToBarrel(World worldIn, EntityPlayer playerIn, ItemStack itemStack, IBlockState state, BlockPos pos)
    {
        UseBarrel(worldIn, playerIn, itemStack, state, pos);
    }

    //whenever player right click to barrel call this.
    public boolean UseBarrel(World worldIn, EntityPlayer playerIn, ItemStack itemstack, IBlockState state, BlockPos pos)
    {
        MineColonies.getLogger().info("block activated");

        int barrelState = state.getValue(BARRELSTATE);
        int fullness = fillings.getOrDefault(pos, 0);

        MineColonies.getLogger().info("At this moment bs= " + barrelState + " and fl=" + fullness);


        //if statement 1
        if (state.getValue(BARRELSTATE) == BARRELSTATE_DONE)
        {
            // todo: add this back in once compost exists again
            // playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost, 8));
            worldIn.setBlockState(pos, state.withProperty(BARRELSTATE, BARRELSTATE_FILLING));
            fillings.put(pos, MIN_FULLNESS);
            MineColonies.getLogger().info("Set Blockstate to " + worldIn.getBlockState(pos));
            return true;
        }

        if (itemstack == null)
        {
            return true;
        }

        Item item = itemstack.getItem();

        if (item == Items.ROTTEN_FLESH && barrelState == BARRELSTATE_FILLING)
        {
            MineColonies.getLogger().info("item Consumed");

            itemstack.stackSize--;

            fullness += 1;
            if (fullness >= MAX_FULLNESS)
            {
                fullness = MAX_FULLNESS;
                worldIn.setBlockState(pos, state.withProperty(BARRELSTATE, BARRELSTATE_COMPOSTING));
            }
            fillings.put(pos, fullness);
            MineColonies.getLogger().info("now FULLNESS = " + fullness);

            return true;
        }

        return true;
    }

    public void GetItemFromBarrel(World worldIn, EntityPlayer playerIn, ItemStack itemStack, IBlockState state, BlockPos pos)
    {
        final int bs = state.getValue(BARRELSTATE);
        if (bs == 2)
        {
            UseBarrel(worldIn, playerIn, null, state, pos);
        }
    }

    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(BARRELSTATE, meta);
    }

    @Override
    public int getMetaFromState(final IBlockState state)
    {
        return state.getValue(BARRELSTATE);
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render.
     *
     * @return true
     */
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        MineColonies.getLogger().info("UpdateTick called");

        int barrelState = state.getValue(BARRELSTATE);

        MineColonies.getLogger().info("now BARRELSTATE = " + barrelState);
        switch (state.getValue(BARRELSTATE))
        {
            case BARRELSTATE_FILLING:
                checkIfBarrelFull(worldIn, pos, state);
                break;
            case BARRELSTATE_COMPOSTING:
                doBarrelCompostTick(worldIn, pos, state);
                break;
            case BARRELSTATE_DONE:
                break;
        }
    }

    private void checkIfBarrelFull(World world, BlockPos pos, IBlockState state)
    {
        int fullness = fillings.getOrDefault(pos, 0);
        if (fullness >= MAX_FULLNESS)
        {
            MineColonies.getLogger().info("Barrel is full.");
            world.setBlockState(pos, state.withProperty(BARRELSTATE, BARRELSTATE_COMPOSTING));
        }
    }

    private void doBarrelCompostTick(World world, BlockPos pos, IBlockState state)
    {
        int timer = timers.getOrDefault(pos, 0);
        timer++;
        if (timer >= 20)
        {
            world.setBlockState(pos, state.withProperty(BARRELSTATE, BARRELSTATE_DONE));
            timer = 0;
        }
        timers.put(pos, timer);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, BARRELSTATE);
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        MineColonies.getLogger().info("block right-clicked");

        ItemStack itemstack = playerIn.inventory.getCurrentItem();
        UseBarrel(worldIn, playerIn, itemstack, state, pos);
        return true;
    }
}
