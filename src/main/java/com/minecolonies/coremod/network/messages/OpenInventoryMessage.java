package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;

import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
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
        name = buf.readString(32767);
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

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayerEntity player = ctxIn.getSender();
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                doCitizenInventory(player);
                break;
            case INVENTORY_CHEST:
                doHutInventory(player);
                break;
            case INVENTORY_FIELD:
                doFieldInventory(player);
                break;
            default:
                break;
        }
    }

    private void doCitizenInventory(final ServerPlayerEntity player)
    {
        @Nullable final AbstractEntityCitizen citizen = (AbstractEntityCitizen) CompatibilityUtils.getWorldFromEntity(player).getEntityByID(entityID);
        if (citizen != null && checkPermissions(citizen.getCitizenColonyHandler().getColony(), player))
        {
            if (!StringUtils.isNullOrEmpty(name))
            {
                citizen.getInventoryCitizen().setCustomName(name);
            }

            NetworkHooks.openGui(player, citizen, packetBuffer -> packetBuffer.writeVarInt(citizen.getCitizenColonyHandler().getColonyId()).writeVarInt(citizen.getCitizenId()));
        }
    }

    private void doHutInventory(final ServerPlayerEntity player)
    {
        if (checkPermissions(IColonyManager.getInstance().getClosestColony(player.getEntityWorld(), tePos), player))
        {
            @NotNull final TileEntityColonyBuilding chest = (TileEntityColonyBuilding) BlockPosUtil.getTileEntity(CompatibilityUtils.getWorldFromEntity(player), tePos);
            if (!StringUtils.isNullOrEmpty(name))
            {
                chest.setCustomName(new StringTextComponent(name));
            }

            NetworkHooks.openGui(player, chest, packetBuffer -> packetBuffer.writeVarInt(chest.getColonyId()).writeBlockPos(chest.getPos()));
        }
    }

    private void doFieldInventory(final ServerPlayerEntity player)
    {
        if (checkPermissions(IColonyManager.getInstance().getClosestColony(player.getEntityWorld(), tePos), player))
        {
            @NotNull final ScarecrowTileEntity scarecrowTileEntity = (ScarecrowTileEntity) BlockPosUtil.getTileEntity(CompatibilityUtils.getWorldFromEntity(player), tePos);
            NetworkHooks.openGui(player, scarecrowTileEntity);

        }
    }

    private static boolean checkPermissions(final IColony colony, final ServerPlayerEntity player)
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
