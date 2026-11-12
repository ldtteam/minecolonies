package com.minecolonies.core.network.messages.server.colony.building.stable;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.StablePatrolTargetsModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingStable;
import com.minecolonies.core.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Requests a change to a Stable's selected automatic patrol targets.
 */
public class StablePatrolTargetMessage extends AbstractBuildingServerMessage<BuildingStable>
{
    /**
     * Target building position.
     */
    private BlockPos target;

    /**
     * Desired selected state.
     */
    private boolean selected;

    /**
     * Constructs an empty message for network deserialization.
     */
    public StablePatrolTargetMessage()
    {
        super();
    }

    /**
     * Constructs a patrol target selection message.
     *
     * @param building the Stable view being configured.
     * @param target the target building position.
     * @param selected whether the target should be selected.
     */
    public StablePatrolTargetMessage(@NotNull final IBuildingView building, final BlockPos target, final boolean selected)
    {
        super(building);
        this.target = target;
        this.selected = selected;
    }

    /**
     * Reads the requested target state from the network buffer.
     *
     * @param buf the source buffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        target = buf.readBlockPos();
        selected = buf.readBoolean();
    }

    /**
     * Writes the requested target state to the network buffer.
     *
     * @param buf the destination buffer.
     */
    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(target);
        buf.writeBoolean(selected);
    }

    /**
     * Applies the validated selection request to the Stable module.
     *
     * @param ctxIn the network context.
     * @param isLogicalServer whether execution is on the logical server.
     * @param colony the target colony.
     * @param building the Stable being configured.
     */
    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingStable building)
    {
        final StablePatrolTargetsModule module = building.getModule(BuildingModules.STABLE_PATROL_TARGETS);
        final boolean changed;
        
        if (selected)
        {
            changed = module.addPatrolTarget(target);
        }
        else
        {
            changed = module.removePatrolTarget(target);
        }

        if (!changed)
        {
            building.markDirty();
        }
    }
}
