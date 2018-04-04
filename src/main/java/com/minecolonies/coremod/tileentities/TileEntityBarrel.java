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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.logging.Logger;

public class TileEntityBarrel extends TileEntity implements ITickable
{
    private int                                     items          = 0;
    private int                                     timer          = 0;
    public static final int                         MAX_ITEMS      = 64;
    private static final int                        TIMER_END      = 24000; //Number of Minecraft ticks in 2 whole days

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
        if(barrelType.equals(BarrelType.DONE))
        {
            //If the barrel is done, we spawn particles imitating "bad smell"
            ((WorldServer)worldIn).spawnParticle(
                    EnumParticleTypes.VILLAGER_HAPPY, this.getPos().getX()+0.5,
                    this.getPos().getY()+1.5, this.getPos().getZ()+0.5,
                    1, 0.2, 0, 0.2, 0);
        }
    }

    private void doBarrelCompostTick(final World worldIn, final BlockPos pos, final IBlockState blockState)
    {
        timer++;
        if (timer >= TIMER_END)
        {
            IBlockState newState = blockState.withProperty(BlockBarrel.VARIANT,
                    BarrelType.DONE).withProperty(BlockBarrel.FACING, blockState.getValue(BlockBarrel.FACING));
            worldIn.setBlockState(this.pos, newState);
            this.updateBlock(worldIn, newState);
            timer = 0;
            items = 0;
        }
    }

    //whenever player right click to barrel call this.
    public boolean useBarrel(final World worldIn, final EntityPlayer playerIn, final ItemStack itemstack, final IBlockState state, final BlockPos pos)
    {
        if(getWorld().isRemote) return false;

        //If the barrel has finished composting, we drop and change state
        if(state.getValue(BlockBarrel.VARIANT).equals(BarrelType.DONE))
        {
            // TODO: Add this back in once compost exists again. For now it drops 6 boneMeals
            // playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost, 6));
            playerIn.inventory.addItemStackToInventory(new ItemStack(Items.DYE, 6, 15));
            IBlockState newState = state.withProperty(BlockBarrel.VARIANT,
                    BarrelType.ZERO).withProperty(BlockBarrel.FACING, state.getValue(BlockBarrel.FACING));
            worldIn.setBlockState(this.pos, newState);
            this.updateBlock(worldIn, newState);
            return true;
        }

        //We check if the player is holding one of this items. If none is found, the method ends.
        if(!this.checkCorrectItem(itemstack))
        {
            return false;
        }

        if(state.getValue(BlockBarrel.VARIANT).equals(BarrelType.WORKING))
        {
            playerIn.sendMessage(new TextComponentString("The barrel is working!"));
            return false;
        }
        else
        {
            this.consumeNeededItems(worldIn, itemstack);
            this.updateBlock(worldIn, worldIn.getBlockState(this.getPos()));
            return true;
        }
    }

    private void consumeNeededItems(World worldIn, ItemStack itemStack)
    {

        int factor;
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
        this.updateBlock(worldIn, worldIn.getBlockState(this.getPos()));

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

    public void updateBlock(World worldIn, IBlockState state) {
        if (!worldIn.isRemote)
        {
            this.markDirty();
            worldIn.notifyBlockUpdate(this.getPos(), state, state, 0x3 );
            Log.getLogger().info("notifyBlockUpdate called");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setInteger("items", this.items);
        compound.setInteger("timer", this.timer);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.items = compound.getInteger("items");
        this.timer = compound.getInteger("timer");
    }

    public int getItems()
    {
        return items;
    }


    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        return new SPacketUpdateTileEntity(this.pos, 0, compound);
    }

    @NotNull
    @Override
    public NBTTagCompound getUpdateTag()
    {
        Log.getLogger().info("getUpdateTag called");
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet)
    {
        Log.getLogger().info("onDataPacket called");
        final NBTTagCompound compound = packet.getNbtCompound();
        this.readFromNBT(compound);
    }


}
