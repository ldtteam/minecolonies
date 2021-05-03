package com.minecolonies.coremod.network.messages.server.colony.building.beekeeper;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBeekeeper;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Message to set the whether the beekeeper should harvest honeycombs.
 */
public class BeekeeperSetHarvestHoneycombsMessage extends AbstractBuildingServerMessage<BuildingBeekeeper>
{
    public enum HarvestType
    {
        HONEYCOMBS,
        HONEY_BOTTLES,
        BOTH
    }

    private HarvestType harvestHoneycombs;

    /**
     * Empty standard constructor.
     */
    public BeekeeperSetHarvestHoneycombsMessage()
    {
        super();
    }

    /**
     * Creates object for the BeekeeperSetHarvestHoneycomb
     *
     * @param building          View of the building to read data from.
     * @param harvestHoneycombs Whether Beekeeper should harvest honeycombs.
     */
    public BeekeeperSetHarvestHoneycombsMessage(final BuildingBeekeeper.View building, final HarvestType harvestHoneycombs)
    {
        super(building);
        this.harvestHoneycombs = harvestHoneycombs;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingBeekeeper building)
    {
        building.setHarvestHoneycombs(harvestHoneycombs);
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {
        buf.writeEnumValue(this.harvestHoneycombs);
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {
        this.harvestHoneycombs = buf.readEnumValue(HarvestType.class);
    }
}
