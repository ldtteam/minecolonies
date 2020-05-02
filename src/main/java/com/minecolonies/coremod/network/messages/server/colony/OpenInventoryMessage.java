package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message sent to open an inventory.
 */
public class OpenInventoryMessage extends AbstractColonyServerMessage
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
    public OpenInventoryMessage(IColonyView colony, @NotNull final String name, final int id)
    {
        super(colony);
        inventoryType = InventoryType.INVENTORY_CITIZEN;
        this.name = name;
        this.entityID = id;
    }

    /**
     * Creates an open inventory message for a building.
     */
    public OpenInventoryMessage(final IBuildingView building)
    {
        super(building.getColony());
        inventoryType = InventoryType.INVENTORY_CHEST;
        name = "";
        tePos = building.getID();
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        inventoryType = InventoryType.values()[buf.readInt()];
        name = buf.readString(32767);
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                entityID = buf.readInt();
                break;
            case INVENTORY_CHEST:
            case INVENTORY_FIELD:
                tePos = buf.readBlockPos();
                break;
        }
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeInt(inventoryType.ordinal());
        buf.writeString(name);
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                buf.writeInt(entityID);
                break;
            case INVENTORY_CHEST:
            case INVENTORY_FIELD:
                buf.writeBlockPos(tePos);
                break;
        }
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final ServerPlayerEntity player = ctxIn.getSender();
        if (player == null) return;
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                doCitizenInventory(player);
                break;
            case INVENTORY_CHEST:
                doHutInventory(player, colony);
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
        if (citizen != null)
        {
            if (!StringUtils.isNullOrEmpty(name))
            {
                citizen.getInventoryCitizen().setCustomName(name);
            }

            NetworkHooks.openGui(player, citizen, packetBuffer -> packetBuffer.writeVarInt(citizen.getCitizenColonyHandler().getColonyId()).writeVarInt(citizen.getCitizenId()));
        }
    }

    private void doHutInventory(final ServerPlayerEntity player, final IColony colony)
    {
        @NotNull final TileEntityRack chest = (TileEntityRack) BlockPosUtil.getTileEntity(player.world, tePos);

        NetworkHooks.openGui(player, chest, packetBuffer -> packetBuffer.writeVarInt(colony.getID()).writeBlockPos(chest.getPos()));
    }

    private void doFieldInventory(final ServerPlayerEntity player)
    {
        @NotNull final ScarecrowTileEntity scarecrowTileEntity = (ScarecrowTileEntity) BlockPosUtil.getTileEntity(CompatibilityUtils.getWorldFromEntity(player), tePos);
        NetworkHooks.openGui(player, scarecrowTileEntity);
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
