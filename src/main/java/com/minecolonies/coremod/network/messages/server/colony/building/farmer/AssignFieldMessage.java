package com.minecolonies.coremod.network.messages.server.colony.building.farmer;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message which handles the assignment of fields to farmers.
 */
public class AssignFieldMessage extends AbstractBuildingServerMessage<BuildingFarmer>
{
    private boolean  assign;
    private BlockPos field;

    /**
     * Empty standard constructor.
     */
    public AssignFieldMessage()
    {
        super();
    }

    /**
     * Creates the message to assign a field.
     *
     * @param assign   assign if true, free if false.
     * @param field    the field to assign or release.
     */
    public AssignFieldMessage(final IBuildingView building, final boolean assign, final BlockPos field)
    {
        super(building);
        this.assign = assign;
        this.field = field;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        assign = buf.readBoolean();
        field = buf.readBlockPos();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeBoolean(assign);
        buf.writeBlockPos(field);
    }

    @Override
    public void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final BuildingFarmer building)
    {
        if (assign)
        {
            building.assignField(field);
        }
        else
        {
            building.freeField(field);
        }
    }
}

