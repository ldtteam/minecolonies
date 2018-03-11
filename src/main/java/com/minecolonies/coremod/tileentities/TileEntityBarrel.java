package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.BarrelType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Random;

public class TileEntityBarrel extends TileEntity implements ITickable
{

    private static final int MIN_FULLNESS           = 0;
    private static final int MAX_FULLNESS           = 16;
    private static final int BARRELSTATE_FILLING    = 0;
    private static final int BARRELSTATE_COMPOSTING = 1;
    private static final int BARRELSTATE_DONE       = 2;

    // todo: use a TileEntity to store state
    private static final PropertyEnum<BarrelType> VARIANT        = PropertyEnum.create("variant", BarrelType.class);

    private static final PropertyInteger BARRELSTATE    = PropertyInteger.create("BARRELSTATE", BARRELSTATE_FILLING, BARRELSTATE_DONE);

    private static final HashMap<BlockPos, Integer> timers         = new HashMap<>();
    private static final HashMap<BlockPos, Integer> fillings       = new HashMap<>();
    private static final int                        TIMER_END      = 20;

    @Override
    public void update()
    {

    }

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

    private static void checkIfBarrelFull(final World world, final BlockPos pos, final IBlockState state)
    {
        final int fullness = fillings.getOrDefault(pos, 0);
        if (fullness >= MAX_FULLNESS)
        {
            Log.getLogger().info("Barrel is full.");
            world.setBlockState(pos, state.withProperty(BARRELSTATE, BARRELSTATE_COMPOSTING));
        }
    }

    //whenever player right click to barrel call this.
    public boolean useBarrel(final World worldIn, final EntityPlayer playerIn, final ItemStack itemstack, final IBlockState state, final BlockPos pos)
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

        if (ItemStackUtils.isEmpty(itemstack))
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

        return false;
    }

    public void getItemFromBarrel(final World worldIn, final EntityPlayer playerIn, final ItemStack itemStack, final IBlockState state, final BlockPos pos)
    {
        final int bs = state.getValue(BARRELSTATE);
        if (bs == 2)
        {
            useBarrel(worldIn, playerIn, null, state, pos);
        }
    }

}
