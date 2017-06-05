package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.colony.management.ColonyManager;
import com.minecolonies.api.entity.ai.citizen.farmer.ScareCrowType;
import com.minecolonies.api.lib.Constants;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.inventory.InventoryField;
import com.minecolonies.coremod.util.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * The scarecrow tile entity to store extra data.
 */
public class ScarecrowTileEntity extends TileEntityChest implements com.minecolonies.api.entity.ai.citizen.farmer.IScarecrow
{
    /**
     * NBTTag to store the type.
     */
    private static final String TAG_TYPE = "type";

    /**
     * Tag to store the inventory to nbt.
     */
    private static final String TAG_INVENTORY = "inventory";

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
    private ItemStackHandler inventoryField;

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
        this.inventoryField = new InventoryField();
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
    }

    ///////////---- Following methods are used to update the tileEntity between client and server ----///////////

    /**
     * Returns the type of the scarecrow (Important for the rendering).
     *
     * @return the enum type.
     */
    @Override
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
    @Override
    public ItemStackHandler getInventoryField()
    {
        return inventoryField;
    }

    /////////////--------------------------- End Synchronization-area ---------------------------- /////////////

    /**
     * Set the inventory connected with the scarecrow.
     *
     * @param inventoryField the field to set it to
     */
    @Override
    public final void setInventoryField(final ItemStackHandler inventoryField)
    {
        this.inventoryField = inventoryField;
    }

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
        if (compound.hasKey(Constants.MOD_ID + TAG_INVENTORY))
        {
            getInventoryField().deserializeNBT((NBTTagCompound) compound.getTag(Constants.MOD_ID + TAG_INVENTORY));
        }
        name = compound.getString(TAG_NAME);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger(TAG_TYPE, this.getType().ordinal());
        compound.setTag(Constants.MOD_ID + TAG_INVENTORY, getInventoryField().serializeNBT());
        compound.setString(TAG_NAME, name);
        return compound;
    }
}
