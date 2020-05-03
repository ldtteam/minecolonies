package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the style of a building.
 */
public class BuildingSetStyleMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The style to set.
     */
    private String style;

    /**
     * Empty constructor used when registering the 
     */
    public BuildingSetStyleMessage()
    {
        super();
    }

    /**
     * Creates object for the style of a building.
     *
     * @param building View of the building to read data from.
     * @param style    style of the building.
     */
    public BuildingSetStyleMessage(@NotNull final IBuildingView building, final String style)
    {
        super(building);
        this.style = style;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        style = buf.readString(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeString(style);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        building.setStyle(style);
        if(building.getTileEntity() != null)
        {
            building.getTileEntity().setStyle(style);
            if(building.getBuildingLevel() > 0)
            {
                building.onUpgradeComplete(building.getBuildingLevel());
            }
        }
    }
}
