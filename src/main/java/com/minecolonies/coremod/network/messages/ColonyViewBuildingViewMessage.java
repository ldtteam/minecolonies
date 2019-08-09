package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.IColonyManager;
import net.minecraft.network.PacketBuffer;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a AbstractBuilding.View to a ColonyView on the client.
 */
public class ColonyViewBuildingViewMessage implements IMessage
{
    private int      colonyId;
    private BlockPos buildingId;
    private PacketBuffer  buildingData;

    /**
     * Dimension of the colony.
     */
    private int dimension;

    /**
     * Empty constructor used when registering the
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
        super();
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
        dimension = buf.readInt();
        dimension = buf.readInt();
        buildingData = Unpooled.buffer(buf.readableBytes());
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

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        IColonyManager.getInstance().handleColonyBuildingViewMessage(colonyId, buildingId, buildingData, dimension);
    }
}
