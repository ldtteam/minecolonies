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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Message to update the recipes on the client side.
 */
public class UpdateClientWithCompatibilityMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "update_client_with_compatibility", UpdateClientWithCompatibilityMessage::new, true, false);

    private final RegistryFriendlyByteBuf buffer;

    /**
     * Message creation.
     *
     * @param dummy just pass true to initialize the message for sending.
     */
    public UpdateClientWithCompatibilityMessage(final boolean dummy)
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
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
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
    }
}
