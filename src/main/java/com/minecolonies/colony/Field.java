package com.minecolonies.colony;

import com.minecolonies.inventory.InventoryField;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import com.minecolonies.util.BlockPosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

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
     * Tag to store the fields length.
     */
    private static final String TAG_LENGTH = "length";

    /**
     * Tag to store the fields width.
     */
    private static final String TAG_WIDTH = "width";

    /**
     * The max width/length of a field.
     */
    private static final int MAX_RANGE = 10;

    /**
     * The fields location.
     */
    private final BlockPos location;

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
    private boolean planted = false;

    /**
     * The set seed type for the field.
     */
    private ItemSeeds seed;

    /**
     * The length of the field.
     */
    private int length;

    /**
     * The width of the seed;
     */
    private int width;

    /**
     * The inventorySlot of the field.
     */
    private InventoryField inventory;

    /**
     * Creates a new field object.
     * @param colony The colony the field is a part of.
     * @param location The location the field has been placed.
     * @param width The fields width.
     * @param length The fields length.
     */
    public Field(Colony colony, BlockPos location, int width, int length, InventoryField inventory)
    {
        this.location = location;
        this.colony   = colony;
        this.length = length;
        this.width = width;
        this.inventory = inventory;
    }

    public Field(InventoryField inventory, final InventoryPlayer playerInventory)
    {
        colony = this.getColony();
        location = new BlockPos(0,1,2);

        this.inventory = inventory;

        addSlotToContainer(new Slot(inventory, 0, 80, 34));

        // add player inventory slots
        // note that the slot numbers are within the player inventory so can
        // be same as the tile entity inventory
        int i;
        for (i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                addSlotToContainer(new Slot(playerInventory, j+i*9+9,
                                            8+j*18, 84+i*18));
            }
        }

        // add hotbar slots
        for (i = 0; i < 9; ++i)
        {
            addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18,
                                        142));
        }
    }

    //todo some problem with the remove stuff
    @Override
    public ItemStack slotClick(final int slotId, final int clickedButton, final int mode, final EntityPlayer playerIn)
    {
        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    @Override
    protected void retrySlotClick(final int slotId, final int clickedButton, final boolean mode, final EntityPlayer playerIn)
    {
        super.retrySlotClick(slotId, clickedButton, mode, playerIn);
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID
     *
     * @return          {@link BlockPos} of the current object
     */
    public BlockPos getID()
    {
        return this.location; //  Location doubles as ID
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
     * Create and load a Building given it's saved NBTTagCompound
     *
     * @param colony    The owning colony
     * @param compound  The saved data
     * @return          {@link com.minecolonies.colony.Field} created from the compound.
     */
    public static Field createFromNBT(Colony colony, NBTTagCompound compound)
    {
        BlockPos pos = BlockPosUtil.readFromNBT(compound, TAG_LOCATION);
        Boolean free = compound.getBoolean(TAG_TAKEN);
        int localLength = compound.getInteger(TAG_LENGTH);
        int localWidth = compound.getInteger(TAG_WIDTH);
        //todo what happens after shutdown?
        Field field = new Field(colony,pos,localWidth,localLength, new InventoryField("Scarecrow ", true));
        field.setTaken(free);
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
    public boolean isPlanted()
    {
        return this.planted;
    }

    /**
     * Sets if there are any crops planted.
     * @param planted true after planting, false after harvesting.
     */
    public void setPlanted(boolean planted)
    {
        this.planted = planted;
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
     * Getter for the length.
     * @return the fields length.
     */
    public int getLength()
    {
        return this.length;
    }

    /**
     * Getter for the width.
     * @return the fields with.
     */
    public int getWidth()
    {
        return this.width;
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
    public boolean canInteractWith(final EntityPlayer playerIn)
    {
        return true;
    }
}
