package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.util.FurnaceRecipes;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to update the recipes on the client side.
 */
public class UpdateClientWithCompatibilityMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "update_client_with_compatibility", UpdateClientWithCompatibilityMessage::new);

    private final FriendlyByteBuf buffer;

    /**
     * Message creation.
     *
     * @param dummy just pass true to initialize the message for sending.
     */
    public UpdateClientWithCompatibilityMessage(final boolean dummy)
    {
        super(TYPE);

        this.buffer = new FriendlyByteBuf(Unpooled.buffer());
        IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager().serialize(this.buffer);
    }

    protected UpdateClientWithCompatibilityMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.buffer = new FriendlyByteBuf(buf.retain());
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.buffer.resetReaderIndex();
        buf.writeBytes(this.buffer);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        final ClientLevel world = Minecraft.getInstance().level;
        FurnaceRecipes.getInstance().loadUtilityPredicates();
        try
        {
            IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager().deserialize(this.buffer, world);
        }
        catch (Exception e)
        {
            Log.getLogger().error("Failed to load compatibility manager", e);
        }
        this.buffer.release();
    }
}
