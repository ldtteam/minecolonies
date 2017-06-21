package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;

public class BlockBarrel extends Block
{
    private static final int MIN_FULLNESS = 0;
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
    private static final int                        TIMER_END      = 20;

    public BlockBarrel()
    {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BARRELSTATE, BARRELSTATE_FILLING));

        this.setTickRandomly(true);
        initBlock();
    }

    //todo: register block with new method
    /**
     * initialize the block
     */
    @SuppressWarnings("deprecation")
    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        GameRegistry.registerBlock(this, BLOCK_NAME);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    public void AddItemToBarrel(final World worldIn, final EntityPlayer playerIn, final ItemStack itemStack, final IBlockState state, final BlockPos pos)
    {
        UseBarrel(worldIn, playerIn, itemStack, state, pos);
    }

    //whenever player right click to barrel call this.
    public boolean UseBarrel(final World worldIn, final EntityPlayer playerIn, final ItemStack itemstack, final IBlockState state, final BlockPos pos)
    {
        Log.getLogger().info("block activated");

        final int barrelState = state.getValue(BARRELSTATE);
        int fullness = fillings.getOrDefault(pos, 0);

        Log.getLogger().info("At this moment bs= " + barrelState + " and fl=" + fullness);


        //if statement 1
        if (state.getValue(BARRELSTATE) == BARRELSTATE_DONE)
        {
            // todo: add this back in once compost exists again
            // playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost, 8));
            worldIn.setBlockState(pos, state.withProperty(BARRELSTATE, BARRELSTATE_FILLING));
            fillings.put(pos, MIN_FULLNESS);
            Log.getLogger().info("Set Blockstate to " + worldIn.getBlockState(pos));
            return true;
        }

        if (!ItemStackUtils.isEmpty(itemstack))
        {
            return true;
        }

        final Item item = itemstack.getItem();

        if (item == Items.ROTTEN_FLESH && barrelState == BARRELSTATE_FILLING)
        {
            Log.getLogger().info("item Consumed");

            ItemStackUtils.changeSize(itemstack, -1);

            fullness += 1;
            if (fullness >= MAX_FULLNESS)
            {
                fullness = MAX_FULLNESS;
                worldIn.setBlockState(pos, state.withProperty(BARRELSTATE, BARRELSTATE_COMPOSTING));
            }
            fillings.put(pos, fullness);
            Log.getLogger().info("now FULLNESS = " + fullness);

            return true;
        }

        return true;
    }

    public void GetItemFromBarrel(final World worldIn, final EntityPlayer playerIn, final ItemStack itemStack, final IBlockState state, final BlockPos pos)
    {
        final int bs = state.getValue(BARRELSTATE);
        if (bs == 2)
        {
            UseBarrel(worldIn, playerIn, null, state, pos);
        }
    }

    //todo: remove once we no longer need to support this
    @SuppressWarnings("deprecation")
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
    //todo: remove once we no longer need to support this
    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(final IBlockState state)
    {
        return true;
    }

    @Override
    public void updateTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand)
    {
        Log.getLogger().info("UpdateTick called");

        final int barrelState = state.getValue(BARRELSTATE);

        Log.getLogger().info("now BARRELSTATE = " + barrelState);
        switch (state.getValue(BARRELSTATE))
        {
            case BARRELSTATE_FILLING:
                checkIfBarrelFull(worldIn, pos, state);
                break;
            case BARRELSTATE_COMPOSTING:
                doBarrelCompostTick(worldIn, pos, state);
                break;
            case BARRELSTATE_DONE:
            default:
                break;
        }
    }

    private static void checkIfBarrelFull(final World world, final BlockPos pos, final IBlockState state)
    {
        final int fullness = fillings.getOrDefault(pos, 0);
        if (fullness >= MAX_FULLNESS)
        {
            Log.getLogger().info("Barrel is full.");
            world.setBlockState(pos, state.withProperty(BARRELSTATE, BARRELSTATE_COMPOSTING));
        }
    }

    private static void doBarrelCompostTick(final World world, final BlockPos pos, final IBlockState state)
    {
        int timer = timers.getOrDefault(pos, 0);
        timer++;
        if (timer >= TIMER_END)
        {
            world.setBlockState(pos, state.withProperty(BARRELSTATE, BARRELSTATE_DONE));
            timer = 0;
        }
        timers.put(pos, timer);
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, BARRELSTATE);
    }

    public boolean onBlockActivated(
                                     final World worldIn,
                                     final BlockPos pos,
                                     final IBlockState state,
                                     final EntityPlayer playerIn,
                                     final EnumFacing side,
                                     final float hitX,
                                     final float hitY,
                                     final float hitZ)
    {
        Log.getLogger().info("block right-clicked");

        final ItemStack itemstack = playerIn.inventory.getCurrentItem();
        UseBarrel(worldIn, playerIn, itemstack, state, pos);
        return true;
    }
}
