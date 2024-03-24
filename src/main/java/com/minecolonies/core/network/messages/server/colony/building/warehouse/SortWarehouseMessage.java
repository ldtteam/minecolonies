package com.minecolonies.core.network.messages.server.colony.building.warehouse;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.inventory.api.CombinedItemHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.core.util.SortingUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Sort the warehouse if level bigger than 3.
 */
public class SortWarehouseMessage extends AbstractBuildingServerMessage<BuildingWareHouse>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "sort_warehouse_message", SortWarehouseMessage::new);

    /**
     * The required level to sort a warehouse.
     */
    private static final int REQUIRED_LEVEL_TO_SORT_WAREHOUSE = 3;

    public SortWarehouseMessage(final IBuildingView building)
    {
        super(TYPE, building);
    }

    protected SortWarehouseMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final BuildingWareHouse building)
    {
        if (building.getBuildingLevel() >= REQUIRED_LEVEL_TO_SORT_WAREHOUSE)
        {
            if (building.getItemHandlerCap() instanceof final CombinedItemHandler combinedInv)
            {
                SortingUtils.sort(combinedInv);
            }
        }
    }
}
