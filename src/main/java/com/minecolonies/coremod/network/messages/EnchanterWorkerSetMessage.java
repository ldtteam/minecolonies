package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set add or remove a worker to gather from.
 */
public class EnchanterWorkerSetMessage extends AbstractMessage<EnchanterWorkerSetMessage, IMessage>
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The building id of the enchanter.
     */
    private BlockPos buildingId;

    /**
     * The dimension of the building.
     */
    private int dimension;

    /**
     * The worker to add/remove.
     */
    private BlockPos worker;

    /**
     * true if add, false if remove.
     */
    private boolean add;

    /**
     * Empty constructor used when registering the message.
     */
    public EnchanterWorkerSetMessage()
    {
        super();
    }

    /**
     * Create the enchanter worker message.
     * @param building the building of the enchanter.
     * @param worker the worker to add/remove.
     * @param add true if add, else false
     */
    public EnchanterWorkerSetMessage(@NotNull final BuildingEnchanter.View building, final BlockPos worker, final boolean add)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.worker = worker;
        this.dimension = building.getColony().getDimension();
        this.add = add;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        dimension = buf.readInt();
        worker = BlockPosUtil.readFromByteBuf(buf);
        add = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(dimension);
        BlockPosUtil.writeToByteBuf(buf, worker);
        buf.writeBoolean(add);
    }

    @Override
    public void messageOnServerThread(final EnchanterWorkerSetMessage message, final EntityPlayerMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {

            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingEnchanter building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingEnchanter.class);
            if (building != null)
            {
                if (message.add)
                {
                    building.addWorker(message.worker);
                }
                else
                {
                    building.removeWorker(message.worker);
                }
            }
        }
    }
}
