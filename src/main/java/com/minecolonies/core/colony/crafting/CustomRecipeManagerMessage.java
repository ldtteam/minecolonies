package com.minecolonies.core.colony.crafting;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * The message used to synchronize crafter recipes from a server to a client.
 */
public class CustomRecipeManagerMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "custom_recipe_manager", CustomRecipeManagerMessage::new, true, false);

    /**
     * The buffer with the data.
     */
    private final FriendlyByteBuf managerBuffer;

    /**
     * Add or Update a CustomRecipeManager on the client.
     *
     * @param buf               the bytebuffer.
     */
    public CustomRecipeManagerMessage(final FriendlyByteBuf buf)
    {
        super(TYPE);
        this.managerBuffer = new FriendlyByteBuf(buf.copy());
    }

    protected CustomRecipeManagerMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        managerBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(buf.readByteArray()));
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeByteArray(managerBuffer.array());
    }

    @Override
    public void onExecute(final PlayPayloadContext context, final Player player)
    {
        CustomRecipeManager.getInstance().handleCustomRecipeManagerMessage(managerBuffer);
    }
}