package com.minecolonies.network.messages;

import com.minecolonies.colony.CitizenDataView;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.inventory.InventoryField;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message sent to open an inventory.
 */
public class OpenInventoryMessage extends AbstractMessage<OpenInventoryMessage, IMessage>
{
    /***
     * The inventory name.
     */
    private String        name;
    /**
     * The inventory type.
     */
    private InventoryType inventoryType;
    /**
     * The entities id.
     */
    private int           entityID;
    /**
     * The position of the inventory block/entity.
     */
    private BlockPos      tePos;
    /**
     * The colony id the field or building etc is in.
     */
    private int           colonyId;

    /**
     * Empty public constructor.
     */
    public OpenInventoryMessage()
    {
        super();
    }

    /**
     * Creates an open inventory message for a citizen.
     *
     * @param citizen {@link CitizenDataView}
     */
    public OpenInventoryMessage(@NotNull CitizenDataView citizen)
    {
        super();
        inventoryType = InventoryType.INVENTORY_CITIZEN;
        name = citizen.getName();
        this.entityID = citizen.getEntityId();
    }

    /**
     * Creates an open inventory message for a building
     *
     * @param building {@link AbstractBuilding.View}
     */
    public OpenInventoryMessage(@NotNull AbstractBuilding.View building)
    {
        super();
        inventoryType = InventoryType.INVENTORY_CHEST;
        name = "";
        tePos = building.getLocation();
    }

    /**
     * Creates an open inventory message for a field
     *
     * @param field    {@link AbstractBuilding.View}
     * @param colonyId the colony associated with the inventory.
     */
    public OpenInventoryMessage(BlockPos field, int colonyId)
    {
        super();
        inventoryType = InventoryType.INVENTORY_FIELD;
        name = "field";
        tePos = field;
        this.colonyId = colonyId;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        inventoryType = InventoryType.values()[buf.readInt()];
        name = ByteBufUtils.readUTF8String(buf);
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                entityID = buf.readInt();
                break;
            case INVENTORY_CHEST:
                tePos = BlockPosUtil.readFromByteBuf(buf);
                break;
            case INVENTORY_FIELD:
                colonyId = buf.readInt();
                tePos = BlockPosUtil.readFromByteBuf(buf);
        }
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(inventoryType.ordinal());
        ByteBufUtils.writeUTF8String(buf, name);
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                buf.writeInt(entityID);
                break;
            case INVENTORY_CHEST:
                BlockPosUtil.writeToByteBuf(buf, tePos);
                break;
            case INVENTORY_FIELD:
                buf.writeInt(colonyId);
                BlockPosUtil.writeToByteBuf(buf, tePos);
                break;
        }
    }

    @Override
    public void messageOnServerThread(final OpenInventoryMessage message, final EntityPlayerMP player)
    {
        switch (message.inventoryType)
        {
            case INVENTORY_CITIZEN:
                @NotNull final InventoryCitizen citizenInventory = ((EntityCitizen) player.worldObj.getEntityByID(message.entityID)).getInventoryCitizen();
                if (!StringUtils.isNullOrEmpty(message.name))
                {
                    citizenInventory.setCustomName(message.name);
                }
                player.displayGUIChest(citizenInventory);
                break;
            case INVENTORY_CHEST:
                @NotNull final TileEntityChest chest = (TileEntityChest) BlockPosUtil.getTileEntity(player.worldObj, message.tePos);
                if (!StringUtils.isNullOrEmpty(message.name))
                {
                    chest.setCustomName(message.name);
                }
                player.displayGUIChest(chest);
                break;
            case INVENTORY_FIELD:
                @NotNull final InventoryField inventoryField = ColonyManager.getColony(colonyId).getField(message.tePos).getInventoryField();
                if (!StringUtils.isNullOrEmpty(message.name))
                {
                    inventoryField.setCustomName(message.name);
                }
                player.displayGUIChest(inventoryField);
                break;
        }
    }

    /**
     * Type of inventory.
     */
    private enum InventoryType
    {
        INVENTORY_CITIZEN,
        INVENTORY_CHEST,
        INVENTORY_FIELD
    }
}
