package com.minecolonies.core.network.messages.server.colony.building.guard;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the position of the mine a guard should patrol
 */
public class GuardSetMinePosMessage extends AbstractBuildingServerMessage<AbstractBuildingGuards>
{
    /**
     * the position of the mine (can be null)
     */
    private BlockPos minePos;
    /**
     * Indicates whether minePos is a valid position
     */
    private Boolean hasMinePos = false;

    /**
     * Empty standard constructor
     */
    public GuardSetMinePosMessage()
    {
        super();
    }

    /**
     * Creates an instance of the message to set a new position
     * @param building the building to apply the position change to
     * @param minePos the position of the mine
     */
    public GuardSetMinePosMessage(@NotNull AbstractBuildingGuards.View building, BlockPos minePos)
    {
        super(building);
        this.minePos = minePos;
        this.hasMinePos = true;
    }

    /**
     * Creates an instance of the message to clear the position
     * @param building the building to apply the position change to
     */
    public GuardSetMinePosMessage(@NotNull AbstractBuildingGuards.View building)
    {
        super(building);
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        this.hasMinePos = buf.readBoolean();
        if (this.hasMinePos)
        {
            this.minePos = buf.readBlockPos();
        }
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
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
        final IBuilding miner;
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
        }
    }
}
