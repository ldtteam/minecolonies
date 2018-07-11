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
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TileEntityBarrel extends TileEntity implements ITickable
{
    /**
     * True if the barrel has finished composting and the items are ready to harvest
     */
    private boolean                                 done           = false;
    /**
     * The number of items that the barrel contains
     */
    private int                                     items          = 0;
    /**
     * The timer for the composting process
     */
    private int                                     timer          = 0;
    /**
     * The number of items it needs to start composting
     */
    public static final int                         MAX_ITEMS      = 64;
    /**
     * The number the timer has to reach to finish composting. Number of Minecraft ticks in 2 whole days
     */
    private static final int                        TIMER_END      = 24000;
    /**
     * The average of ticks that passes between actually ticking the tileEntity
     */
    private static final int                        AVERAGE_TICKS  = 20;

    /**
     * Update method to be called by Minecraft every tick
     */
    @Override
    public void update( )
    {
        final World world = this.getWorld();

        if (!world.isRemote && (world.getWorldTime() % (world.rand.nextInt(AVERAGE_TICKS * 2) + 1) == 0))
        {
            this.updateTick(world, this.getPos(), world.getBlockState(this.getPos()), new Random());
        }

    }

    /**
     * Method that does compost ticks if needed or spawns particles if finished
     * @param worldIn the world
     * @param pos the position
     * @param state the state of the block
     * @param rand Random class
     */
    public void updateTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand)
    {
        if(getItems() == TileEntityBarrel.MAX_ITEMS)
        {
            doBarrelCompostTick(worldIn, pos, state);
        }
        if(this.done)
        {
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

    /**
     * Method called when a player uses the block. Takes the needed itmes from the player if needed.
     * @param worldIn the world
     * @param playerIn the player
     * @param itemstack the itemStack on the hand of the player
     * @param state the state of the block
     * @param pos the position
     * @return if the barrel took any item
     */
    public boolean useBarrel(final World worldIn, final EntityPlayer playerIn, final ItemStack itemstack, final IBlockState state, final BlockPos pos)
    {
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
            if(OreDictionary.getOreIDs(itemStack).length > 0 &&
                 OreDictionary.getOreName(OreDictionary.getOreIDs(itemStack)[0]).equals(string))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the block between the server and the client
     * @param worldIn the world
     * @param state the state of the block
     */
    public void updateBlock(final World worldIn, final IBlockState state)
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

    /**
     * Returns the number of items that the block contains
     * @return the number of items
     */
    public int getItems()
    {
        return items;
    }

    /**
     * Returns if the barrel has finished composting
     * @return true if done, false if not
     */
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

}
