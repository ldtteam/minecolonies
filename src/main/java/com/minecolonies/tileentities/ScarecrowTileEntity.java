package com.minecolonies.tileentities;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.inventory.InventoryField;
import com.minecolonies.util.EntityUtils;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import java.util.Random;

/**
 * The scarecrow tile entity to store extra data.
 */
public class ScarecrowTileEntity extends TileEntityChest
{
    /**
     * NBTTag to store the type.
     */
    private static final String TAG_TYPE = "type";
    /**
     * Random generator.
     */
    private final Random random = new Random();
    /**
     * The inventory connected with the scarecrow.
     */
    private InventoryField inventoryField;
    /**
     * The type of the scarecrow.
     */
    private ScareCrowType  type;

    /**
     * Creates an instance of the tileEntity.
     */
    public ScarecrowTileEntity()
    {
        super();
        this.inventoryField = new InventoryField(LanguageHandler.getString("com.minecolonies.gui.inventory.scarecrow"), true);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        World world = getWorld();

        if(world != null)
        {
            Colony colony = ColonyManager.getColony(world, pos);

            if (colony != null && colony.getField(pos) == null)
            {
                Entity entity = EntityUtils.getEntityFromUUID(world, colony.getPermissions().getOwner());

                if (entity instanceof EntityPlayer)
                {
                    colony.addNewField(this, ((EntityPlayer) entity).inventory, pos, world);
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        type = ScareCrowType.values()[compound.getInteger(TAG_TYPE)];
        getInventoryField().readFromNBT(compound);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setInteger(TAG_TYPE, this.getType().ordinal());
        getInventoryField().writeToNBT(compound);
    }

    /**
     * Returns the type of the scarecrow (Important for the rendering).
     *
     * @return the enum type.
     */
    public ScareCrowType getType()
    {
        if (this.type == null)
        {
            this.type = ScareCrowType.values()[this.random.nextInt(2)];
        }
        return this.type;
    }
    /**
     * Set the inventory connected with the scarecrow.
     *
     * @param inventoryField the field to set it to
     */
    public final void setInventoryField(final InventoryField inventoryField)
    {
        this.inventoryField = inventoryField;
    }

    /**
     * Get the inventory connected with the scarecrow.
     *
     * @return the inventory field of this scarecrow
     */
    public InventoryField getInventoryField()
    {
        return inventoryField;
    }

    /**
     * Enum describing the different textures the scarecrow has.
     */
    public enum ScareCrowType
    {
        PUMPKINHEAD,
        NORMAL
    }
}
