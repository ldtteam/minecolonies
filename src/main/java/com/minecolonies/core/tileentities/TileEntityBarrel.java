package com.minecolonies.core.tileentities;

import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.CompostRecipe;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.AbstractTileEntityBarrel;
import com.minecolonies.api.tileentities.ITickable;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class TileEntityBarrel extends AbstractTileEntityBarrel implements ITickable
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

    public TileEntityBarrel(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.BARREL.get(), pos, state);
    }

    /**
     * Update method to be called by Minecraft every tick
     */
    @Override
    public void tick()
    {
        final Level world = this.getLevel();

        if (!world.isClientSide && (world.getGameTime() % (world.random.nextInt(AVERAGE_TICKS * 2) + 1) == 0))
        {
            this.updateTick(world, this.getBlockPos(), world.getBlockState(this.getBlockPos()), new Random());
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
    public void updateTick(final Level worldIn, final BlockPos pos, final BlockState state, final Random rand)
    {
        if (getItems() == AbstractTileEntityBarrel.MAX_ITEMS)
        {
            doBarrelCompostTick(worldIn, pos, state);
        }
        if (this.done)
        {
            ((ServerLevel) worldIn).sendParticles(
              ParticleTypes.HAPPY_VILLAGER, this.getBlockPos().getX() + 0.5,
              this.getBlockPos().getY() + 1.5, this.getBlockPos().getZ() + 0.5,
              1, 0.2, 0, 0.2, 0);
        }
    }

    private void doBarrelCompostTick(final Level worldIn, final BlockPos pos, final BlockState blockState)
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
     * Method called when a player uses the block. Takes the needed items from the player.
     *
     * @param playerIn  the player
     * @param itemstack the itemStack on the hand of the player
     * @param hitFace   the side of the barrel the player hit.
     *                  Passing null when composting is complete will insert resulting compost directly into inventory, spawning overflow as an ItemEntity
     * @return if the barrel took any item
     */
    public boolean useBarrel(final Player playerIn, final ItemStack itemstack, @Nullable Direction hitFace)
    {
        if (done)
        {
            ItemStack compostStack = new ItemStack(ModItems.compost, 6);
            if (hitFace != null) // Spawn all as ItemEntity
            {
                playerIn.level().addFreshEntity(new ItemEntity(playerIn.level(), worldPosition.getX() + 0.5, worldPosition.getY() + 1.75, worldPosition.getZ() + 0.5, compostStack, hitFace.getStepX() / 5f, hitFace.getStepY() / 5f + 0.2f, hitFace.getStepZ() / 5f));
                this.level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1, 1);
            }
            else // Insert directly into inventory, spawning overflow as ItemEntity
            {
                if(!playerIn.getInventory().add(compostStack))
                {
                    playerIn.level().addFreshEntity(new ItemEntity(playerIn.level(), worldPosition.getX() + 0.5, worldPosition.getY() + 1.75, worldPosition.getZ() + 0.5, compostStack, 0, 0.2f, 0));
                }
                this.level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1, 1);
            }
            done = false;
            return true;
        }

        final RecipeHolder<CompostRecipe> recipe = findCompostRecipe(itemstack);
        if (recipe == null)
        {
            return false;
        }

        if (items == AbstractTileEntityBarrel.MAX_ITEMS)
        {
            MessageUtils.format("entity.barrel.working").sendTo(playerIn);
            return false;
        }
        else
        {
            this.consumeNeededItems(itemstack, recipe);
            return true;
        }
    }

    private void consumeNeededItems(final ItemStack itemStack, final RecipeHolder<CompostRecipe> recipe)
    {
        // the strength defined by the recipe determines how many "compostable items" each
        // item actually counts for.  (most items contribute 4 strength.)
        final int factor = recipe.value().getStrength();

        //The available items the player has in his hand
        final int availableItems = itemStack.getCount() * factor;
        //The items we need to complete the barrel
        final int neededItems = AbstractTileEntityBarrel.MAX_ITEMS - items;
        //The quantity of items that we are going to take from the player
        int itemsToRemove = Math.min(neededItems, availableItems);

        //We update the quantities in the player´s inventory and in the barrel
        this.items += itemsToRemove;
        itemsToRemove /= factor;
        ItemStackUtils.changeSize(itemStack, -itemsToRemove);
    }

    @Nullable
    private static RecipeHolder<CompostRecipe> findCompostRecipe(final ItemStack itemStack)
    {
        return IColonyManager.getInstance().getCompatibilityManager()
                .getCopyOfCompostRecipes().get(itemStack.getItem());
        // TODO: use the recipe to get the ferment time and output count?
        // tricky because they might use multiple items with different values
    }

    /**
     * Updates the block between the server and the client
     *
     * @param worldIn the world
     */
    public void updateBlock(final Level worldIn)
    {
        final BlockState barrel = level.getBlockState(worldPosition);
        if (barrel.getBlock() == ModBlocks.blockBarrel.get())
        {
            worldIn.setBlockAndUpdate(worldPosition, AbstractBlockBarrel.changeStateOverFullness(this, barrel));
            setChanged();
        }
    }

    @Override
    public void saveAdditional(final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        super.saveAdditional(compound, provider);

        compound.putInt("items", this.items);
        compound.putInt("timer", this.timer);
        compound.putBoolean("done", this.done);
    }

    @Override
    public void loadAdditional(final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        super.loadAdditional(compound, provider);
        this.items = compound.getInt("items");
        this.timer = compound.getInt("timer");
        this.done = compound.getBoolean("done");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider provider)
    {
        return saveWithId(provider);
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet, @NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compound = packet.getTag();
        this.loadAdditional(compound, provider);
        setChanged();
    }

    @Override
    public void setChanged()
    {
        if (level != null)
        {
            WorldUtil.markChunkDirty(level, worldPosition);
        }
    }

    @Override
    public final void handleUpdateTag(final CompoundTag tag, @NotNull final HolderLookup.Provider provider)
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
        final RecipeHolder<CompostRecipe> recipe = findCompostRecipe(item);
        if (recipe != null && this.items < MAX_ITEMS)
        {
            this.consumeNeededItems(item, recipe);
            this.updateBlock(this.level);
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
            this.updateBlock(this.level);
            return new ItemStack(ModItems.compost, (int) (6 * multiplier));
        }
        return ItemStack.EMPTY;
    }
}
