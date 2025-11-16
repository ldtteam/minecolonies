package com.minecolonies.core.network.messages.server.colony.building.warehouse;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.inventory.api.CombinedItemHandler;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.core.util.SortingUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkEvent;

/**
 * Sort the specified building inventory if the building allows it.
 */
public class SortBuildingMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * Empty constructor used when registering the message
     */
    public SortBuildingMessage()
    {
        super();
    }

    public SortBuildingMessage(final IBuildingView building)
    {
        super(building);
    }

    @Override
    protected void toBytesOverride(final FriendlyByteBuf buf)
    {

    }

    @Override
    protected void fromBytesOverride(final FriendlyByteBuf buf)
    {

    }

    /**
     * Sort the building's inventory if it can be sorted.
     * 
     * @param ctxIn the context of the network event
     * @param isLogicalServer whether or not this is the logical server
     * @param colony the colony which the building is in
     * @param building the building to sort
     */
    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building.canSort())
        {
            building.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(inv -> building.sort((CombinedItemHandler) inv));
        }
    }
}
