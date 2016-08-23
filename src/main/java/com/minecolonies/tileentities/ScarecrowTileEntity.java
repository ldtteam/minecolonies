package com.minecolonies.tileentities;

import java.util.Random;
import com.minecolonies.inventory.InventoryField;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * The scarecrow tile entity to store extra data.
 */
public class ScarecrowTileEntity extends TileEntity
{
    /**
     * NBTTag to store the type.
     */
    private static final String TAG_TYPE = "type";

    /**
     * The name of the inventory which shows up when opened.
     */
    private static final String INVENTORY_NAME = "Scarecrow";
    //todo change to .lang term

    /**
     * The inventory connected with the scarecrow.
     */
    public InventoryField inventoryField;

    /**
     * Random generator.
     */
    private final Random random = new Random();

    /**
     * The type of the scarecrow.
     */
    private ScareCrowType TYPE;

    /**
     * Enum describing the different textures the scarecrow has.
     */
    public enum ScareCrowType
    {
        PUMPKINHEAD,
        NORMAL
    }

    /**
     * Empty public constructor.
     */
    public ScarecrowTileEntity()
    {
        inventoryField = new InventoryField(INVENTORY_NAME, true);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        
        compound.setInteger(TAG_TYPE, this.getType().ordinal());
        inventoryField.writeToNBT(compound);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        
        TYPE = ScareCrowType.values()[compound.getInteger(TAG_TYPE)];
        inventoryField.readFromNBT(compound);
    }

    public void setInventoryField(final InventoryField inventoryField)
    {
        this.inventoryField = inventoryField;
    }

    /**
     * Returns the type of the scarecrow (Important for the rendering).
     * @return the enum type.
     */
    public ScareCrowType getType()
    {
        if(this.TYPE == null)
        {
            this.TYPE = ScareCrowType.values()[this.random.nextInt(1)];
        }
        return this.TYPE;
    }
}
