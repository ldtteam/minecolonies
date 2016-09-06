package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewRemoveBuildingMessage implements IMessage, IMessageHandler<ColonyViewRemoveBuildingMessage, IMessage>
{
    private int      colonyId;
    private BlockPos buildingId;

    public ColonyViewRemoveBuildingMessage() {}

    /**
     * Creates an object for the building remove message
     *
     * @param colony   Colony the building is in
     * @param building AbstractBuilding that is removed
     */
    public ColonyViewRemoveBuildingMessage(Colony colony, BlockPos building)
    {
        this.colonyId = colony.getID();
        this.buildingId = building;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
    }

    @Override
    public IMessage onMessage(ColonyViewRemoveBuildingMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyViewRemoveBuildingMessage(message.colonyId, message.buildingId);
    }
}
