package com.minecolonies.network.messages;

import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Add or Update a AbstractBuilding.View to a ColonyView on the client
 */
public class ColonyViewBuildingViewMessage implements IMessage, IMessageHandler<ColonyViewBuildingViewMessage, IMessage>
{
    private int              colonyId;
    private BlockPos         buildingId;
    private ByteBuf          buildingData;

    public ColonyViewBuildingViewMessage(){}

    /**
     * Creates a
     * @param building      AbstractBuilding to add or update a view for
     */
    public ColonyViewBuildingViewMessage(AbstractBuilding building)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.buildingData = Unpooled.buffer();
        building.serializeToView(this.buildingData);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBytes(buildingData);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        buildingData = Unpooled.buffer(buf.readableBytes());
        buf.readBytes(buildingData, buf.readableBytes());
    }

    @Override
    public IMessage onMessage(ColonyViewBuildingViewMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyBuildingViewMessage(message.colonyId, message.buildingId, message.buildingData);
    }
}
