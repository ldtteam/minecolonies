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
 * Message which handles the assignment of fields to farmers.
 */
public class AssignFieldMessage implements IMessage, IMessageHandler<AssignFieldMessage, IMessage>
{

    private int      colonyId;
    private BlockPos buildingId;
    private boolean  assign;
    private BlockPos field;

    /**
     * Empty standard constructor.
     */
    public AssignFieldMessage()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Creates the message to assign a field.
     * @param building the farmer to assign to or release from.
     * @param assign assign if true, free if false.
     * @param field the field to assign or release.
     */
    public AssignFieldMessage(BuildingFarmer.View building, boolean assign, BlockPos field)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.assign = assign;
        this.field = field;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(assign);
        BlockPosUtil.writeToByteBuf(buf, field);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        assign = buf.readBoolean();
        field = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public IMessage onMessage(AssignFieldMessage message, MessageContext ctx)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            final BuildingFarmer building = colony.getBuilding(message.buildingId, BuildingFarmer.class);
            if (building != null)
            {
                if(message.assign)
                {
                    building.assignField(message.field);
                }
                else
                {
                    building.freeField(message.field);
                }
            }
        }
        return null;
    }
}

