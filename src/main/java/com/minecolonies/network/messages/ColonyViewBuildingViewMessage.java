package com.minecolonies.network.messages;

import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a AbstractBuilding.View to a ColonyView on the client
 */
public class ColonyViewBuildingViewMessage implements IMessage, IMessageHandler<ColonyViewBuildingViewMessage, IMessage>
{
    private int      colonyId;
    private BlockPos buildingId;
    private ByteBuf  buildingData;

    /**
     * Empty constructor used when registering the message.
     */
    public ColonyViewBuildingViewMessage()
    {
        super();
    }

    /**
     * Creates a
     *
     * @param building AbstractBuilding to add or update a view for
     */
    public ColonyViewBuildingViewMessage(@NotNull AbstractBuilding building)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.buildingData = Unpooled.buffer();
        building.serializeToView(this.buildingData);
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        buildingData = Unpooled.buffer(buf.readableBytes());
        buf.readBytes(buildingData, buf.readableBytes());
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBytes(buildingData);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull ColonyViewBuildingViewMessage message, MessageContext ctx)
    {
        return ColonyManager.handleColonyBuildingViewMessage(message.colonyId, message.buildingId, message.buildingData);
    }
}
