package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TileEntityBarrel extends TileEntity implements ITickable
{
    private boolean                                 done           = false;
    private int                                     items          = 0;
    private int                                     timer          = 0;
    public static final int                         MAX_ITEMS      = 64;
    private static final int                        TIMER_END      = 24000; //24000; //Number of Minecraft ticks in 2
                                                                            // whole days
    private static final int                        AVERAGE_TICKS  = 20;    //The average of ticks that passes between
                                                                            //actually ticking the tileEntity

    @Override
    public void update()
    {
        final World world = this.getWorld();

        if (!world.isRemote && (world.getWorldTime() % (world.rand.nextInt(AVERAGE_TICKS * 2) + 1) == 0))
        {
            this.updateTick(world, this.getPos(), world.getBlockState(this.getPos()), new Random());
        }

    }


    public void updateTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand)
    {
        //Check if the barrel is actually full
        if(getItems() == TileEntityBarrel.MAX_ITEMS)
        {
            doBarrelCompostTick(worldIn, pos, state);
        }
        if(this.done)
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
        if (timer >= TIMER_END/AVERAGE_TICKS)
        {
            timer = 0;
            items = 0;
            done = true;
            this.updateBlock(worldIn, blockState);
        }
    }

    //whenever player right click to barrel call this.
    public boolean useBarrel(final World worldIn, final EntityPlayer playerIn, final ItemStack itemstack, final IBlockState state, final BlockPos pos)
    {
        //If the barrel has finished composting, we drop and change state
        if (done)
        {
            playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost, 6));
            done = false;
            return true;
        }

        if (!this.checkCorrectItem(itemstack))
        {
            return false;
        }

        if (items == MAX_ITEMS)
        {
            playerIn.sendMessage(new TextComponentString("The barrel is working!"));
            return false;
        }
        else
        {
            this.consumeNeededItems(worldIn, itemstack);
            return true;
        }

    }

    private void consumeNeededItems(final World worldIn, final ItemStack itemStack)
    {

        //Saplings and seeds counts as 1 item added, the rest counts as 2 items
        final int factor = itemStack.getItem().getRegistryName().toString().contains("sapling")
                || itemStack.getItem().getRegistryName().toString().contains("seed")?1 : 2;

        //The available items the player has in his hand (Rotten Flesh counts as the double)
        final int availableItems = itemStack.getCount()*factor;
        //The items we need to complete the barrel
        final int neededItems = MAX_ITEMS - items;
        //The quantity of items that we are going to take from the player
        int itemsToRemove = Math.min(neededItems, availableItems);

        //We update the quantities in the player´s inventory and in the barrel
        this.items = this.items + itemsToRemove;
        itemsToRemove = itemsToRemove/factor;
        ItemStackUtils.changeSize(itemStack, -itemsToRemove);

    }

    private boolean checkCorrectItem(final ItemStack itemStack)
    {
        for(final String string : Configurations.gameplay.listOfCompostableItems)
        {
            if(itemStack.getItem().getRegistryName().toString().equals(string))
            {
                return true;
            }
        }
        return false;
    }

    public void updateBlock(World worldIn, final IBlockState state)
    {
         world.notifyBlockUpdate(pos, state, state, 0x03);
         world.markBlockRangeForRenderUpdate(pos,pos);
         this.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setInteger("items", this.items);
        compound.setInteger("timer", this.timer);
        compound.setBoolean("done", this.done);

        return compound;
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.items = compound.getInteger("items");
        this.timer = compound.getInteger("timer");
        this.done = compound.getBoolean("done");
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
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity packet)
    {
        final NBTTagCompound compound = packet.getNbtCompound();
        this.readFromNBT(compound);
        world.markBlockRangeForRenderUpdate(pos,pos);
    }

    @Override
    public final void handleUpdateTag(final NBTTagCompound tag)
    {
        this.items = tag.getInteger("items");
        this.timer = tag.getInteger("timer");
        this.done = tag.getBoolean("done");
    }

    public int getItems()
    {
        return items;
    }

    public boolean isDone()
    {
        return done;
    }

    //INTERFACE WITH AI PAWNS

    /***
     * Lets the AI insert items into the barrel.
     * @param item the itemStack to be placed inside it.
     * @return false if it couldnt be completed, true if at least 1 item of the stack was inserted.
     */
    public boolean addItem(final ItemStack item)
    {
        //Todo implement method
        return false;
    }

    /***
     * Lets the AI retrieve the compost when the barrel has done processing it.
     * @return The generated compost. If the barrel is not ready yet to be harvested, it will return an empty itemStack.
     */
    public ItemStack retrieveCompost()
    {
        //Todo implement method
        return ItemStack.EMPTY;
    }

    /**
     * Lets the AI know if a barrel is ready to be harvested.
     * @return true if the barrel is ready, false if not.
     */
    public boolean isReady()
    {
        return done;
    }

}
