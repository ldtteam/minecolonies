package com.minecolonies.core.network.messages.server.colony.building.miner;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to repair the level of the miner from the GUI.
 */
public class MinerRepairLevelMessage extends AbstractBuildingServerMessage<BuildingMiner>
{
    private int level;

    /**
     * Empty constructor used when registering the
     */
    public MinerRepairLevelMessage()
    {
        super();
    }

    /**
     * Creates object for the miner set level
     *
     * @param building View of the building to read data from.
     * @param level    Level of the miner.
     */
    public MinerRepairLevelMessage(@NotNull final IBuildingView building, final int level)
    {
        super(building);
        this.level = level;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        level = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(level);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingMiner building)
    {
        building.getModule(BuildingModules.MINER_LEVELS).repairLevel(level);
    }
}
