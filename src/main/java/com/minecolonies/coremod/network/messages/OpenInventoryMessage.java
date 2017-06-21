package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.inventory.InventoryField;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public OpenInventoryMessage(@NotNull final CitizenDataView citizen)
    {
        super();
        inventoryType = InventoryType.INVENTORY_CITIZEN;
        name = citizen.getName();
        this.entityID = citizen.getEntityId();
    }

    /**
     * Creates an open inventory message for a building.
     *
     * @param building {@link AbstractBuilding.View}
     */
    public OpenInventoryMessage(@NotNull final AbstractBuilding.View building)
    {
        super();
        inventoryType = InventoryType.INVENTORY_CHEST;
        name = "";
        tePos = building.getLocation();
    }

    /**
     * Creates an open inventory message for a field.
     *
     * @param field    {@link AbstractBuilding.View}
     * @param colonyId the colony associated with the inventory.
     */
    public OpenInventoryMessage(final BlockPos field, final int colonyId)
    {
        super();
        inventoryType = InventoryType.INVENTORY_FIELD;
        name = "field";
        tePos = field;
        this.colonyId = colonyId;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
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
    public void toBytes(@NotNull final ByteBuf buf)
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
                doCitizenInventory(message, player);
                break;
            case INVENTORY_CHEST:
                doHutInventory(message, player);
                break;
            case INVENTORY_FIELD:
                doFieldInventory(message, player);
                break;
            default:
                break;
        }
    }

    private static void doCitizenInventory(final OpenInventoryMessage message, final EntityPlayerMP player)
    {
        @Nullable final EntityCitizen citizen = (EntityCitizen) CompatibilityUtils.getWorld(player).getEntityByID(message.entityID);
        if (citizen != null && checkPermissions(citizen.getColony(), player))
        {
            if (!StringUtils.isNullOrEmpty(message.name))
            {
                citizen.getInventoryCitizen().setCustomName(message.name);
            }
            //TODO(OrionDevelopment): Convert next line to:
            //player.displayGUIChest(new IItemHandlerToIInventoryWrapper(citizen.getInventoryCitizen(), citizen.getInventoryCitizen()));
            player.displayGUIChest(citizen.getInventoryCitizen());
        }
    }

    private static void doHutInventory(final OpenInventoryMessage message, final EntityPlayerMP player)
    {

        if (checkPermissions(ColonyManager.getClosestColony(player.getEntityWorld(), message.tePos), player))
        {
            @NotNull final TileEntityChest chest = (TileEntityChest) BlockPosUtil.getTileEntity(CompatibilityUtils.getWorld(player), message.tePos);
            if (!StringUtils.isNullOrEmpty(message.name))
            {
                chest.setCustomName(message.name);
            }
            player.displayGUIChest(chest);
        }
    }

    private static void doFieldInventory(final OpenInventoryMessage message, final EntityPlayerMP player)
    {
        if (checkPermissions(ColonyManager.getClosestColony(player.getEntityWorld(), message.tePos), player))
        {
            @NotNull final InventoryField inventoryField = ColonyManager.getColony(message.colonyId).getField(message.tePos).getInventoryField();
            if (!StringUtils.isNullOrEmpty(message.name))
            {
                inventoryField.setCustomName(message.name);
            }
            player.displayGUIChest(inventoryField);
        }
    }

    private static boolean checkPermissions(final Colony colony, final EntityPlayerMP player)
    {
        //Verify player has permission to change this huts settings
        return colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS);
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
