package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * The dimension the citizen is in.
     */
    private ResourceKey<Level> dimension;

    /**
     * Visiting entity data
     */
    private Set<IVisitorData> visitors;

    /**
     * Visitor buf to read on client side.
     */
    private FriendlyByteBuf visitorBuf;

    /**
     * If a general refresh is necessary,
     */
    private boolean refresh;

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

        visitorBuf = new FriendlyByteBuf(Unpooled.buffer());
        for (final IVisitorData data : visitors)
        {
            visitorBuf.writeInt(data.getId());
            data.serializeViewNetworkData(visitorBuf);
        }
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
        refresh = buf.readBoolean();
        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, dimension);

        if (colony == null)
        {
            Log.getLogger().warn("Received visitor data for nonexisting colony:" + colonyId + " dim:" + dimension);
            return;
        }

        this.visitorBuf = new FriendlyByteBuf(buf.retain());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        visitorBuf.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeBoolean(refresh);
        buf.writeInt(visitors.size());
        buf.writeBytes(visitorBuf);
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

        colony.handleColonyViewVisitorMessage(visitorBuf, refresh);
        visitorBuf.release();
    }
}
