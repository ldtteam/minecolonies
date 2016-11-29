package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.BuildingGuardTower;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to change the behavior of the guards..
 */
public class GuardTaskMessage extends AbstractMessage<GuardTaskMessage, IMessage>
{
    private int      colonyId;
    private BlockPos buildingId;
    private int job;
    private boolean  assignmentMode;
    private boolean  patrollingMode;
    private boolean retrieval;
    private int task;

    /**
     * Empty standard constructor.
     */
    public GuardTaskMessage()
    {
        super();
    }

    /**
     * Creates object for the assignmentMode message.
     *
     * @param building       View of the building to read data from.
     * @param assignmentMode assignmentMode of the particular farmer.
     */
    public GuardTaskMessage(@NotNull BuildingGuardTower.View building, int job, boolean assignmentMode, boolean patrollingMode, boolean retrieval, int task )
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.job = job;
        this.assignmentMode = assignmentMode;
        this.patrollingMode = patrollingMode;
        this.retrieval = retrieval;
        this.task = task;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        job = buf.readInt();
        assignmentMode = buf.readBoolean();
        patrollingMode = buf.readBoolean();
        retrieval = buf.readBoolean();
        task = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(job);
        buf.writeBoolean(assignmentMode);
        buf.writeBoolean(patrollingMode);
        buf.writeBoolean(retrieval);
        buf.writeInt(task);
    }

    @Override
    public void messageOnServerThread(final GuardTaskMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Permissions.Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingGuardTower building = colony.getBuilding(message.buildingId, BuildingGuardTower.class);
            if (building != null)
            {
                if(message.job != -1)
                {
                    building.setJob(BuildingGuardTower.GuardJob.values()[message.job]);
                }
                building.setAssignManually(message.assignmentMode);
                building.setPatrolManually(message.patrollingMode);
                building.setRetrieveOnLowHealth(message.retrieval);
                building.setTask(BuildingGuardTower.Task.values()[message.task]);

                if(building.getTask().equals(BuildingGuardTower.Task.FOLLOW))
                {
                    building.setPlayerToFollow(player);
                }
            }
        }
    }
}
