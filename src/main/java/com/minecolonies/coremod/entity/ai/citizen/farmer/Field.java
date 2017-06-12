package com.minecolonies.coremod.entity.ai.citizen.farmer;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.inventory.InventoryField;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles the field class.
 */
public class Field extends Container
{
    /**
     * The size of a normal inventory.
     */
    private static final int MAX_INVENTORY_INDEX = 28;

    /**
     * The size of the the inventory hotbar.
     */
    private static final int INVENTORY_BAR_SIZE = 8;

    /**
     * X-Offset of the inventory slot in the GUI of the scarecrow.
     */
    private static final int X_OFFSET = 80;

    /**
     * Y-Offset of the inventory slot in the GUI of the scarecrow.
     */
    private static final int Y_OFFSET = 34;

    /**
     * Tag to store the location.
     */
    private static final String TAG_LOCATION = "location";

    /**
     * Tag to store if the field has been taken.
     */
    private static final String TAG_TAKEN = "taken";

    /**
     * Tag to store the fields positive length.
     */
    private static final String TAG_LENGTH_PLUS = "length+";

    /**
     * Tag to store the fields positive width.
     */
    private static final String TAG_WIDTH_PLUS = "width+";

    /**
     * Tag to store the fields negative length.
     */
    private static final String TAG_LENGTH_MINUS = "length-";

    /**
     * Tag to store the fields negative width.
     */
    private static final String TAG_WIDTH_MINUS = "width-";

    /**
     * Tag to store the fields stage.
     */
    private static final String TAG_STAGE = "stage";

    /**
     * Tag to store the owner.
     */
    private static final String TAG_OWNER = "owner";

    /**
     * Amount of rows in the player inventory.
     */
    private static final int PLAYER_INVENTORY_ROWS = 3;

    /**
     * Amount of columns in the player inventory.
     */
    private static final int PLAYER_INVENTORY_COLUMNS = 9;

    /**
     * Initial x-offset of the inventory slot.
     */
    private static final int PLAYER_INVENTORY_INITIAL_X_OFFSET = 8;

    /**
     * Initial y-offset of the inventory slot.
     */
    private static final int PLAYER_INVENTORY_INITIAL_Y_OFFSET = 84;

    /**
     * Each offset of the inventory slots.
     */
    private static final int PLAYER_INVENTORY_OFFSET_EACH = 18;

    /**
     * Initial y-offset of the inventory slots in the hotbar.
     */
    private static final int PLAYER_INVENTORY_HOTBAR_OFFSET = 142;

    /**
     * The max width/length of a field.
     */
    private static final int MAX_RANGE = 5;

    /**
     * The colony of the field.
     */
    @Nullable
    private final Colony colony;

    /**
     * The fields location.
     */
    private BlockPos location;

    /**
     * Has the field be taken by any worker?
     */
    private boolean taken = false;

    /**
     * Checks if the field needsWork (Hoeig, Seedings, Farming etc).
     */
    private boolean needsWork = true;

    /**
     * Has the field been planted?
     */
    private FieldStage fieldStage = FieldStage.EMPTY;

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
     * The inventorySlot of the field.
     */
    private InventoryField inventory;

    /**
     * Name of the citizen claiming the field.
     */
    @NotNull
    private String owner = "";

    /**
     * Private constructor to create field from NBT.
     *
     * @param colony the colony the field belongs to.
     */
    private Field(final Colony colony)
    {
        super();
        this.colony = colony;
    }

