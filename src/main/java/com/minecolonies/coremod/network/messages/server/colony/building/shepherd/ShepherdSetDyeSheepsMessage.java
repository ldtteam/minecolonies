package com.minecolonies.coremod.network.messages.server.colony.building.shepherd;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingShepherd;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Transfer the current state of automatical sheep dyeing (true = enabled)
 */
public class ShepherdSetDyeSheepsMessage extends AbstractBuildingServerMessage<BuildingShepherd>
{
    private boolean  dyeSheeps;

    /**
     * Empty standard constructor.
     */
    public ShepherdSetDyeSheepsMessage()
    {
        super();
    }

    /**
     * Creates object for the CowboySetMilk 
     *
     * @param building View of the building to read data from.
     */
    public ShepherdSetDyeSheepsMessage(@NotNull final BuildingShepherd.View building)
    {
        super(building);
        this.dyeSheeps = building.isDyeSheeps();
    }

    @Override
    public void fromBytesOverride(final PacketBuffer buf)
    {

        dyeSheeps = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(final PacketBuffer buf)
    {

        buf.writeBoolean(dyeSheeps);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingShepherd building)
    {
        building.setDyeSheeps(dyeSheeps);
    }
}
