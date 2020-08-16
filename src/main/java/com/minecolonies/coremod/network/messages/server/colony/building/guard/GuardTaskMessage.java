package com.minecolonies.coremod.network.messages.server.colony.building.guard;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeRegistry;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change the behavior of the guards..
 */
public class GuardTaskMessage extends AbstractBuildingServerMessage<AbstractBuildingGuards>
{
    private final ResourceLocation job;
    private final boolean          assignmentMode;
    private final boolean          patrollingMode;
    private final boolean          retrieval;
    private final int              task;

    /**
     * Indicates whether tight grouping is used in mode Follow.
     */
    private final boolean tightGrouping;

    /**
     * Empty standard constructor.
     */
    public GuardTaskMessage(final PacketBuffer buf)
    {
        super(buf);
        this.job = buf.readResourceLocation();
        this.assignmentMode = buf.readBoolean();
        this.patrollingMode = buf.readBoolean();
        this.tightGrouping = buf.readBoolean();
        this.retrieval = buf.readBoolean();
        this.task = buf.readInt();
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
     * @param tightGrouping  are we tight grouping.
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
        super(building);
        this.job = job;
        this.assignmentMode = assignmentMode;
        this.patrollingMode = patrollingMode;
        this.retrieval = retrieval;
        this.task = task;
        this.tightGrouping = tightGrouping;
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {
        buf.writeResourceLocation(job);
        buf.writeBoolean(assignmentMode);
        buf.writeBoolean(patrollingMode);
        buf.writeBoolean(tightGrouping);
        buf.writeBoolean(retrieval);
        buf.writeInt(task);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final AbstractBuildingGuards building)
    {
        building.setGuardType(IGuardTypeRegistry.getInstance().getValue(job));
        building.setAssignManually(assignmentMode);
        building.setPatrolManually(patrollingMode);
        building.setTightGrouping(tightGrouping);
        building.setRetrieveOnLowHealth(retrieval);
        building.setTask(GuardTask.values()[task]);

        if (building.getTask().equals(GuardTask.FOLLOW))
        {
            final PlayerEntity player = ctxIn.getSender();
            if (player == null)
            {
                return;
            }

            building.setPlayerToFollow(player);
        }
    }
}
