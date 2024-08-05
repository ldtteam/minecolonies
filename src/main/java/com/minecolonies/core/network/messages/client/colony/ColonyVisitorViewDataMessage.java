package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Sends visitor data to the client
 */
public class ColonyVisitorViewDataMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "colony_visitor_view_data", ColonyVisitorViewDataMessage::new);

    /**
     * The colony id
     */
    private final int colonyId;

    /**
     * The dimension the citizen is in.
     */
    private final ResourceKey<Level> dimension;

    /**
     * Visiting entity data
     */
    private Set<IVisitorData> visitors;

    /**
     * Visitor buf to read on client side.
     */
    private final RegistryFriendlyByteBuf visitorBuf;

    /**
     * If a general refresh is necessary,
     */
    private final boolean refresh;

    /**
     * Updates a {@link com.minecolonies.core.colony.CitizenDataView} of the citizens.
     *
     * @param colony Colony of the citizen
     */
    public ColonyVisitorViewDataMessage(@NotNull final IColony colony, @NotNull final Set<IVisitorData> visitors, final boolean refresh)
    {
        super(TYPE);
        this.colonyId = colony.getID();
        this.dimension = colony.getDimension();
        this.visitors = visitors;
        this.refresh = refresh;

        visitorBuf = new RegistryFriendlyByteBuf(Unpooled.buffer());
        visitorBuf.writeInt(visitors.size());
        for (final IVisitorData data : visitors)
        {
            visitorBuf.writeInt(data.getId());
            data.serializeViewNetworkData(visitorBuf);
        }
    }

    public ColonyVisitorViewDataMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        colonyId = buf.readInt();
        dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        refresh = buf.readBoolean();
        this.visitorBuf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray()));
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        visitorBuf.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeUtf(dimension.location().toString());
        buf.writeBoolean(refresh);
        buf.writeByteArray(visitorBuf.array());
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, dimension);

        if (colony == null)
        {
            Log.getLogger().warn("Received visitor data for nonexisting colony:" + colonyId + " dim:" + dimension);
        }
        else
        {
            colony.handleColonyViewVisitorMessage(visitorBuf, refresh);
        }
    }
}
