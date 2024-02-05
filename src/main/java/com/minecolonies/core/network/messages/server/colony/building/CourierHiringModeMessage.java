package com.minecolonies.core.network.messages.server.colony.building;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.colony.buildings.modules.CourierAssignmentModule;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the hiring mode of a building.
 */
public class CourierHiringModeMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The module id
     */
    private int  id;

    /**
     * The Hiring mode to set.
     */
    private HiringMode mode;

    /**
     * Empty constructor used when registering the
     */
    public CourierHiringModeMessage()
    {
        super();
    }

    /**
     * Creates object for the hiring mode
     *
     * @param building View of the building to read data from.
     * @param mode     the hiring mode.
     */
    public CourierHiringModeMessage(@NotNull final IBuildingView building, final HiringMode mode, final int id)
    {
        super(building);
        this.mode = mode;
        this.id = id;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        mode = HiringMode.values()[buf.readInt()];
        id = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(mode.ordinal());
        buf.writeInt(id);
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        if (building.getModule(id) instanceof CourierAssignmentModule module)
        {
            module.setHiringMode(mode);
        }
    }
}
