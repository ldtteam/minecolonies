package com.minecolonies.tileentities;

import java.util.Random;
import com.minecolonies.inventory.InventoryField;
import com.minecolonies.util.LanguageHandler;
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
    private ScareCrowType type;

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
        super();
        inventoryField = new InventoryField(LanguageHandler.getString("com.minecolonies.gui.inventory.scarecrow"), true);
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
        
        type = ScareCrowType.values()[compound.getInteger(TAG_TYPE)];
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
        if(this.type == null)
        {
            this.type = ScareCrowType.values()[this.random.nextInt(1)];
        }
        return this.type;
    }
}
