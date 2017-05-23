package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ColonyManager;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a AbstractBuilding.View to a ColonyView on the client.
 */
public class ColonyViewBuildingViewMessage implements IMessage, IMessageHandler<ColonyViewBuildingViewMessage, IMessage>
{

    private transient IFactoryController factoryController;
    private           int                colonyId;
    private           IToken             buildingId;
    private           BlockPos           buildingLocation;
    private           ByteBuf            buildingData;

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
    public ColonyViewBuildingViewMessage(@NotNull final AbstractBuilding building)
    {
        this.factoryController = building.getColony().getFactoryController();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.buildingLocation = building.getLocation().getInDimensionLocation();
        this.buildingData = Unpooled.buffer();
        building.serializeToView(this.buildingData);
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        factoryController = ColonyManager.getColonyView(colonyId).getFactoryController();

        buildingId = factoryController.deserialize(ByteBufUtils.readTag(buf));
        buildingLocation = BlockPosUtil.readFromByteBuf(buf);
        buildingData = Unpooled.buffer(buf.readableBytes());
        buf.readBytes(buildingData, buf.readableBytes());
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeTag(buf, factoryController.serialize(buildingId));
        BlockPosUtil.writeToByteBuf(buf, buildingLocation);
        buf.writeBytes(buildingData);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull final ColonyViewBuildingViewMessage message, final MessageContext ctx)
    {
        return ColonyManager.handleColonyBuildingViewMessage(message.colonyId, message.buildingLocation, message.buildingId, message.buildingData);
    }
}
