package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeRegistry;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to change the behavior of the guards..
 */
public class GuardTaskMessage implements IMessage
{
    private int              colonyId;
    private BlockPos         buildingId;
    private ResourceLocation job;
    private boolean          assignmentMode;
    private boolean          patrollingMode;
    private boolean          retrieval;
    private int              task;

    /**
     * The dimension of the
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
     * Creates an instance of the guard task
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
      final ResourceLocation job,
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
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        job = buf.readResourceLocation();
        assignmentMode = buf.readBoolean();
        patrollingMode = buf.readBoolean();
        tightGrouping = buf.readBoolean();
        retrieval = buf.readBoolean();
        task = buf.readInt();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeResourceLocation(job);
        buf.writeBlockPos(buildingId);
        buf.writeBoolean(assignmentMode);
        buf.writeBoolean(patrollingMode);
        buf.writeBoolean(tightGrouping);
        buf.writeBoolean(retrieval);
        buf.writeInt(task);
        buf.writeInt(dimension);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null)
        {
            final PlayerEntity player = ctxIn.getSender();
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final AbstractBuildingGuards building = colony.getBuildingManager().getBuilding(buildingId, AbstractBuildingGuards.class);
            if (building != null)
            {
                building.setGuardType(IGuardTypeRegistry.getInstance().getValue(job));
                building.setAssignManually(assignmentMode);
                building.setPatrolManually(patrollingMode);
                building.setTightGrouping(tightGrouping);
                building.setRetrieveOnLowHealth(retrieval);
                building.setTask(GuardTask.values()[task]);

                if (building.getTask().equals(GuardTask.FOLLOW))
                {
                    building.setPlayerToFollow(player);
                }
            }
        }
    }
}
