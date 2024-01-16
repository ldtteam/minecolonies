package com.minecolonies.core.network.messages.server.colony.building.enchanter;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.colony.buildings.modules.EnchanterStationsModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingEnchanter;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to set add or remove a worker to gather from.
 */
public class EnchanterWorkerSetMessage extends AbstractBuildingServerMessage<BuildingEnchanter>
{
    /**
     * The worker to add/remove.
     */
    private BlockPos worker;

    /**
     * true if add, false if remove.
     */
    private boolean add;

    /**
     * Empty constructor used when registering the
     */
    public EnchanterWorkerSetMessage()
    {
        super();
    }

    /**
     * Create the enchanter worker
     *
     * @param building the building of the enchanter.
     * @param worker   the worker to add/remove.
     * @param add      true if add, else false
     */
    public EnchanterWorkerSetMessage(@NotNull final IBuildingView building, final BlockPos worker, final boolean add)
    {
        super(building);
        this.worker = worker;
        this.add = add;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        worker = buf.readBlockPos();
        add = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(worker);
        buf.writeBoolean(add);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingEnchanter building)
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
