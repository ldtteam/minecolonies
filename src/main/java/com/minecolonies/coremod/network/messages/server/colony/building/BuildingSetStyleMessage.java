package com.minecolonies.coremod.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the style of a building.
 */
public class BuildingSetStyleMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The style to set.
     */
    private String structurePack;

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
     * @param structurePack    style of the building.
     */
    public BuildingSetStyleMessage(@NotNull final IBuildingView building, final String structurePack)
    {
        super(building);
        this.structurePack = structurePack;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        structurePack = buf.readUtf(32767);
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeUtf(structurePack);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building instanceof BuildingTownHall)
        {
            colony.setStructurePack(structurePack);
        }
        else
        {
            if (building.getBuildingLevel() > 0 && !building.isDeconstructed())
            {
                return;
            }

            building.setStructurePack(structurePack);
        }
    }
}
