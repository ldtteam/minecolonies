package com.minecolonies.core.debug.messages;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.debug.DebugPlayerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enables debug mode for the client
 */
public class DebugEnableMessage implements IMessage
{
    /**
     * Whether to enable or disable debug mode
     */
    private boolean enable = true;

    public DebugEnableMessage()
    {
        super();
    }

    public DebugEnableMessage(final boolean enable)
    {
        this.enable = enable;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        enable = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBoolean(enable);
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
        DebugPlayerManager.setDebugModeFor(Minecraft.getInstance().player.getUUID(), enable);
    }
}
