package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.VisitorDataView;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
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
    private int colonyId;

    /**
     * Whether to refresh data clientside
     */
    private boolean refresh = false;

    /**
     * The dimension the citizen is in.
     */
    private RegistryKey<World> dimension;

    /**
     * Visiting entity data
     */
    private Set<IVisitorData> visitors;

    /**
     * Visiting entity views
     */
    private Set<IVisitorViewData> visitorViews = new HashSet<>();

    /**
     * Empty constructor used when registering the
     */
    public ColonyVisitorViewDataMessage()
    {
        super();
    }

    /**
     * Updates a {@link com.minecolonies.coremod.colony.CitizenDataView} of the citizens.
     *
     * @param colony Colony of the citizen
     */
    public ColonyVisitorViewDataMessage(@NotNull final IColony colony, @NotNull final Set<IVisitorData> visitors, final boolean refresh)
    {
        super();
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
        this.visitors = visitors;
        this.refresh = refresh;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
        refresh = buf.readBoolean();

        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, dimension);

        if (colony == null)
        {
            Log.getLogger().warn("Received visitor data for nonexisting colony:" + colonyId + " dim:" + dimension);
            return;
        }

        visitors = new HashSet<>();
        int i = buf.readInt();
        for (int j = 0; j < i; j++)
        {
            final int id = buf.readInt();
            final IVisitorViewData dataView = new VisitorDataView(id, colony);
            dataView.deserialize(buf);
            visitorViews.add(dataView);
        }
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
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
