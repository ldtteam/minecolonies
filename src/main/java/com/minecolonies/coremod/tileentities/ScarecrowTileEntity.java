package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.tileentities.AbstractScarescrowTileEntity;
import com.minecolonies.api.tileentities.ScareCrowType;
import com.minecolonies.api.tileentities.ScarecrowFieldStage;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

/**
 * The scarecrow tile entity to store extra data.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class ScarecrowTileEntity extends AbstractScarescrowTileEntity
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
     * The length to plus x of the field.
     */
    private int lengthPlusX;

    /**
     * The width to plus z of the seed.
     */
    private int widthPlusZ;

    /**
     * The length to minus xof the field.
     */
    private int lengthMinusX;

    /**
     * The width to minus z of the seed.
     */
    private int widthMinusZ;

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
     * Name of the scarecrow, string set in the GUI.
     */
    private String name;

    /**
     * Inventory of the field.
     */
    private final IItemHandlerModifiable inventory = new ItemStackHandler(1);

    /**
     * Creates an instance of the tileEntity.
     */
    public ScarecrowTileEntity()
    {
        super();
        name = LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user", LanguageHandler.format(owner));
    }

    /**
     * Getter of the name of the tileEntity.
     *
     * @return the string.
     */
    @Override
    public String getDesc()
    {
        return name;
    }

    /**
     * Setter for the name.
     *
     * @param name string to set.
     */
    @Override
    public void setName(final String name)
    {
        this.name = name;
        setCustomName(new StringTextComponent(name));
    }

    /**
     * Getter for MAX_RANGE.
     *
     * @return the max range.
     */
    private static int getMaxRange()
    {
        return MAX_RANGE;
    }

    /**
     * Calculates recursively the length of the field until a certain point.
     * <p>
     * This mutates the field!
     *
     * @param position the start position.
     * @param world    the world the field is in.
     */
    @Override
    public final void calculateSize(@NotNull final World world, @NotNull final BlockPos position)
    {
        //Calculate in all 4 directions
        this.lengthPlusX = searchNextBlock(0, position.east(), Direction.EAST, world);
        this.lengthMinusX = searchNextBlock(0, position.west(), Direction.WEST, world);
        this.widthPlusZ = searchNextBlock(0, position.south(), Direction.SOUTH, world);
        this.widthMinusZ = searchNextBlock(0, position.north(), Direction.NORTH, world);
    }

    /**
     * Calculates the field size into a specific direction.
     *
     * @param blocksChecked how many blocks have been checked.
     * @param position      the start position.
     * @param direction     the direction to search.
     * @param world         the world object.
     * @return the distance.
     */
    private int searchNextBlock(final int blocksChecked, @NotNull final BlockPos position, final Direction direction, @NotNull final World world)
    {
        if (blocksChecked >= getMaxRange() || isNoPartOfField(world, position))
        {
            return blocksChecked;
        }
        return searchNextBlock(blocksChecked + 1, position.offset(direction), direction, world);
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
        return world.isAirBlock(position) ||  isValidDelimiter(world.getBlockState(position.up()).getBlock());
    }

    /**
     * Check if a block is a valid delimiter of the field.
     * @param block the block to analyze.
     * @return true if so.
     */
    private static boolean isValidDelimiter(final Block block)
    {
        return block instanceof FenceBlock || block instanceof FenceGateBlock || block == ModBlocks.blockCactusFence || block == ModBlocks.blockCactusFenceGate || block instanceof WallBlock;
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
        if (inventory.getStackInSlot(0) != ItemStackUtils.EMPTY && inventory.getStackInSlot(0).getItem() instanceof IPlantable)
        {
            return inventory.getStackInSlot(0);
        }
        return null;
    }

    /**
     * Getter of the length in plus x direction.
     *
     * @return field length.
     */
    @Override
    public int getLengthPlusX()
    {
        return lengthPlusX;
    }

    /**
     * Getter of the with in plus z direction.
     *
     * @return field width.
     */
    @Override
    public int getWidthPlusZ()
    {
        return widthPlusZ;
    }

    /**
     * Getter of the length in minus x direction.
     *
     * @return field length.
     */
    @Override
    public int getLengthMinusX()
    {
        return lengthMinusX;
    }

    /**
     * Getter of the with in minus z direction.
     *
     * @return field width.
     */
    @Override
    public int getWidthMinusZ()
    {
        return widthMinusZ;
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
    public void setOwner(@NotNull final int ownerId)
    {
        this.ownerId = ownerId;
        if(colony != null)
        {
            if(colony.getCitizenManager().getCitizen(ownerId) == null)
            {
                owner = "";
            }
            else
            {
                owner = colony.getCitizenManager().getCitizen(ownerId).getName();
            }
        }
        setName(LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user", LanguageHandler.format(owner)));
    }

    /**
     * Sets the owner of the field.
     *
     * @param ownerId the name of the citizen.
     * @param tempColony the colony view.
     */
    @Override
    public void setOwner(final int ownerId, final IColonyView tempColony)
    {
        this.ownerId = ownerId;
        if(tempColony != null)
        {
            if(tempColony.getCitizen(ownerId) == null)
            {
                owner = "";
            }
            else
            {
                owner = tempColony.getCitizen(ownerId).getName();
            }
        }
    }

    /**
     * Get the inventory of the scarecrow.
     * @return the IItemHandler.
     */
    @Override
    public IItemHandlerModifiable getInventory()
    {
        return inventory;
    }

    ///////////---- Following methods are used to update the tileEntity between client and server ----///////////

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        final CompoundNBT compound = new CompoundNBT();
        this.write(compound);
        if(colony != null)
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
        if(compound.keySet().contains(TAG_COLONY_ID))
        {
            setOwner(ownerId, IColonyManager.getInstance().getColonyView(compound.getInt(TAG_COLONY_ID), world.getDimension().getType().getId()));
        }
    }

    /////////////--------------------------- End Synchronization-area ---------------------------- /////////////

    @Override
    public void onLoad()
    {
        super.onLoad();
        final World world = getWorld();

        colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        if (colony != null && !colony.getBuildingManager().getFields().contains(pos))
        {
            @Nullable final Entity entity = EntityUtils.getEntityFromUUID(world, colony.getPermissions().getOwner());

            if (entity instanceof PlayerEntity)
            {
                colony.getBuildingManager().addNewField(this, pos, world);
            }
        }
    }

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
        lengthPlusX = compound.getInt(TAG_LENGTH_PLUS);
        widthPlusZ = compound.getInt(TAG_WIDTH_PLUS);
        lengthMinusX = compound.getInt(TAG_LENGTH_MINUS);
        widthMinusZ = compound.getInt(TAG_WIDTH_MINUS);
        ownerId = compound.getInt(TAG_OWNER);
        name = compound.getString(TAG_NAME);
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
        compound.putInt(TAG_LENGTH_PLUS, lengthPlusX);
        compound.putInt(TAG_WIDTH_PLUS, widthPlusZ);
        compound.putInt(TAG_LENGTH_MINUS, lengthMinusX);
        compound.putInt(TAG_WIDTH_MINUS, widthMinusZ);
        compound.putInt(TAG_OWNER, ownerId);
        compound.putString(TAG_NAME, name);

        return super.write(compound);
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
}
