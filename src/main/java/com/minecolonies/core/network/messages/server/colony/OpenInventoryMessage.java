package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import com.minecolonies.core.tileentities.TileEntityGrave;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message sent to open an inventory.
 */
public class OpenInventoryMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "open_inventory", OpenInventoryMessage::new);

    /***
     * The inventory name.
     */
    private final String name;

    /**
     * The inventory type.
     */
    private final InventoryType inventoryType;

    /**
     * The entities id.
     */
    private int entityID;

    /**
     * The position of the inventory block/entity.
     */
    private BlockPos tePos;

    /**
     * Creates an open inventory message for the citizen.
     *
     * @param name   the name of the citizen.
     * @param id     its id.
     * @param colony the colony of the network message
     */
    public OpenInventoryMessage(final IColonyView colony, @NotNull final String name, final int id)
    {
        super(TYPE, colony);
        inventoryType = InventoryType.INVENTORY_CITIZEN;
        this.name = name;
        this.entityID = id;
    }

    /**
     * Creates an open inventory message for a building.
     *
     * @param building the building we're executing on.
     */
    public OpenInventoryMessage(final IBuildingView building)
    {
        super(TYPE, building.getColony());
        inventoryType = InventoryType.INVENTORY_CHEST;
        name = "";
        tePos = building.getID();
    }

    protected OpenInventoryMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);

        inventoryType = InventoryType.values()[buf.readInt()];
        name = buf.readUtf(32767);
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                entityID = buf.readInt();
                break;
            case INVENTORY_CHEST:
                tePos = buf.readBlockPos();
                break;
        }
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);

        buf.writeInt(inventoryType.ordinal());
        buf.writeUtf(name);
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                buf.writeInt(entityID);
                break;
            case INVENTORY_CHEST:
                buf.writeBlockPos(tePos);
                break;
        }
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        switch (inventoryType)
        {
            case INVENTORY_CITIZEN:
                doCitizenInventory(player);
                break;
            case INVENTORY_CHEST:
                doHutInventory(player, colony);
                break;
            default:
                break;
        }
    }

    private void doCitizenInventory(final ServerPlayer player)
    {
        @Nullable final AbstractEntityCitizen citizen = (AbstractEntityCitizen) CompatibilityUtils.getWorldFromEntity(player).getEntity(entityID);
        if (citizen != null)
        {
            if (!StringUtil.isNullOrEmpty(name))
            {
                citizen.getInventoryCitizen().setCustomName(name);
            }

            player.openMenu(citizen, packetBuffer -> packetBuffer.writeVarInt(citizen.getCitizenColonyHandler().getColonyId()).writeVarInt(citizen.getCivilianID()));
        }
    }

    private void doHutInventory(final ServerPlayer player, final IColony colony)
    {
        final BlockEntity tileEntity = BlockPosUtil.getTileEntity(player.level(), tePos);

        if(tileEntity instanceof TileEntityRack || tileEntity instanceof TileEntityGrave)
        {
            player.openMenu((MenuProvider) tileEntity, packetBuffer -> packetBuffer.writeVarInt(colony.getID()).writeBlockPos(tileEntity.getBlockPos()));
        }
    }

    /**
     * Type of inventory.
     */
    private enum InventoryType
    {
        INVENTORY_CITIZEN,
        INVENTORY_CHEST
    }
}
