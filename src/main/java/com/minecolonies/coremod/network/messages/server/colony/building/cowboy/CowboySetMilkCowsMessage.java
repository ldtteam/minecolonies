package com.minecolonies.coremod.network.messages.server.colony.building.cowboy;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCowboy;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class CowboySetMilkCowsMessage extends AbstractBuildingServerMessage<BuildingCowboy>
{
    private boolean milkCows;

    /**
     * Empty standard constructor.
     */
    public CowboySetMilkCowsMessage()
    {
        super();
    }

    /**
     * Creates object for the CowboySetMilk
     *
     * @param building View of the building to read data from.
     * @param milkCows Whether Cowboy should milk cows.
     */
    public CowboySetMilkCowsMessage(@NotNull final BuildingCowboy.View building, final boolean milkCows)
    {
        super(building);
        this.milkCows = milkCows;
    }

    @Override
    public void fromBytesOverride(final PacketBuffer buf)
    {

        milkCows = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(final PacketBuffer buf)
    {

        buf.writeBoolean(milkCows);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingCowboy building)
    {
        building.setMilkCows(milkCows);
    }
}
