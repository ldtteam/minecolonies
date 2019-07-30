package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to change the assignmentMode of the fields of the farmer.
 */
public class AssignmentModeMessage implements IMessage
{

    private int      colonyId;
    private BlockPos buildingId;
    private boolean  assignmentMode;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty standard constructor.
     */
    public AssignmentModeMessage()
    {
        super();
    }

    /**
     * Creates object for the assignmentMode message.
     *
     * @param building       View of the building to read data from.
     * @param assignmentMode assignmentMode of the particular farmer.
     */
    public AssignmentModeMessage(@NotNull final BuildingFarmer.View building, final boolean assignmentMode)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.assignmentMode = assignmentMode;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        assignmentMode = buf.readBoolean();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(assignmentMode);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final AssignmentModeMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingFarmer building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingFarmer.class);
            if (building != null)
            {
                building.setAssignManually(message.assignmentMode);
            }
        }
    }
}
