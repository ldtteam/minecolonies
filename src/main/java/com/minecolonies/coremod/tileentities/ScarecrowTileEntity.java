package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.inventory.InventoryField;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * NBTag to store the name.
     */
    private static final String TAG_NAME = "name";

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
    private ScareCrowType type;

    /**
     * Name of the scarecrow, string set in the GUI.
     */
    private String name = LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user", LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user.noone"));

    /**
     * Creates an instance of the tileEntity.
     */
    public ScarecrowTileEntity()
    {
        super();
        this.inventoryField = new InventoryField(name);
    }

    /**
     * Getter of the name of the tileEntity.
     *
     * @return the string.
     */
    public String getDesc()
    {
        return name;
    }

    /**
     * Setter for the name.
     *
     * @param name string to set.
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    ///////////---- Following methods are used to update the tileEntity between client and server ----///////////

    @NotNull
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        @NotNull final NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new SPacketUpdateTileEntity(this.getPos(), 0, tag);
    }

    @Override
    public void onDataPacket(final NetworkManager net, @NotNull final SPacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());
    }

    /////////////--------------------------- End Synchronization-area ---------------------------- /////////////

    @Override
    public void onLoad()
    {
        super.onLoad();
        final World world = getWorld();

        @Nullable final Colony colony = ColonyManager.getColony(world, pos);
        if (colony != null && colony.getField(pos) == null)
        {
            @Nullable final Entity entity = EntityUtils.getEntityFromUUID(world, colony.getPermissions().getOwner());

            if (entity instanceof EntityPlayer)
            {
                colony.addNewField(this, ((EntityPlayer) entity).inventory, pos, world);
            }
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        type = ScareCrowType.values()[compound.getInteger(TAG_TYPE)];
        getInventoryField().readFromNBT(compound);
        name = compound.getString(TAG_NAME);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setInteger(TAG_TYPE, this.getType().ordinal());
        getInventoryField().writeToNBT(compound);
        compound.setString(TAG_NAME, name);
        return compound;
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
     * Get the inventory connected with the scarecrow.
     *
     * @return the inventory field of this scarecrow
     */
    public InventoryField getInventoryField()
    {
        return inventoryField;
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
     * Enum describing the different textures the scarecrow has.
     */
    public enum ScareCrowType
    {
        PUMPKINHEAD,
        NORMAL
    }
}
