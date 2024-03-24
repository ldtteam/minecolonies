package com.minecolonies.core.network.messages.server.colony.building.enchanter;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.modules.EnchanterStationsModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingEnchanter;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set add or remove a worker to gather from.
 */
public class EnchanterWorkerSetMessage extends AbstractBuildingServerMessage<BuildingEnchanter>
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "enchanter_worker_set", EnchanterWorkerSetMessage::new);

    /**
     * The worker to add/remove.
     */
    private final BlockPos worker;

    /**
     * true if add, false if remove.
     */
    private final boolean add;

    /**
     * Create the enchanter worker
     *
     * @param building the building of the enchanter.
     * @param worker   the worker to add/remove.
     * @param add      true if add, else false
     */
    public EnchanterWorkerSetMessage(@NotNull final IBuildingView building, final BlockPos worker, final boolean add)
    {
        super(TYPE, building);
        this.worker = worker;
        this.add = add;
    }

    protected EnchanterWorkerSetMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        worker = buf.readBlockPos();
        add = buf.readBoolean();
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeBlockPos(worker);
        buf.writeBoolean(add);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony, final BuildingEnchanter building)
    {
        if (add)
        {
            building.getFirstModuleOccurance(EnchanterStationsModule.class).addWorker(worker);
        }
        else
        {
            building.getFirstModuleOccurance(EnchanterStationsModule.class).removeWorker(worker);
        }
    }
}
