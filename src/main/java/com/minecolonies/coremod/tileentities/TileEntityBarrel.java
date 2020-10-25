package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.AbstractTileEntityBarrel;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TileEntityBarrel extends AbstractTileEntityBarrel
{
    /**
     * True if the barrel has finished composting and the items are ready to harvest
     */
    private              boolean done          = false;
    /**
     * The number of items that the barrel contains
     */
    private              int     items         = 0;
    /**
     * The timer for the composting process
     */
    private              int     timer         = 0;
    /**
     * The number the timer has to reach to finish composting. Number of Minecraft ticks in 2 whole days
     */
    private static final int     TIMER_END     = 24000;
    /**
     * The average of ticks that passes between actually ticking the tileEntity
     */
    private static final int     AVERAGE_TICKS = 20;

    public TileEntityBarrel()
    {
        super(MinecoloniesTileEntities.BARREL);
    }

    /**
     * Update method to be called by Minecraft every tick
     */
    @Override
    public void tick()
    {
        final World world = this.getWorld();

        if (!world.isRemote && (world.getGameTime() % (world.rand.nextInt(AVERAGE_TICKS * 2) + 1) == 0))
        {
            this.updateTick(world, this.getPos(), world.getBlockState(this.getPos()), new Random());
        }
    }

    /**
     * Method that does compost ticks if needed or spawns particles if finished
     *
     * @param worldIn the world
     * @param pos     the position
     * @param state   the state of the block
     * @param rand    Random class
     */
    public void updateTick(final World worldIn, final BlockPos pos, final BlockState state, final Random rand)
    {
        if (getItems() == AbstractTileEntityBarrel.MAX_ITEMS)
        {
            doBarrelCompostTick(worldIn, pos, state);
        }
        if (this.done)
        {
            ((ServerWorld) worldIn).spawnParticle(
              ParticleTypes.HAPPY_VILLAGER, this.getPos().getX() + 0.5,
              this.getPos().getY() + 1.5, this.getPos().getZ() + 0.5,
              1, 0.2, 0, 0.2, 0);
        }
    }

    private void doBarrelCompostTick(final World worldIn, final BlockPos pos, final BlockState blockState)
    {
        timer++;
        if (timer >= TIMER_END / AVERAGE_TICKS)
        {
            timer = 0;
            items = 0;
            done = true;
            this.updateBlock(worldIn);
        }
    }

    /**
     * Method called when a player uses the block. Takes the needed itmes from the player if needed.
     *
     * @param playerIn  the player
     * @param itemstack the itemStack on the hand of the player
     * @return if the barrel took any item
     */
    public boolean useBarrel(final PlayerEntity playerIn, final ItemStack itemstack)
    {
        if (done)
        {
            playerIn.inventory.addItemStackToInventory(new ItemStack(ModItems.compost, 6));
            done = false;
            return true;
        }

        if (!checkCorrectItem(itemstack))
        {
            return false;
        }

        if (items == AbstractTileEntityBarrel.MAX_ITEMS)
        {
            playerIn.sendMessage(new TranslationTextComponent("entity.barrel.working"));
            return false;
        }
        else
        {
            this.consumeNeededItems(itemstack);
            return true;
        }
    }

    private void consumeNeededItems(final ItemStack itemStack)
    {
        //Saplings and seeds counts as 1 item added, the rest counts as 2 items
        final int factor = itemStack.getItem().getRegistryName().toString().contains("sapling")
                             || itemStack.getItem().getRegistryName().toString().contains("seed") ? 2 : 4;

        //The available items the player has in his hand (Rotten Flesh counts as the double)
        final int availableItems = itemStack.getCount() * factor;
        //The items we need to complete the barrel
        final int neededItems = AbstractTileEntityBarrel.MAX_ITEMS - items;
        //The quantity of items that we are going to take from the player
        int itemsToRemove = Math.min(neededItems, availableItems);

        //We update the quantities in the player´s inventory and in the barrel
        this.items = this.items + itemsToRemove;
        itemsToRemove = itemsToRemove / factor;
        ItemStackUtils.changeSize(itemStack, -itemsToRemove);
    }

    public static boolean checkCorrectItem(final ItemStack itemStack)
    {
        return IColonyManager.getInstance().getCompatibilityManager().isCompost(itemStack);
    }

    /**
     * Updates the block between the server and the client
     *
     * @param worldIn the world
     */
    public void updateBlock(final World worldIn)
    {
        final BlockState barrel = world.getBlockState(pos);
        if (barrel.getBlock() == ModBlocks.blockBarrel)
        {
            worldIn.setBlockState(pos, AbstractBlockBarrel.changeStateOverFullness(this, barrel));
            markDirty();
        }
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);

        compound.putInt("items", this.items);
        compound.putInt("timer", this.timer);
        compound.putBoolean("done", this.done);

        return compound;
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        super.read(compound);
        this.items = compound.getInt("items");
        this.timer = compound.getInt("timer");
        this.done = compound.getBoolean("done");
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        final CompoundNBT compound = new CompoundNBT();
        this.write(compound);
        return new SUpdateTileEntityPacket(this.pos, 0, compound);
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        final CompoundNBT compound = packet.getNbtCompound();
        this.read(compound);
        markDirty();
    }

    @Override
    public void markDirty()
    {
        WorldUtil.markChunkDirty(world, pos);
    }

    @Override
    public final void handleUpdateTag(final CompoundNBT tag)
    {
        this.items = tag.getInt("items");
        this.timer = tag.getInt("timer");
        this.done = tag.getBoolean("done");
    }

    /**
     * Returns the number of items that the block contains
     *
     * @return the number of items
     */
    @Override
    public int getItems()
    {
        return items;
    }

    /**
     * Returns if the barrel has finished composting
     *
     * @return true if done, false if not
     */
    @Override
    public boolean isDone()
    {
        return this.done;
    }

    //INTERFACE WITH AI PAWNS

    /***
     * Checks if the barrel is composting
     * @return true if the number of items is equal to the maximum. If not, false.
     */
    @Override
    public boolean checkIfWorking()
    {
        return this.items == MAX_ITEMS;
    }

    /***
     * Lets the AI insert items into the barrel.
     * @param item the itemStack to be placed inside it.
     * @return false if the item couldn't be cosumed. True if it could
     */
    @Override
    public boolean addItem(final ItemStack item)
    {
        if (checkCorrectItem(item) && this.items < MAX_ITEMS)
        {
            this.consumeNeededItems(item);
            this.updateBlock(this.world);
            return true;
        }
        return false;
    }

    /***
     * Lets the AI retrieve the compost when the barrel has done processing it.
     * @return The generated compost. If the barrel is not ready yet to be harvested, it will return an empty itemStack.
     */
    @Override
    public ItemStack retrieveCompost(final double multiplier)
    {
        if (this.done)
        {
            this.done = false;
            this.updateBlock(this.world);
            return new ItemStack(ModItems.compost, (int) (6 * multiplier));
        }
        return ItemStack.EMPTY;
    }
}
