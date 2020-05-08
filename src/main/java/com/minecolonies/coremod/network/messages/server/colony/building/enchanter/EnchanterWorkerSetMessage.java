package com.minecolonies.coremod.network.messages.server.colony.building.enchanter;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
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
    public EnchanterWorkerSetMessage(@NotNull final BuildingEnchanter.View building, final BlockPos worker, final boolean add)
    {
        super(building);
        this.worker = worker;
        this.add = add;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        worker = buf.readBlockPos();
        add = buf.readBoolean();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeBlockPos(worker);
        buf.writeBoolean(add);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingEnchanter building)
    {
        if (add)
        {
            building.addWorker(worker);
        }
        else
        {
            building.removeWorker(worker);
        }
    }
}
