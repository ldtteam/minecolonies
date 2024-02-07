package com.minecolonies.core.network.messages.server.colony.building.builder;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public class BuilderSelectWorkOrderMessage extends AbstractBuildingServerMessage<BuildingBuilder>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "builder_select_work_order", BuilderSelectWorkOrderMessage::new);

    private final int workOrder;

    /**
     * Creates a new BuilderSetManualModeMessage.
     *
     * @param building View of the building to read data from.
     * @param workOrder workorder id.
     */
    public BuilderSelectWorkOrderMessage(@NotNull final IBuildingView building, final int workOrder)
    {
        super(TYPE, building);
        this.workOrder = workOrder;
    }

    protected BuilderSelectWorkOrderMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        workOrder = buf.readInt();
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(workOrder);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final BuildingBuilder building)
    {
        building.setWorkOrder(workOrder);
    }
}
