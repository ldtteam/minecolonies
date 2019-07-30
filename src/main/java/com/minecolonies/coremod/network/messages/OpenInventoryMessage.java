package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.entity.IEntityCitizen;
import com.minecolonies.coremod.inventory.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message sent to open an inventory.
 */
public class OpenInventoryMessage implements IMessage
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
     * Creates an open inventory message for the citizen.
     * @param name the name of the citizen.
     * @param id its id.
     */
    public OpenInventoryMessage(@NotNull final String name, @NotNull final int id)
    {
        super();
        inventoryType = InventoryType.INVENTORY_CITIZEN;
        this.name = name;
        this.entityID = id;
    }

    /**
     * Creates an open inventory message for a building.
     *
     * @param pos the position of the building.
     */
    public OpenInventoryMessage(@NotNull final BlockPos pos)
    {
        super();
        inventoryType = InventoryType.INVENTORY_CHEST;
        name = "";
        tePos = pos;
    }

    /**
     * Creates an open inventory message for a field.
     *
     * @param field    {@link AbstractBuildingView}
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
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        inventoryType = InventoryType.values()[buf.readInt()];
        name = buf.readString();
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                entityID = buf.readInt();
                break;
            case INVENTORY_CHEST:
                tePos = buf.readBlockPos();
                break;
            case INVENTORY_FIELD:
                colonyId = buf.readInt();
                tePos = buf.readBlockPos();
        }
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(inventoryType.ordinal());
        buf.writeString(name);
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                buf.writeInt(entityID);
                break;
            case INVENTORY_CHEST:
                buf.writeBlockPos(tePos);
                break;
            case INVENTORY_FIELD:
                buf.writeInt(colonyId);
                buf.writeBlockPos(tePos);
                break;
        }
    }

    @Override
    public void messageOnServerThread(final OpenInventoryMessage message, final ServerPlayerEntity player)
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

    private static void doCitizenInventory(final OpenInventoryMessage message, final ServerPlayerEntity player)
    {
        @Nullable final IEntityCitizen citizen = (IEntityCitizen) CompatibilityUtils.getWorldFromEntity(player).getEntityByID(message.entityID);
        if (citizen != null && checkPermissions(citizen.getCitizenColonyHandler().getColony(), player))
        {
            if (!StringUtils.isNullOrEmpty(message.name))
            {
                citizen.getInventoryCitizen().setCustomName(message.name);
            }

            player.openGui(MineColonies.instance, GuiHandler.ID.CITIZEN_INVENTORY.ordinal(), player.world, citizen.getCitizenColonyHandler().getColony().getID(), citizen.getCitizenData().getId(), 0);
        }
    }

    private static void doHutInventory(final OpenInventoryMessage message, final ServerPlayerEntity player)
    {
        if (checkPermissions(IColonyManager.getInstance().getClosestColony(player.getEntityWorld(), message.tePos), player))
        {
            @NotNull final TileEntityChest chest = (TileEntityChest) BlockPosUtil.getTileEntity(CompatibilityUtils.getWorldFromEntity(player), message.tePos);
            if (!StringUtils.isNullOrEmpty(message.name))
            {
                chest.setCustomName(message.name);
            }

            player.openGui(MineColonies.instance, GuiHandler.ID.BUILDING_INVENTORY.ordinal(), player.world, chest.getPos().getX(), chest.getPos().getY(), chest.getPos().getZ());
        }
    }

    private static void doFieldInventory(final OpenInventoryMessage message, final ServerPlayerEntity player)
    {
        if (checkPermissions(IColonyManager.getInstance().getClosestColony(player.getEntityWorld(), message.tePos), player))
        {
            player.openGui(MineColonies.instance,
              GuiHandler.ID.BUILDING_INVENTORY.ordinal(),
              player.getEntityWorld(),
              player.getPosition().getX(),
              player.getPosition().getY(),
              player.getPosition().getZ());
        }
    }

    private static boolean checkPermissions(final IColony colony, final EntityPlayerMP player)
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
        INVENTORY_FIELD,
    }
}
