package com.minecolonies.coremod.network.messages.server.colony.building.herder;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingHerder;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class HerderSetBreedingMessage extends AbstractBuildingServerMessage<AbstractBuildingHerder>
{
    private boolean breeding;

    /**
     * Empty standard constructor.
     */
    public HerderSetBreedingMessage()
    {
        super();
    }

    /**
     * Creates object for the HerderSetBreeding
     *
     * @param building View of the building to read data from.
     * @param breeding Whether the Herder should breed animals.
     */
    public HerderSetBreedingMessage(@NotNull final AbstractBuildingHerder.View building, final boolean breeding)
    {
        super(building);
        this.breeding = breeding;
    }

    @Override
    public void fromBytesOverride(final PacketBuffer buf)
    {
        breeding = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(final PacketBuffer buf)
    {
        buf.writeBoolean(breeding);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuildingHerder building)
    {
        building.setBreeding(breeding);
    }
}
