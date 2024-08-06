package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.Colony;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
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
    private final RegistryFriendlyByteBuf colonyBuffer;

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
    public ColonyViewMessage(@NotNull final Colony colony, final RegistryFriendlyByteBuf buf)
    {
        super(TYPE);
        this.colonyId = colony.getID();
        this.dim = colony.getDimension();
        this.colonyBuffer = buf;
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

    protected ColonyViewMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        colonyId = buf.readInt();
        isNewSubscription = buf.readBoolean();
        dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)));
        colonyBuffer = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray())), buf.registryAccess());
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        colonyBuffer.resetReaderIndex();
        buf.writeInt(colonyId);
        buf.writeBoolean(isNewSubscription);
        buf.writeUtf(dim.location().toString());
        buf.writeByteArray(colonyBuffer.array());
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, @Nullable final Player player)
    {
        IColonyManager.getInstance().handleColonyViewMessage(colonyId, colonyBuffer, isNewSubscription, dim);
    }
}
