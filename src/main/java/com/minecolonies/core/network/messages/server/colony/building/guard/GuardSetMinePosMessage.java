package com.minecolonies.core.network.messages.server.colony.building.guard;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set the position of the mine a guard should patrol
 */
public class GuardSetMinePosMessage extends AbstractBuildingServerMessage<AbstractBuildingGuards>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "guard_set_mine_pos", GuardSetMinePosMessage::new);

    /**
     * the position of the mine (can be null)
     */
    private BlockPos minePos;

    /**
     * Creates an instance of the message to set a new position
     * @param building the building to apply the position change to
     * @param minePos the position of the mine
     */
    public GuardSetMinePosMessage(@NotNull final AbstractBuildingGuards.View building, final BlockPos minePos)
    {
        super(TYPE, building);
        this.minePos = minePos;
    }

    /**
     * Creates an instance of the message to clear the position
     * @param building the building to apply the position change to
     */
    public GuardSetMinePosMessage(@NotNull final AbstractBuildingGuards.View building)
    {
        super(TYPE, building);
        this.minePos = null;
    }

    protected GuardSetMinePosMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.minePos = buf.readBoolean() ? buf.readBlockPos() : null;
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeBoolean(this.minePos != null);
        if (this.minePos != null)
        {
            buf.writeBlockPos(this.minePos);
        }
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final AbstractBuildingGuards building)
    {
        final IBuilding miner = building.getColony().getBuildingManager().getBuilding(minePos == null ? building.getMinePos() : minePos);
        if (miner instanceof BuildingMiner)
        {
            building.setMinePos(this.minePos);
        }
    }
}
