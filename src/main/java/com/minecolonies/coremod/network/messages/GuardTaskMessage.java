package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
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
    private int      job;
    private boolean  assignmentMode;
    private boolean  patrollingMode;
    private boolean  retrieval;
    private int      task;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Indicates whether tight grouping is used in mode Follow.
     */
    private boolean  tightGrouping;

    /**
     * Empty standard constructor.
     */
    public GuardTaskMessage()
    {
        super();
    }

    /**
     * Creates an instance of the guard task message.
     *
     * @param building       the building.
     * @param job            the new job.
     * @param assignmentMode the new assignment mode.
     * @param patrollingMode the new patrolling mode.
     * @param retrieval      the new retrievel mode.
     * @param task           the new task.
     */
    public GuardTaskMessage(
                             @NotNull final AbstractBuildingGuards.View building,
                             final int job,
                             final boolean assignmentMode,
                             final boolean patrollingMode,
                             final boolean retrieval,
                             final int task,
                             final boolean tightGrouping
    )
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.job = job;
        this.assignmentMode = assignmentMode;
        this.patrollingMode = patrollingMode;
        this.retrieval = retrieval;
        this.task = task;
        this.tightGrouping = tightGrouping;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        job = buf.readInt();
        assignmentMode = buf.readBoolean();
        patrollingMode = buf.readBoolean();
        tightGrouping = buf.readBoolean();
        retrieval = buf.readBoolean();
        task = buf.readInt();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(job);
        buf.writeBoolean(assignmentMode);
        buf.writeBoolean(patrollingMode);
        buf.writeBoolean(tightGrouping);
        buf.writeBoolean(retrieval);
        buf.writeInt(task);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final GuardTaskMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final AbstractBuildingGuards building = colony.getBuildingManager().getBuilding(message.buildingId, AbstractBuildingGuards.class);
            if (building != null)
            {
                if (message.job != -1)
                {
                    building.setJob(AbstractBuildingGuards.GuardJob.values()[message.job]);
                }
                building.setAssignManually(message.assignmentMode);
                building.setPatrolManually(message.patrollingMode);
                building.setTightGrouping(message.tightGrouping);
                building.setRetrieveOnLowHealth(message.retrieval);
                building.setTask(GuardTask.values()[message.task]);

                if (building.getTask().equals(GuardTask.FOLLOW))
                {
                    building.setPlayerToFollow(player);
                }
            }
        }
    }
}
