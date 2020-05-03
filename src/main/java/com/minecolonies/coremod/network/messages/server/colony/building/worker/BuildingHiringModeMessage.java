package com.minecolonies.coremod.network.messages.server.colony.building.worker;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.IBuildingWorkerView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the hiring mode of a building.
 */
public class BuildingHiringModeMessage extends AbstractBuildingServerMessage<IBuildingWorker>
{
    /**
     * The Hiring mode to set.
     */
    private HiringMode mode;

    /**
     * Empty constructor used when registering the 
     */
    public BuildingHiringModeMessage()
    {
        super();
    }

    /**
     * Creates object for the hiring mode 
     *
     * @param building View of the building to read data from.
     * @param mode  the hiring mode.
     */
    public BuildingHiringModeMessage(@NotNull final IBuildingWorkerView building, final HiringMode mode)
    {
        super(building);
        this.mode = mode;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        mode = HiringMode.values()[buf.readInt()];
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeInt(mode.ordinal());
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuildingWorker building)
    {
        building.setHiringMode(mode);
    }
}
