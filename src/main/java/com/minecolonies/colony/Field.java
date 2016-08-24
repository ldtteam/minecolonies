package com.minecolonies.colony;

import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.inventory.InventoryField;
import com.minecolonies.util.BlockPosUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the field class.
 */
public class Field extends Container
{
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
     * The max width/length of a field.
     */
    private static final int MAX_RANGE = 5;

    /**
     * The fields location.
     */
    private BlockPos location;

    /**
     * The colony of the field.
     */
    private final Colony colony;

    /**
     * Has the field be taken by any worker?
     */
    private boolean taken = false;

    /**
     * Checks if the field needsWork (Hoeig, Seedings, Farming etc)
     */
    private boolean needsWork = false;

    /**
     * Has the field been planted?
     */
    private FieldStage fieldStage = FieldStage.EMPTY;

    /**
     * The set seed type for the field.
     */
    private ItemSeeds seed;

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
     * Describes the stage the field is in.
     * Like if it has been hoed, planted or is empty.
     */
    public enum FieldStage
    {
        EMPTY,
        HOED,
        PLANTED
    }


     /**
     * Private constructor to create field from NBT.
     * @param colony the colony the field belongs to.
     */
    private Field(Colony colony)
    {
       this.colony = colony;
    }

    /**
     * Creates an instance of our field container, this may be serve to open the GUI.
     * @param inventory the field inventory.
     * @param playerInventory the player inventory.
     * @param world the world.
     * @param location the position of the field.
     */
    public Field(InventoryField inventory, InventoryPlayer playerInventory, World world, BlockPos location)
    {
        this.colony = ColonyManager.getColony(world,location);
        this.location = location;

        this.inventory = inventory;

        addSlotToContainer(new Slot(inventory, 0, 80, 34));

        //Ddd player inventory slots
        // Note: The slot numbers are within the player inventory and may be the same as the field inventory.
        int i;
        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                addSlotToContainer(new Slot(playerInventory, j+i*9+9,
                                            8+j*18, 84+i*18));
            }
        }

        for (i = 0; i < 9; ++i)
        {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18,
                                        142));
        }
        calculateSize(world,location.down());
    }

    /**
     * Calculates recursively the length of the field until a certain point.
     * @param position the start position.
     * @param world the world the field is in.
     */
    private void calculateSize(World world, BlockPos position)
    {
        //Calculate in all 4 directions
        this.lengthPlusX = searchNextBlock(0, position.east(), EnumFacing.EAST, world);
        this.lengthMinusX= searchNextBlock(0, position.west(), EnumFacing.WEST, world);
        this.widthPlusZ  = searchNextBlock(0, position.south(), EnumFacing.SOUTH, world);
        this.widthMinusZ = searchNextBlock(0, position.north(), EnumFacing.NORTH, world);
    }

    /**
     * Calculates the field size into a specific direction.
     * @param blocksChecked how many blocks have been checked.
     * @param position the start position.
     * @param direction the direction to search.
     * @param world the world object.
     * @return the distance.
     */
    private int searchNextBlock(int blocksChecked, BlockPos position, EnumFacing direction, World world)
    {
        if(blocksChecked == getMaxRange() || isNoPartOfField(world, position))
        {
            return blocksChecked;
        }
        return  searchNextBlock(blocksChecked+1,position.offset(direction),direction,world);
    }

    /**
     * Checks if a certain position is part of the field. Complies with the definition of field block.
     * @param world the world object.
     * @param position the position.
     * @return true if it is.
     */
    public boolean isNoPartOfField(World world, BlockPos position)
    {
        return world.isAirBlock(position) || world.getBlockState(position.up()).getBlock().getMaterial().isSolid();
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID
     *
     * @return          {@link BlockPos} of the current object
     */
    public BlockPos getID()
    {
        // Location doubles as ID
        return this.location;
    }

    /**
     * Returns the colony of the field
     *
     * @return          {@link com.minecolonies.colony.Colony} of the current object
     */
    public Colony getColony()
    {
        return this.colony;
    }

    /**
     * Create and load a Field given it's saved NBTTagCompound
     *
     * @param colony    The owning colony
     * @param compound  The saved data
     * @return          {@link com.minecolonies.colony.Field} created from the compound.
     */
    public static Field createFromNBT(Colony colony, NBTTagCompound compound)
    {
        Field field = new Field(colony);
        field.readFromNBT(compound);
        return field;
    }

    /**
     * Save data to NBT compound.
     * Writes the {@link #location} value.
     *
     * @param compound      {@link net.minecraft.nbt.NBTTagCompound} to write data to
     */
    public void writeToNBT(NBTTagCompound compound)
    {
        BlockPosUtil.writeToNBT(compound, TAG_LOCATION, this.location);
        compound.setBoolean(TAG_TAKEN, taken);
        compound.setInteger(TAG_STAGE, fieldStage.ordinal());
        compound.setInteger(TAG_LENGTH_PLUS, lengthPlusX);
        compound.setInteger(TAG_WIDTH_PLUS, widthPlusZ);
        compound.setInteger(TAG_LENGTH_MINUS, lengthMinusX);
        compound.setInteger(TAG_WIDTH_MINUS, widthMinusZ);
        inventory.writeToNBT(compound);
    }

    /**
     * Save data to NBT compound.
     * Writes the {@link #location} value.
     *
     * @param compound      {@link net.minecraft.nbt.NBTTagCompound} to write data to
     */
    public void readFromNBT(NBTTagCompound compound)
    {
        location = BlockPosUtil.readFromNBT(compound, TAG_LOCATION);
        taken = compound.getBoolean(TAG_TAKEN);
        fieldStage = FieldStage.values()[compound.getInteger(TAG_STAGE)];
        lengthPlusX = compound.getInteger(TAG_LENGTH_PLUS);
        widthPlusZ = compound.getInteger(TAG_WIDTH_PLUS);
        lengthMinusX = compound.getInteger(TAG_LENGTH_MINUS);
        widthMinusZ = compound.getInteger(TAG_WIDTH_MINUS);
        inventory = new InventoryField("Scarecrow", true);
        inventory.readFromNBT(compound);
    }

    /**
     * Has the field been taken?
     * @return true if the field is not free to use, false after releasing it.
     */
    public boolean isTaken() {
        return this.taken;
    }

    /**
     * Sets the field taken.
     * @param taken is field free or not
     */
    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    /**
     * Checks if the field has been planted.
     * @return true if there are crops planted.
     */
    public FieldStage getFieldStage()
    {
        return this.fieldStage;
    }

    /**
     * Sets if there are any crops planted.
     * @param fieldStage true after planting, false after harvesting.
     */
    public void setFieldStage(FieldStage fieldStage)
    {
        this.fieldStage = fieldStage;
    }

    /**
     * Checks if the field needs work (planting, hoeing)
     * @return true if so.
     */
    public boolean needsWork()
    {
        return this.needsWork;
    }

    /**
     * Sets that the field needs work
     * @param needsWork true if work needed, false after completing the job.
     */
    public void setNeedsWork(boolean needsWork)
    {
        this.needsWork = needsWork;
    }

    /**
     * Getter for MAX_RANGE.
     * @return the max range.
     */
    public int getMaxRange()
    {
        return MAX_RANGE;
    }

    /**
     * Getter of the seed of the field
     * @return the ItemSeed
     */
    public ItemSeeds getSeed()
    {
        return seed;
    }

    /**
     * Getter of the length in plus x direction.
     * @return field length.
     */
    public int getLengthPlusX()
    {
        return lengthPlusX;
    }

    /**
     * Getter of the with in plus z direction.
     * @return field width.
     */
    public int getWidthPlusZ()
    {
        return widthPlusZ;
    }

    /**
     * Getter of the length in minus x direction.
     * @return field length.
     */
    public int getLengthMinusX()
    {
        return lengthMinusX;
    }

    /**
     * Getter of the with in minus z direction.
     * @return field width.
     */
    public int getWidthMinusZ()
    {
        return widthMinusZ;
    }

    /**
     * Location getter.
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

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return getColony().getPermissions().hasPermission(playerIn, Permissions.Action.ACCESS_HUTS);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex)
    {
        if (slotIndex == 0)
        {
            playerIn.inventory.addItemStackToInventory(inventory.getStackInSlot(0));
            inventory.setInventorySlotContents(0, null);
        }
        else if(inventory.getStackInSlot(0) == null)
        {
            int playerIndex = slotIndex < 28 ? (slotIndex + 8) : (slotIndex - 28);
            if(playerIn.inventory.getStackInSlot(playerIndex) != null)
            {
                ItemStack stack = playerIn.inventory.getStackInSlot(playerIndex).splitStack(1);
                inventory.setInventorySlotContents(0, stack);
                if(playerIn.inventory.getStackInSlot(playerIndex).stackSize == 0)
                {
                    playerIn.inventory.removeStackFromSlot(playerIndex);
                }

                if(stack.getItem() != null && stack.getItem() instanceof ItemSeeds)
                {
                    seed = (ItemSeeds)stack.getItem();
                }
            }
        }

        return null;
    }

}
