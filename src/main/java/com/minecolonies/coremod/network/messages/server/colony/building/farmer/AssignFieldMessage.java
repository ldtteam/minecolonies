package com.minecolonies.coremod.network.messages.server.colony.building.farmer;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.colony.buildings.modules.FarmerFieldModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
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
     * @param building the building we're executing on.
     */
    public AssignFieldMessage(final IBuildingView building, final boolean assign, final BlockPos field)
    {
        super(building);
        this.assign = assign;
        this.field = field;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        assign = buf.readBoolean();
        field = buf.readBlockPos();
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
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
            building.getFirstOptionalModuleOccurance(FarmerFieldModule.class).ifPresent(m -> m.assignField(field));
        }
        else
        {
            building.getFirstOptionalModuleOccurance(FarmerFieldModule.class).ifPresent(m -> m.freeField(field));
        }
    }
}

