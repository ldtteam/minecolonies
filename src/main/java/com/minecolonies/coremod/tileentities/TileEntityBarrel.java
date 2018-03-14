package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.BarrelType;
import com.minecolonies.coremod.blocks.BlockBarrel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.Random;

public class TileEntityBarrel extends TileEntity implements ITickable
{
    private int                                     items          = 0;
    private int                                     timer          = 0;
    private static final int                        MAX_ITEMS      = 64;
    private static final int                        TIMER_END      = 600;

    @Override
    public void update()
    {
        World world = this.getWorld();
        if(!world.isRemote)
            this.updateTick(world, this.getPos(), world.getBlockState(this.getPos()), new Random());
    }


    public void updateTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand)
    {
        //Log.getLogger().info("UpdateTick called");

        //We get the actual value of the blockstate for the barrel
        BarrelType barrelType = state.getActualState(worldIn, pos).getValue(BlockBarrel.VARIANT);
        //Log.getLogger().info("The barrel is "+barrelType.getName());

        //Check if the barrel is actually full
        if(barrelType.equals(BarrelType.WORKING))
        {
            doBarrelCompostTick(worldIn, pos, state);
        }
    }

    private void doBarrelCompostTick(final World worldIn, final BlockPos pos, final IBlockState blockState)
    {
        timer++;
        if (timer >= TIMER_END)
        {
            //TODO: Add particles to the barrel and change models in the blockstate JSON
            this.updateBlock(blockState.withProperty(BlockBarrel.VARIANT, BarrelType.DONE));
            timer = 0;
            items = 0;
        }
    }

    //whenever player right click to barrel call this.
    public boolean useBarrel(final World worldIn, final EntityPlayer playerIn, final ItemStack itemstack, final IBlockState state, final BlockPos pos)
    {
        if(getWorld().isRemote) return false;
        Log.getLogger().info("block activated (currently under development!)");
        IBlockState blockState =  state.getActualState(worldIn, pos);

        //If the barrel has finished composting, we drop and change state
        if(blockState.getValue(BlockBarrel.VARIANT).equals(BarrelType.DONE))
        {
            // TODO: Add this back in once compost exists again. For now it drops a bone
            // playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost, 6));
            playerIn.inventory.addItemStackToInventory(new ItemStack(Items.DYE, 6, 15));
            this.updateBlock(blockState.withProperty(BlockBarrel.VARIANT, BarrelType.ZERO));
            return true;
        }

        //We check if the player is holding one of this items. If none is found, the method ends.
        if(!this.checkCorrectItem(itemstack))
        {
            return false;
        }

        if(blockState.getValue(BlockBarrel.VARIANT).equals(BarrelType.WORKING))
        {
            //TODO: fix double chat messages here
            playerIn.sendMessage(new TextComponentString("The barrel is working!"));
            Log.getLogger().info("The barrel is working");
            return false;
        }
        else
        {
            this.consumeNeededItems(itemstack);
            this.changeStateOverFullness(worldIn, state);
            return true;
        }
    }

    private void changeStateOverFullness(World worldIn, IBlockState blockState)
    {
        //The posible states of the barrel (minus the state done and working)
        int posibleStates = BarrelType.values().length-3;   //From 0 to last state

        //12.8 -> the number of items needed to go up on a state (having 6 filling states)
        //So items/12.8 -> meta of the state we should get
        BarrelType state = BarrelType.byMetadata((int) Math.round(items/12.8));

        Log.getLogger().info("Barrel State: " +(state.getName()));

        //If the barrel is in a "filling" state, we update it
        if(state.getMetadata() <= posibleStates)
        {
            this.updateBlock(
                       blockState.withProperty(
                            BlockBarrel.VARIANT, state));
        }

        //If the barrel is full, it starts to compost
        if(items == MAX_ITEMS)
            this.updateBlock(
                    blockState.withProperty(
                            BlockBarrel.VARIANT, BarrelType.WORKING));

    }

    private void consumeNeededItems(ItemStack itemStack)
    {
        Log.getLogger().info("Consuming: "+itemStack.getItem().getUnlocalizedName());

        int factor = 1;
        //Rotten Flesh counts as 2 items
        factor = itemStack.getItem().equals(Items.ROTTEN_FLESH)?2 : 1;

        //The available items the player has in his hand (Rotten Flesh counts as the double)
        int availableItems = itemStack.getCount()*factor;
        //The items we need to complete the barrel
        int neededItems = MAX_ITEMS - items;
        //The quantity of items that we are going to take from the player
        int itemsToRemove = Math.min(neededItems, availableItems);

        //We update the quantities in the player´s inventory and in the barrel
        this.items = this.items + itemsToRemove;
        itemsToRemove = itemsToRemove/factor;
        ItemStackUtils.changeSize(itemStack, -itemsToRemove);
        this.updateBlock(getWorld().getBlockState(pos));

        Log.getLogger().info("Consumed "+ itemsToRemove+" and now the barrel contains: "+items);

    }

    private boolean checkCorrectItem(ItemStack itemStack)
    {
        //Any new item we want it to accept should be added here
        return itemStack.getItem().equals(Items.ROTTEN_FLESH)
                || itemStack.getItem().equals(Items.WHEAT_SEEDS)
                || itemStack.getItem().equals(Items.MELON_SEEDS)
                || itemStack.getItem().equals(Items.BEETROOT_SEEDS)
                || itemStack.getItem().equals(Items.PUMPKIN_SEEDS)
                || itemStack.getItem().equals(Item.getItemFromBlock(Blocks.SAPLING));
    }

    private void updateBlock(IBlockState newState) {
        if (!getWorld().isRemote) {
            getWorld().setBlockState(pos, newState);
            this.markDirty();
            getWorld().notifyBlockUpdate(this.getPos(), newState, newState, 3);
        }
    }

}
