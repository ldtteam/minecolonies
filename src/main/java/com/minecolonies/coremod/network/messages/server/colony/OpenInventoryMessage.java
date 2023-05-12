package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
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
    private String name;

    /**
     * The inventory type.
     */
    private InventoryType inventoryType;

    /**
     * The entities id.
     */
    private int entityID;

    /**
     * The position of the inventory block/entity.
     */
    private BlockPos tePos;

    /**
     * Empty public constructor.
     */
    public OpenInventoryMessage()
    {
        super();
    }

    /**
     * Creates an open inventory message for the citizen.
     *
     * @param name   the name of the citizen.
     * @param id     its id.
     * @param colony the colony of the network message
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
     *
     * @param building the building we're executing on.
     */
    public OpenInventoryMessage(final IBuildingView building)
    {
        super(building.getColony());
        inventoryType = InventoryType.INVENTORY_CHEST;
        name = "";
        tePos = building.getID();
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        inventoryType = InventoryType.values()[buf.readInt()];
        name = buf.readUtf(32767);
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
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        buf.writeInt(inventoryType.ordinal());
        buf.writeUtf(name);
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
        final ServerPlayer player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }
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

    private void doCitizenInventory(final ServerPlayer player)
    {
        @Nullable final AbstractEntityCitizen citizen = (AbstractEntityCitizen) CompatibilityUtils.getWorldFromEntity(player).getEntity(entityID);
        if (citizen != null)
        {
            if (!StringUtil.isNullOrEmpty(name))
            {
                citizen.getInventoryCitizen().setCustomName(name);
            }

            NetworkHooks.openScreen(player, citizen, packetBuffer -> packetBuffer.writeVarInt(citizen.getCitizenColonyHandler().getColonyId()).writeVarInt(citizen.getCivilianID()));
        }
    }

    private void doHutInventory(final ServerPlayer player, final IColony colony)
    {
        final BlockEntity tileEntity = BlockPosUtil.getTileEntity(player.level, tePos);

        if(tileEntity instanceof TileEntityRack || tileEntity instanceof TileEntityGrave)
        {
            NetworkHooks.openScreen(player, (MenuProvider) tileEntity, packetBuffer -> packetBuffer.writeVarInt(colony.getID()).writeBlockPos(tileEntity.getBlockPos()));
        }
    }

    private void doFieldInventory(final ServerPlayer player)
    {
        @NotNull final ScarecrowTileEntity scarecrowTileEntity = (ScarecrowTileEntity) BlockPosUtil.getTileEntity(CompatibilityUtils.getWorldFromEntity(player), tePos);
        NetworkHooks.openScreen(player, scarecrowTileEntity);
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
