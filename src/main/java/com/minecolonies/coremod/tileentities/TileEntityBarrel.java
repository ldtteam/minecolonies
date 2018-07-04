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
    private boolean                                 done           = false;
    private int                                     items          = 0;
    private int                                     timer          = 0;
    public static final int                         MAX_ITEMS      = 64;
    private static final int                        TIMER_END      = 24000; //24000; //Number of Minecraft ticks in 2 whole days

    @Override
    public void update()
    {
        World world = this.getWorld();

        //todo we should do this here in intervals look how we check things in random intervals in the colony.
        // this way we avoid calling this every tick which causes lag. Just calling this probablistically every 20 ticks and
        // make it run until 24000/20 will have the same result but consume way less load on the server.
        this.updateTick(world, this.getPos(), world.getBlockState(this.getPos()), new Random());
    }


    public void updateTick(final World worldIn, final BlockPos pos, final IBlockState state, final Random rand)
    {
        //Check if the barrel is actually full
        if(getItems() == TileEntityBarrel.MAX_ITEMS)
        {
            doBarrelCompostTick(worldIn, pos, state);
        }
        if(this.done && !world.isRemote)
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
            timer = 0;
            items = 0;
            done = true;
            updateBlock(worldIn, blockState);
        }
    }

    //whenever player right click to barrel call this.
    public boolean useBarrel(final World worldIn, final EntityPlayer playerIn, final ItemStack itemstack, final IBlockState state, final BlockPos pos)
    {
        //If the barrel has finished composting, we drop and change state
        if (done)
        {
            // TODO: Add this back in once compost exists again. For now it drops 6 boneMeals
            // playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost, 6));
            done = false;
            playerIn.inventory.addItemStackToInventory(new ItemStack(Items.DYE, 6, 15));
            updateBlock(worldIn, state);
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
            this.updateBlock(worldIn, state);
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

    }

    private boolean checkCorrectItem(ItemStack itemStack)
    {
        // todo we should probably make this configureable in the configuration, but for testing this is okay.
        //Any new item we want it to accept should be added here
        return itemStack.getItem().equals(Items.ROTTEN_FLESH)
                || itemStack.getItem().equals(Items.WHEAT_SEEDS)
                || itemStack.getItem().equals(Items.MELON_SEEDS)
                || itemStack.getItem().equals(Items.BEETROOT_SEEDS)
                || itemStack.getItem().equals(Items.PUMPKIN_SEEDS)
                || itemStack.getItem().equals(Item.getItemFromBlock(Blocks.SAPLING));
    }

    public void updateBlock(World worldIn, IBlockState state)
    {
         this.markDirty();
         world.notifyBlockUpdate(pos, state, state, 0x03);
         world.markBlockRangeForRenderUpdate(pos,pos);

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setInteger("items", this.items);
        compound.setInteger("timer", this.timer);
        compound.setBoolean("done", this.done);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
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
    }

    @Override
    public final void handleUpdateTag(NBTTagCompound tag)
    {
        this.items = tag.getInteger("items");
        this.timer = tag.getInteger("timer");
        this.done = tag.getBoolean("done");
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 11);
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
    public boolean addItem(ItemStack item)
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
