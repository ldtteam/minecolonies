package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.inventory.container.ContainerField;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
import com.minecolonies.api.tileentities.ScareCrowType;
import com.minecolonies.api.tileentities.ScarecrowFieldStage;
import com.minecolonies.api.util.ItemStackUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Random;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

/**
 * The scarecrow tile entity to store extra data.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class ScarecrowTileEntity extends AbstractScarecrowTileEntity
{
    /**
     * The max width/length of a field.
     */
    private static final int MAX_RANGE = 5;

    /**
     * Has the field be taken by any worker?
     */
    private boolean taken = false;

    /**
     * Checks if the field doesNeedWork (Hoeig, Seedings, Farming etc).
     */
    private boolean doesNeedWork = true;

    /**
     * Has the field been planted?
     */
    private ScarecrowFieldStage fieldStage = ScarecrowFieldStage.EMPTY;

    /**
     * Citizen Id of the citizen owning the field.
     */
    private int ownerId;

    /**
     * Name of the citizen claiming the field.
     */
    @NotNull
    private String owner = "";

    /**
     * Random generator.
     */
    private final Random random = new Random();

    /**
     * The type of the scarecrow.
     */
    private ScareCrowType type;

    /**
     * The colony of the field.
     */
    @Nullable
    private IColony colony;

    /**
     * Inventory of the field.
     */
    private final ItemStackHandler inventory;

    /**
     * Creates an instance of the tileEntity.
     */
    public ScarecrowTileEntity()
    {
        super();
        this.inventory = new ItemStackHandler()
        {
            @Override
            public int getSlotLimit(int slot) { return 1; }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack)
            {
                return Tags.Items.SEEDS.contains(stack.getItem())
                         || (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof CropsBlock);
            }
        };
    }

    /**
     * Getter for MAX_RANGE.
     *
     * @return the max range.
     */
    public static int getMaxRange()
    {
        return MAX_RANGE;
    }

    /**
     * The size of the field in all four directions
     * in the same order as {@link Direction#getHorizontalIndex()}:
     * S, W, N, E
     */
    protected int[] radii = {MAX_RANGE, MAX_RANGE, MAX_RANGE, MAX_RANGE};

    /**
     * @param direction the direction for the radius
     * @param radius    the number of blocks from the scarecrow that the farmer will work with
     */
    public void setRadius(Direction direction, int radius)
    {
        this.radii[direction.getHorizontalIndex()] = radius;
        markDirty();
        world.notifyBlockUpdate(this.getPos(), this.getBlockState(), this.getBlockState(), 2);
    }

    /**
     * @param direction the direction to get the range for
     * @return the radius
     */
    public int getRadius(Direction direction)
    {
        return radii[direction.getHorizontalIndex()];
    }

    /**
     * Checks if a certain position is part of the field. Complies with the definition of field block.
     *
     * @param world    the world object.
     * @param position the position.
     * @return true if it is.
     */
    @Override
    public boolean isNoPartOfField(@NotNull final World world, @NotNull final BlockPos position)
    {
        return world.isAirBlock(position) || isValidDelimiter(world.getBlockState(position.up()).getBlock());
    }

    /**
     * Check if a block is a valid delimiter of the field.
     *
     * @param block the block to analyze.
     * @return true if so.
     */
    private static boolean isValidDelimiter(final Block block)
    {
        return block instanceof FenceBlock || block instanceof FenceGateBlock || block == ModBlocks.blockCactusFence || block == ModBlocks.blockCactusFenceGate
                 || block instanceof WallBlock;
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    @Override
    public BlockPos getID()
    {
        // Location doubles as ID
        return this.getPosition();
    }

    /**
     * Has the field been taken?
     *
     * @return true if the field is not free to use, false after releasing it.
     */
    @Override
    public boolean isTaken()
    {
        return this.taken;
    }

    /**
     * Sets the field taken.
     *
     * @param taken is field free or not
     */
    @Override
    public void setTaken(final boolean taken)
    {
        this.taken = taken;
        markDirty();
    }

    @Override
    public void nextState()
    {
        if (getFieldStage().ordinal() + 1 >= ScarecrowFieldStage.values().length)
        {
            doesNeedWork = false;
            setFieldStage(ScarecrowFieldStage.values()[0]);
            return;
        }
        setFieldStage(ScarecrowFieldStage.values()[getFieldStage().ordinal() + 1]);
    }

    /**
     * Checks if the field has been planted.
     *
     * @return true if there are crops planted.
     */
    @Override
    public ScarecrowFieldStage getFieldStage()
    {
        return this.fieldStage;
    }

    /**
     * Sets if there are any crops planted.
     *
     * @param fieldStage true after planting, false after harvesting.
     */
    @Override
    public void setFieldStage(final ScarecrowFieldStage fieldStage)
    {
        this.fieldStage = fieldStage;
        markDirty();
    }

    /**
     * Checks if the field needs work (planting, hoeing).
     *
     * @return true if so.
     */
    @Override
    public boolean needsWork()
    {
        return this.doesNeedWork;
    }

    /**
     * Sets that the field needs work.
     *
     * @param needsWork true if work needed, false after completing the job.
     */
    @Override
    public void setNeedsWork(final boolean needsWork)
    {
        this.doesNeedWork = needsWork;
        markDirty();
    }

    /**
     * Getter of the seed of the field.
     *
     * @return the ItemSeed
     */
    @Override
    @Nullable
    public ItemStack getSeed()
    {
        if (inventory.getStackInSlot(0) != ItemStackUtils.EMPTY)
        {
            return inventory.getStackInSlot(0);
        }
        return null;
    }

    @Override
    public BlockPos getPosition()
    {
        return getPos();
    }

    /**
     * Getter of the owner of the field.
     *
     * @return the string description of the citizen.
     */
    @Override
    @NotNull
    public String getOwner()
    {
        return owner;
    }

    /**
     * Getter for the ownerId of the field.
     *
     * @return the int id.
     */
    @Override
    public int getOwnerId()
    {
        return ownerId;
    }

    /**
     * Sets the owner of the field.
     *
     * @param ownerId the id of the citizen.
     */
    @Override
    public void setOwner(final int ownerId)
    {
        this.ownerId = ownerId;
        if (colony != null)
        {
            if (colony.getCitizenManager().getCivilian(ownerId) == null)
            {
                owner = "";
            }
            else
            {
                owner = colony.getCitizenManager().getCivilian(ownerId).getName();
            }
        }
        markDirty();
    }

    /**
     * Sets the owner of the field.
     *
     * @param ownerId    the name of the citizen.
     * @param tempColony the colony view.
     */
    @Override
    public void setOwner(final int ownerId, final IColonyView tempColony)
    {
        this.ownerId = ownerId;
        if (tempColony != null)
        {
            if (tempColony.getCitizen(ownerId) == null)
            {
                owner = "";
            }
            else
            {
                owner = tempColony.getCitizen(ownerId).getName();
            }
        }
        markDirty();
    }

    /**
     * Get the inventory of the scarecrow.
     *
     * @return the IItemHandler.
     */
    @Override
    public IItemHandler getInventory()
    {
        return inventory;
    }

    ///////////---- Following methods are used to update the tileEntity between client and server ----///////////

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        final CompoundNBT compound = new CompoundNBT();

        this.write(compound);
        if (colony != null)
        {
            compound.putInt(TAG_COLONY_ID, colony.getID());
        }
        return new SUpdateTileEntityPacket(this.getPosition(), 0, compound);
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
        if (compound.keySet().contains(TAG_COLONY_ID))
        {
            setOwner(ownerId, IColonyManager.getInstance().getColonyView(compound.getInt(TAG_COLONY_ID), world.getDimension().getType().getId()));
        }
    }

    /////////////--------------------------- End Synchronization-area ---------------------------- /////////////

    @Override
    public void read(final CompoundNBT compound)
    {
        final ListNBT inventoryTagList = compound.getList(TAG_INVENTORY, TAG_COMPOUND);
        for (int i = 0; i < inventoryTagList.size(); ++i)
        {
            final CompoundNBT inventoryCompound = inventoryTagList.getCompound(i);
            final ItemStack stack = ItemStack.read(inventoryCompound);
            if (ItemStackUtils.getSize(stack) <= 0)
            {
                inventory.setStackInSlot(i, ItemStackUtils.EMPTY);
            }
            else
            {
                inventory.setStackInSlot(i, stack);
            }
        }

        taken = compound.getBoolean(TAG_TAKEN);
        fieldStage = ScarecrowFieldStage.values()[compound.getInt(TAG_STAGE)];
        radii[3] = compound.getInt(TAG_LENGTH_PLUS);
        radii[2] = compound.getInt(TAG_WIDTH_PLUS);
        radii[1] = compound.getInt(TAG_LENGTH_MINUS);
        radii[0] = compound.getInt(TAG_WIDTH_MINUS);
        ownerId = compound.getInt(TAG_OWNER);
        setOwner(ownerId);

        super.read(compound);
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        @NotNull final ListNBT inventoryTagList = new ListNBT();
        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            @NotNull final CompoundNBT inventoryCompound = new CompoundNBT();
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == ItemStackUtils.EMPTY)
            {
                new ItemStack(Blocks.AIR, 0).write(inventoryCompound);
            }
            else
            {
                stack.write(inventoryCompound);
            }
            inventoryTagList.add(inventoryCompound);
        }
        compound.put(TAG_INVENTORY, inventoryTagList);

        compound.putBoolean(TAG_TAKEN, taken);
        compound.putInt(TAG_STAGE, fieldStage.ordinal());
        compound.putInt(TAG_LENGTH_PLUS, radii[3]);
        compound.putInt(TAG_WIDTH_PLUS, radii[2]);
        compound.putInt(TAG_LENGTH_MINUS, radii[1]);
        compound.putInt(TAG_WIDTH_MINUS, radii[0]);
        compound.putInt(TAG_OWNER, ownerId);
        if (colony != null)
        {
            compound.putInt(TAG_COLONY_ID, colony.getID());
        }

        return super.write(compound);
    }

    /**
     * Set the colony of the field.
     *
     * @param colony the colony to set.
     */
    public void setColony(final IColony colony)
    {
        this.colony = colony;
    }

    //----------------------- Type Specific parameters -----------------------//

    /**
     * Returns the type of the scarecrow (Important for the rendering).
     *
     * @return the enum type.
     */
    @Override
    public ScareCrowType getScarecrowType()
    {
        if (this.type == null)
        {
            this.type = ScareCrowType.values()[this.random.nextInt(2)];
        }
        return this.type;
    }

    @Nullable
    @Override
    public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
    {
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeBlockPos(this.getPos());
        return new ContainerField(id, inv, buffer);
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName()
    {
        return new StringTextComponent(owner);
    }
}
