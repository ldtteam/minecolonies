package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.util.FurnaceRecipes;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to update the recipes on the client side.
 */
public class UpdateClientWithCompatibilityMessage implements IMessage
{
    private FriendlyByteBuf buffer;

    /**
     * Empty public constructor.
     */
    public UpdateClientWithCompatibilityMessage()
    {
        super();
    }

    /**
     * Message creation.
     *
     * @param dummy just pass true to initialize the message for sending.
     */
    public UpdateClientWithCompatibilityMessage(final boolean dummy)
    {
        super();

        this.buffer = new FriendlyByteBuf(Unpooled.buffer());
        IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager().serialize(this.buffer);
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.buffer = new FriendlyByteBuf(buf.retain());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.buffer.resetReaderIndex();
        buf.writeBytes(this.buffer);
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
