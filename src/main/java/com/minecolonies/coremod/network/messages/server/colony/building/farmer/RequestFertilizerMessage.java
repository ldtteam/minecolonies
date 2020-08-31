package com.minecolonies.coremod.network.messages.server.colony.building.farmer;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to toggle the farming requesting fertilizer
 */
public class RequestFertilizerMessage extends AbstractBuildingServerMessage<BuildingFarmer>
{
    private boolean shouldRequestFertilizer;

    /**
     * Standard constructor
     */
    public RequestFertilizerMessage()
    {
        super();
    }

    /**
     * Creates object for request fertilizer
     *
     * @param building the building we're executing on
     * @param shouldRequestFertilizer the setting for whether to request fertilizer
     */
    public RequestFertilizerMessage(@NotNull final BuildingFarmer.View building, final boolean shouldRequestFertilizer)
    {
        super(building);
        this.shouldRequestFertilizer = shouldRequestFertilizer;
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony, BuildingFarmer building)
    {
        building.setRequestFertilizer(shouldRequestFertilizer);
    }

    @Override
    protected void toBytesOverride(PacketBuffer buf)
    {
        buf.writeBoolean(shouldRequestFertilizer);
    }

    @Override
    protected void fromBytesOverride(PacketBuffer buf)
    {
        shouldRequestFertilizer = buf.readBoolean();
    }
}
