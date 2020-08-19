package com.minecolonies.coremod.network.messages.server.colony.building.composter;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Class used for setting whether dirt or compost should be retrived from the bin.
 */
public class ComposterRetrievalMessage extends AbstractBuildingServerMessage<BuildingComposter>
{
    /**
     * Whether the composter should retrieve dirt or not.
     */
    private boolean retrieveDirt;

    /**
     * Empty standard constructor.
     */
    public ComposterRetrievalMessage()
    {
        super();
    }

    /**
     * Creates a message which will be sent to set the retrieval setting in the composter.
     *
     * @param building the building view of the composter
     * @param retrieve whether or not dirt should be retrieved.
     */
    public ComposterRetrievalMessage(final BuildingComposter.View building, final boolean retrieve)
    {
        super(building);
        this.retrieveDirt = retrieve;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        retrieveDirt = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeBoolean(retrieveDirt);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingComposter building)
    {
        building.setShouldRetrieveDirtFromCompostBin(retrieveDirt);
    }
}
