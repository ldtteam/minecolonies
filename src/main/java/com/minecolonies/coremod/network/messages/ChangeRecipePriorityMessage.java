package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

/**
 * Message to change priorities of recipes.
 */
public class ChangeRecipePriorityMessage implements IMessage
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
     * The workOrder to remove or change priority.
     */
    private int recipeLocation;

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
    public ChangeRecipePriorityMessage()
    {
        super();
    }

    /**
     *  Creates message for player to change the priority of the recipes.
     *
     * @param building        view of the building to read data from
     * @param location     the recipeLocation.
     * @param up            up or down?
     */
    public ChangeRecipePriorityMessage(@NotNull final AbstractBuildingView building, final int location, final boolean up)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getPosition();
        this.recipeLocation = location;
        this.up = up;
        this.dimension = building.getColony().getDimension();
    }

    /**
     * Transformation from a byteStream to the variables.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.colonyId = buf.readInt();
        this.buildingId = buf.readBlockPos();
        this.recipeLocation = buf.readInt();
        this.up = buf.readBoolean();
        dimension = buf.readInt();
    }

    /**
     * Transformation to a byteStream.
     *
     * @param buf the used byteBuffer.
     */
    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(this.colonyId);
        buf.writeBlockPos(this.buildingId);
        buf.writeInt(this.recipeLocation);
        buf.writeBoolean(this.up);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final ChangeRecipePriorityMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }
            final IBuilding building = colony.getBuildingManager().getBuilding(message.buildingId);

            if(building instanceof AbstractBuildingWorker)
            {
                if (message.up)
                {
                    ((IBuildingWorker) building).switchIndex(message.recipeLocation, message.recipeLocation + 1);
                }
                else
                {
                    ((IBuildingWorker) building).switchIndex(message.recipeLocation, message.recipeLocation - 1);
                }
            }
        }
    }
}


