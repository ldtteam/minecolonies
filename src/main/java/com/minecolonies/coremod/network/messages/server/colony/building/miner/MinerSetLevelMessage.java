package com.minecolonies.coremod.network.messages.server.colony.building.miner;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the level of the miner from the GUI.
 */
public class MinerSetLevelMessage extends AbstractBuildingServerMessage<BuildingMiner>
{
    private int level;

    /**
     * Empty constructor used when registering the
     */
    public MinerSetLevelMessage()
    {
        super();
    }

    /**
     * Creates object for the miner set level
     *
     * @param building View of the building to read data from.
     * @param level    Level of the miner.
     */
    public MinerSetLevelMessage(@NotNull final BuildingMiner.View building, final int level)
    {
        super(building);
        this.level = level;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        level = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeInt(level);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingMiner building)
    {
        building.setCurrentLevel(level);
    }
}
