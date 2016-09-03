package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.BuildingFarmer;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Message to change the assignmentMode of the fields of the farmer.
 */
public class AssignmentModeMessage implements IMessage, IMessageHandler<AssignmentModeMessage, IMessage>
{

    private int      colonyId;
    private BlockPos buildingId;
    private boolean  assignmentMode;

    /**
     * Empty standard constructor.
     */
    public AssignmentModeMessage()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Creates object for the miner set level message
     *
     * @param building View of the building to read data from
     * @param assignmentMode assignmentMode of the particular farmer.
     */
    public AssignmentModeMessage(BuildingFarmer.View building, boolean assignmentMode)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.assignmentMode = assignmentMode;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(assignmentMode);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        assignmentMode = buf.readBoolean();
    }

    @Override
    public IMessage onMessage(AssignmentModeMessage message, MessageContext ctx)
    {
        Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            BuildingFarmer building = colony.getBuilding(message.buildingId, BuildingFarmer.class);
            if (building != null)
            {
                building.setAssignManually(message.assignmentMode);
            }
        }
        return null;
    }
}
