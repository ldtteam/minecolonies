package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.inventory.api.CombinedItemHandler;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.util.SortingUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Sort the warehouse if level bigger than 3.
 */
public class SortWarehouseMessage implements IMessage
{
    /**
     * The required level to sort a warehouse.
     */
    private static final int REQUIRED_LEVEL_TO_SORT_WAREHOUSE = 3;

    /**
     * The id of the building.
     */
    private BlockPos buildingId;

    /**
     * The id of the colony.
     */
    private int colonyId;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty constructor used when registering the message.
     */
    public SortWarehouseMessage()
    {
        super();
    }

    /**
     * Creates a Sort Warehouse message.
     *
     * @param building AbstractBuilding of the request.
     */
    public SortWarehouseMessage(@NotNull final AbstractBuildingView building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final SortWarehouseMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony == null)
        {
            Log.getLogger().warn("UpgradeWarehouseMessage colony is null");
            return;
        }

        if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            return;
        }

        final IBuilding building = colony.getBuildingManager().getBuilding(message.buildingId);
        if (!(building instanceof BuildingWareHouse))
        {
            Log.getLogger().warn("UpgradeWarehouseMessage building is not a Warehouse");
            return;
        }

        if (building.getBuildingLevel() >= REQUIRED_LEVEL_TO_SORT_WAREHOUSE)
        {
            final CombinedItemHandler inv = (CombinedItemHandler) building.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            SortingUtils.sort(inv);
        }
    }
}
