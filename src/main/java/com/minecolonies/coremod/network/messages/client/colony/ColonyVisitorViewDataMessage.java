package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.VisitorDataView;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Sends visitor data to the client
 */
public class ColonyVisitorViewDataMessage implements IMessage
{
    /**
     * The colony id
     */
    private final int colonyId;

    /**
     * Whether to refresh data clientside
     */
    private final boolean refresh;

    /**
     * The dimension the citizen is in.
     */
    private final ResourceLocation dimension;

    /**
     * Visiting entity data
     */
    private final Set<IVisitorData> visitors;

    /**
     * Visiting entity views
     */
    private final Set<IVisitorViewData> visitorViews;

    /**
     * Empty constructor used when registering the
     */
    public ColonyVisitorViewDataMessage(final PacketBuffer buf)
    {
        this.colonyId = buf.readInt();
        this.dimension = new ResourceLocation(buf.readString(32767));
        this.refresh = buf.readBoolean();
        this.visitors = null;

        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, dimension);

        if (colony == null)
        {
            this.visitorViews = null;
            return;
        }


        int visitorsSize = buf.readInt();
        this.visitorViews = new HashSet<>(visitorsSize);
        for (int j = 0; j < visitorsSize; j++)
        {
            final int id = buf.readInt();
            final IVisitorViewData dataView = new VisitorDataView(id, colony);
            dataView.deserialize(buf);
            visitorViews.add(dataView);
        }
    }

    /**
     * Updates a {@link com.minecolonies.coremod.colony.CitizenDataView} of the citizens.
     *
     * @param colony Colony of the citizen
     */
    public ColonyVisitorViewDataMessage(@NotNull final IColony colony, @NotNull final Set<IVisitorData> visitors, final boolean refresh)
    {
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
        this.visitors = visitors;
        this.refresh = refresh;
        this.visitorViews = null;
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeString(dimension.toString());
        buf.writeBoolean(refresh);
        buf.writeInt(visitors.size());

        for (final IVisitorData data : visitors)
        {
            buf.writeInt(data.getId());
            data.serializeViewNetworkData(buf);
        }
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
        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, dimension);

        if (colony == null)
        {
            Log.getLogger().warn("Received visitor data for nonexisting colony:" + colonyId + " dim:" + dimension);
            return;
        }

        colony.handleColonyViewVisitorMessage(refresh, visitorViews);
    }
}
