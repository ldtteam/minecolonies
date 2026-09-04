package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.minecolonies.core.event.DataPackSyncEventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Message to update the recipes on the client side.
 */
public class UpdateClientWithCompatibilityMessage extends AbstractPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forBothSides(Constants.MOD_ID, "update_client_with_compatibility", UpdateClientWithCompatibilityMessage::new, true, false);

    private final RegistryFriendlyByteBuf buffer;

    /**
     * Message creation.
     */
    public UpdateClientWithCompatibilityMessage(@NotNull RegistryAccess provider)
    {
        super(TYPE);
        this.buffer = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.buffer()), provider);
        IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager().serialize(this.buffer);
    }

    protected UpdateClientWithCompatibilityMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.buffer = new RegistryFriendlyByteBuf(new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray())), buf.registryAccess());
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeByteArray(this.buffer.array());
        this.buffer.resetWriterIndex();
    }

    @Override
    protected void onClientExecute(final IPayloadContext ctxIn, final Player player)
    {
        try
        {
            DataPackSyncEventHandler.ClientEvents.onCompatibilityMessage(this.buffer);
        }
        catch (Exception e)
        {
            Log.getLogger().error("Failed to load compatibility manager", e);
        }
    }

    @Override
    protected void onServerExecute(final IPayloadContext ctxIn, final ServerPlayer player)
    {
        throw new IllegalStateException("The compatibility update payload is clientbound only");
    }
}
