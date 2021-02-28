package com.minecolonies.coremod.network.messages.server.colony.building.guard;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

public class GuardSetMinePosMessage extends AbstractBuildingServerMessage<AbstractBuildingGuards>
{
    private BlockPos minePos;
    private Boolean hasMinePos = false;

    public GuardSetMinePosMessage()
    {
        super();
    }

    public GuardSetMinePosMessage(@NotNull AbstractBuildingGuards.View building, BlockPos minePos)
    {
        super(building);
        this.minePos = minePos;
        this.hasMinePos = true;
    }

    public GuardSetMinePosMessage(@NotNull AbstractBuildingGuards.View building)
    {
        super(building);
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {
        this.hasMinePos = buf.readBoolean();
        if (this.hasMinePos)
        {
            this.minePos = buf.readBlockPos();
        }
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeBoolean(this.hasMinePos);
        if (this.hasMinePos)
        {
            buf.writeBlockPos(this.minePos);
        }
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuildingGuards building)
    {
        IBuilding miner;
        if (this.minePos == null)
        {
            miner = building.getColony().getBuildingManager().getBuilding(building.getMinePos());
        }
        else
        {
            miner = building.getColony().getBuildingManager().getBuilding(this.minePos);
        }
        if (miner instanceof BuildingMiner)
        {
            building.setMinePos(this.minePos);
            ((BuildingMiner) miner).pullGuards();
        }
    }
}
