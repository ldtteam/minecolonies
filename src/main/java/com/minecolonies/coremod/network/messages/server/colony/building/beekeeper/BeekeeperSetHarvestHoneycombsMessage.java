package com.minecolonies.coremod.network.messages.server.colony.building.beekeeper;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Message to set the wether the beekeeper should harvest honeycombs.
 */
public class BeekeeperSetHarvestHoneycombsMessage extends AbstractBuildingServerMessage<BuildingBeekeeper>
{
    private final boolean harvestHoneycombs;

    /**
     * Empty standard constructor.
     */
    public BeekeeperSetHarvestHoneycombsMessage(final PacketBuffer buf)
    {
        super(buf);
        this.harvestHoneycombs = buf.readBoolean();
    }

    /**
     * Creates object for the BeekeeperSetHarvestHoneycomb
     *
     * @param building          View of the building to read data from.
     * @param harvestHoneycombs Whether Beekeeper should harvest honeycombs.
     */
    public BeekeeperSetHarvestHoneycombsMessage(final BuildingBeekeeper.View building, final boolean harvestHoneycombs)
    {
        super(building);
        this.harvestHoneycombs = harvestHoneycombs;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingBeekeeper building)
    {
        building.setHarvestHoneycombs(harvestHoneycombs);
    }

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {
        buf.writeBoolean(this.harvestHoneycombs);
    }
}