    /**
     * Creates an instance of our field container, this may be serve to open the GUI.
     *
     * @param scarecrowTileEntity the tileEntity of the field containing the inventory.
     * @param playerInventory     the player inventory.
     * @param world               the world.
     * @param location            the position of the field.
     */
    public Field(@NotNull final ScarecrowTileEntity scarecrowTileEntity, final InventoryPlayer playerInventory, @NotNull final World world, @NotNull final BlockPos location)
    {
        super();
        this.colony = ColonyManager.getColony(world, location);
        this.location = location;
        this.inventory = scarecrowTileEntity.getInventoryField();

        addSlotToContainer(new Slot(inventory, 0, X_OFFSET, Y_OFFSET));

        //Ddd player inventory slots
        // Note: The slot numbers are within the player inventory and may be the same as the field inventory.
        int i;
        for (i = 0; i < PLAYER_INVENTORY_ROWS; i++)
        {
            for (int j = 0; j < PLAYER_INVENTORY_COLUMNS; j++)
            {
                addSlotToContainer(new Slot(
                                             playerInventory,
                                             j + i * PLAYER_INVENTORY_COLUMNS + PLAYER_INVENTORY_COLUMNS,
                                             PLAYER_INVENTORY_INITIAL_X_OFFSET + j * PLAYER_INVENTORY_OFFSET_EACH,
                                             PLAYER_INVENTORY_INITIAL_Y_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < PLAYER_INVENTORY_COLUMNS; i++)
        {
            addSlotToContainer(new Slot(
                                         playerInventory, i,
                                         PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
                                         PLAYER_INVENTORY_HOTBAR_OFFSET
            ));
        }
    }

    @Override
    protected final Slot addSlotToContainer(final Slot slotToAdd)
    {
        return super.addSlotToContainer(slotToAdd);
    }

    @Nullable
    @Override
    public ItemStack transferStackInSlot(@NotNull final EntityPlayer playerIn, final int slotIndex)
    {
        if (slotIndex == 0)
        {
            playerIn.inventory.addItemStackToInventory(inventory.getStackInSlot(0));
            inventory.setInventorySlotContents(0, null);
        }
        else if (ItemStackUtils.isEmpty(inventory.getStackInSlot(0)))
        {
            final int playerIndex = slotIndex < MAX_INVENTORY_INDEX ? (slotIndex + INVENTORY_BAR_SIZE) : (slotIndex - MAX_INVENTORY_INDEX);
            if (!ItemStackUtils.isEmpty(playerIn.inventory.getStackInSlot(playerIndex)))
            {
                @NotNull final ItemStack stack = playerIn.inventory.getStackInSlot(playerIndex).splitStack(1);
                inventory.setInventorySlotContents(0, stack);
                if (ItemStackUtils.isEmpty(playerIn.inventory.getStackInSlot(playerIndex)))
                {
                    playerIn.inventory.removeStackFromSlot(playerIndex);
                }
            }
        }

        return null;
    }

    @Override
    public boolean canInteractWith(@NotNull final EntityPlayer playerIn)
    {
        return getColony().getPermissions().hasPermission(playerIn, Action.ACCESS_HUTS);
    }

    /**
     * Returns the colony of the field.
     *
     * @return {@link com.minecolonies.coremod.colony.Colony} of the current object.
     */
    @Nullable
    public Colony getColony()
    {
        return this.colony;
    }

    /**
     * Create and load a Field given it's saved NBTTagCompound.
     *
     * @param colony   The owning colony.
     * @param compound The saved data.
     * @return {@link Field} created from the compound.
     */
    @NotNull
    public static Field createFromNBT(final Colony colony, @NotNull final NBTTagCompound compound)
    {
        @NotNull final Field field = new Field(colony);
        field.readFromNBT(compound);
        return field;
    }

    /**
     * Save data to NBT compound.
     * Writes the {@link #location} value.
     *
     * @param compound {@link net.minecraft.nbt.NBTTagCompound} to write data to.
     */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        location = BlockPosUtil.readFromNBT(compound, TAG_LOCATION);
        taken = compound.getBoolean(TAG_TAKEN);
        fieldStage = FieldStage.values()[compound.getInteger(TAG_STAGE)];
        lengthPlusX = compound.getInteger(TAG_LENGTH_PLUS);
        widthPlusZ = compound.getInteger(TAG_WIDTH_PLUS);
        lengthMinusX = compound.getInteger(TAG_LENGTH_MINUS);
        widthMinusZ = compound.getInteger(TAG_WIDTH_MINUS);
        inventory = new InventoryField("");
        inventory.readFromNBT(compound);
        setOwner(compound.getString(TAG_OWNER));
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
    public final void calculateSize(@NotNull final World world, @NotNull final BlockPos position)
    {
        //Calculate in all 4 directions
        this.lengthPlusX = searchNextBlock(0, position.east(), EnumFacing.EAST, world);
        this.lengthMinusX = searchNextBlock(0, position.west(), EnumFacing.WEST, world);
        this.widthPlusZ = searchNextBlock(0, position.south(), EnumFacing.SOUTH, world);
        this.widthMinusZ = searchNextBlock(0, position.north(), EnumFacing.NORTH, world);
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
    private int searchNextBlock(final int blocksChecked, @NotNull final BlockPos position, final EnumFacing direction, @NotNull final World world)
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
    public boolean isNoPartOfField(@NotNull final World world, @NotNull final BlockPos position)
    {
        return world.isAirBlock(position) || world.getBlockState(position.up()).getMaterial().isSolid();
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID.
     *
     * @return {@link BlockPos} of the current object.
     */
    public BlockPos getID()
    {
        // Location doubles as ID
        return this.location;
    }

    /**
     * Save data to NBT compound.
     * Writes the {@link #location} value.
     *
     * @param compound {@link net.minecraft.nbt.NBTTagCompound} to write data to.
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        BlockPosUtil.writeToNBT(compound, TAG_LOCATION, this.location);
        compound.setBoolean(TAG_TAKEN, taken);
        compound.setInteger(TAG_STAGE, fieldStage.ordinal());
        compound.setInteger(TAG_LENGTH_PLUS, lengthPlusX);
        compound.setInteger(TAG_WIDTH_PLUS, widthPlusZ);
        compound.setInteger(TAG_LENGTH_MINUS, lengthMinusX);
        compound.setInteger(TAG_WIDTH_MINUS, widthMinusZ);
        inventory.writeToNBT(compound);
        compound.setString(TAG_OWNER, owner);
    }

    /**
     * Has the field been taken?
     *
     * @return true if the field is not free to use, false after releasing it.
     */
    public boolean isTaken()
    {
        return this.taken;
    }

    /**
     * Sets the field taken.
     *
     * @param taken is field free or not
     */
    public void setTaken(final boolean taken)
    {
        this.taken = taken;
    }

    public void nextState()
    {
        if(getFieldStage().ordinal() + 1 >= FieldStage.values().length)
        {
            needsWork = false;
            setFieldStage(FieldStage.values()[0]);
            return;
        }
        setFieldStage(FieldStage.values()[getFieldStage().ordinal() + 1]);
    }

    /**
     * Checks if the field has been planted.
     *
     * @return true if there are crops planted.
     */
    public FieldStage getFieldStage()
    {
        return this.fieldStage;
    }

    /**
     * Sets if there are any crops planted.
     *
     * @param fieldStage true after planting, false after harvesting.
     */
    public void setFieldStage(final FieldStage fieldStage)
    {
        this.fieldStage = fieldStage;
    }

    /**
     * Checks if the field needs work (planting, hoeing).
     *
     * @return true if so.
     */
    public boolean needsWork()
    {
        return this.needsWork;
    }

    /**
     * Sets that the field needs work.
     *
     * @param needsWork true if work needed, false after completing the job.
     */
    public void setNeedsWork(final boolean needsWork)
    {
        this.needsWork = needsWork;
    }

    /**
     * Getter of the seed of the field.
     *
     * @return the ItemSeed
     */
    @Nullable
    public ItemStack getSeed()
    {
        if (inventory.getStackInSlot(0) != null && inventory.getStackInSlot(0).getItem() instanceof IPlantable)
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
    public int getLengthPlusX()
    {
        return lengthPlusX;
    }

    /**
     * Getter of the with in plus z direction.
     *
     * @return field width.
     */
    public int getWidthPlusZ()
    {
        return widthPlusZ;
    }

    /**
     * Getter of the length in minus x direction.
     *
     * @return field length.
     */
    public int getLengthMinusX()
    {
        return lengthMinusX;
    }

    /**
     * Getter of the with in minus z direction.
     *
     * @return field width.
     */
    public int getWidthMinusZ()
    {
        return widthMinusZ;
    }

    /**
     * Location getter.
     *
     * @return the location of the scarecrow of the field.
     */
    public BlockPos getLocation()
    {
        return this.location;
    }

    /**
     * Return this citizens inventory.
     *
     * @return the inventory this citizen has.
     */
    @NotNull
    public InventoryField getInventoryField()
    {
        return inventory;
    }

    /**
     * Sets the inventory of the field.
     *
     * @param inventory the inventory to set.
     */
    public void setInventoryField(final InventoryField inventory)
    {
        this.inventory = inventory;
    }

    /**
     * Getter of the owner of the field.
     *
     * @return the string description of the citizen.
     */
    @NotNull
    public String getOwner()
    {
        return owner;
    }

    /**
     * Sets the owner of the field.
     *
     * @param owner the name of the citizen.
     */
    public void setOwner(@NotNull final String owner)
    {
        if (owner.isEmpty())
        {
            this.inventory.setCustomName(LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user",
              LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user.noone")));
        }
        else
        {
            this.inventory.setCustomName(LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user", owner));
        }
        this.owner = owner;
    }

    /**
     * Setter for a custom description of the inventory.
     *
     * @param customName the name to set.
     */
    public void setCustomName(final String customName)
    {
        this.inventory.setCustomName(customName);
    }

    /**
     * Describes the stage the field is in.
     * Like if it has been hoed, planted or is empty.
     */
    public enum FieldStage
    {
        EMPTY,
        HOED,
        PLANTED
    }
}
