package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

/**
 * Add or Update a AbstractBuilding.View to a ColonyView on the client.
 */
public class ColonyViewBuildingViewMessage implements IMessage
{
    private int      colonyId;
    private BlockPos buildingId;
    private ByteBuf  buildingData;

    /**
     * Dimension of the colony.
     */
    private int dimension;

    /**
     * Empty constructor used when registering the message.
     */
    public ColonyViewBuildingViewMessage()
    {
        super();
    }

    /**
     * Creates a message to handle colony views.
     *
     * @param building AbstractBuilding to add or update a view.
     */
    public ColonyViewBuildingViewMessage(@NotNull final IBuilding building)
    {
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.buildingData = Unpooled.buffer();
        building.serializeToView(this.buildingData);
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        buildingData = Unpooled.buffer(buf.readableBytes());
        dimension = buf.readInt();
        buf.readBytes(buildingData, buf.readableBytes());
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(dimension);
        buf.writeBytes(buildingData);
    }

    @Override
    protected void messageOnClientThread(final ColonyViewBuildingViewMessage message, final MessageContext ctx)
    {
        IColonyManager.getInstance().handleColonyBuildingViewMessage(message.colonyId, message.buildingId, message.buildingData, message.dimension);
    }
}
