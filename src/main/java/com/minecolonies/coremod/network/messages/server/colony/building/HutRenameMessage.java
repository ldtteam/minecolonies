package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to execute the renaiming of the townHall.
 */
public class HutRenameMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The custom name to set.
     */
    private String name;

    /**
     * Empty public constructor.
     */
    public HutRenameMessage()
    {
        super();
    }

    /**
     * Object creation for the town hall rename 
     *
     * @param name   New name of the town hall.
     */
    public HutRenameMessage(@NotNull final IBuildingView building, final String name)
    {
        super(building);
        this.name = name;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        name = buf.readString(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeString(name);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        building.setCustomBuildingName(name);
    }
}
