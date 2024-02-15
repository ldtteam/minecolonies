package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.Colony;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Add or Update a ColonyView on the client.
 */
public class ColonyViewMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "colony_view", ColonyViewMessage::new, true, false);

    /**
     * The colony id.
     */
    private final int colonyId;

    /**
     * If this is a new subscription.
     */
    private boolean isNewSubscription;

    /**
     * The buffer with the data.
     */
    private final FriendlyByteBuf colonyBuffer;

    /**
     * The dimension of the colony.
     */
    private final ResourceKey<Level> dim;

    /**
     * Add or Update a ColonyView on the client.
     *
     * @param colony Colony of the view to update.
     * @param buf    the bytebuffer.
     */
    public ColonyViewMessage(@NotNull final Colony colony, final FriendlyByteBuf buf)
    {
        super(TYPE);
        this.colonyId = colony.getID();
        this.dim = colony.getDimension();
        this.colonyBuffer = new FriendlyByteBuf(buf.copy());
    }

    /**
     * Set whether the message is a new subscription(full view)
     *
     * @param newSubscription
     */
    public ColonyViewMessage setIsNewSubscription(final boolean newSubscription)
    {
        isNewSubscription = newSubscription;
        return this;
    }

    protected ColonyViewMessage(@NotNull final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        final FriendlyByteBuf newBuf = new FriendlyByteBuf(buf.retain());
        colonyId = newBuf.readInt();
        isNewSubscription = newBuf.readBoolean();
        dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(newBuf.readUtf(32767)));
        colonyBuffer = newBuf;
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        colonyBuffer.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeBoolean(isNewSubscription);
        buf.writeUtf(dim.location().toString());
        buf.writeBytes(colonyBuffer);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, @Nullable final Player player)
    {
        IColonyManager.getInstance().handleColonyViewMessage(colonyId, colonyBuffer, isNewSubscription, dim);
        colonyBuffer.release();
    }
}
