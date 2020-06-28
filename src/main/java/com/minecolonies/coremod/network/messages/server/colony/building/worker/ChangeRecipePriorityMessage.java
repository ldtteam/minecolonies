package com.minecolonies.coremod.network.messages.server.colony.building.worker;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Message to change priorities of recipes.
 */
public class ChangeRecipePriorityMessage extends AbstractBuildingServerMessage<IBuildingWorker>
{
    /**
     * The workOrder to remove or change priority.
     */
    private int recipeLocation;

    /**
     * If up true, if down false.
     */
    private boolean up;

    /**
     * Empty public constructor.
     */
    public ChangeRecipePriorityMessage()
    {
        super();
    }

    /**
     * Creates message for player to change the priority of the recipes.
     *
     * @param building view of the building to read data from
     * @param location the recipeLocation.
     * @param up       up or down?
     */
    public ChangeRecipePriorityMessage(@NotNull final IBuildingView building, final int location, final boolean up)
    {
        super(building);
        this.recipeLocation = location;
        this.up = up;
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        this.recipeLocation = buf.readInt();
        this.up = buf.readBoolean();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeInt(this.recipeLocation);
        buf.writeBoolean(this.up);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuildingWorker building)
    {
        if (up)
        {
            building.switchIndex(recipeLocation, recipeLocation + 1);
        }
        else
        {
            building.switchIndex(recipeLocation, recipeLocation - 1);
        }
    }
}


