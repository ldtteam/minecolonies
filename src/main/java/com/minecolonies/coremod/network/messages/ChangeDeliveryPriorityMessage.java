package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

public class ChangeDeliveryPriorityMessage extends AbstractMessage<ChangeDeliveryPriorityMessage, IMessage>
{
    /**
     * The Colony ID.
     */
    private int colonyId;

    /**
     * Id of the building.
     */
    private BlockPos buildingId;

    /**
     * If up true, if down false.
     */
    private boolean up;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public ChangeDeliveryPriorityMessage()
    {
        super();
    }

    /**
     * Creates message for player to change the priority of the delivery.
     *
     * @param building view of the building to read data from
     * @param up       up or down?
     */
    public ChangeDeliveryPriorityMessage(@NotNull final AbstractBuildingView building, final boolean up)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getLocation();
        this.up = up;
        this.dimension = building.getColony().getDimension();
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.colonyId = buf.readInt();
        this.buildingId = BlockPosUtil.readFromByteBuf(buf);
        this.up = buf.readBoolean();
        dimension = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(this.colonyId);
        BlockPosUtil.writeToByteBuf(buf, this.buildingId);
        buf.writeBoolean(this.up);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final ChangeDeliveryPriorityMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }
            final AbstractBuilding building = colony.getBuildingManager().getBuilding(message.buildingId);

            if (building instanceof AbstractBuildingWorker)
            {
                if (message.up)
                {
                    building.alterPickUpPriority(1);
                    building.markDirty();
                }
                else
                {
                    building.alterPickUpPriority(-1);
                    building.markDirty();
                }
            }
        }
    }
}
