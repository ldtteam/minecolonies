package com.minecolonies.core.debug.messages;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.debug.gui.DebugWindowCitizen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message for sending debug text to the client. Used for citizen debug window for now
 */
public class DebugOutputMessage implements IMessage
{
    /**
     * The debug information to be displayed in the output
     */
    private Component debugInfo;

    /**
     * Whether to clear the output first
     */
    private boolean clear = false;

    public DebugOutputMessage()
    {
        super();
    }

    public DebugOutputMessage(final Component message, final boolean clear)
    {
        this.debugInfo = message;
        this.clear = clear;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        debugInfo = buf.readComponent();
        clear = buf.readBoolean();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeComponent(debugInfo);
        buf.writeBoolean(clear);
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
        if (clear)
        {
            DebugWindowCitizen.outputMessage = Component.literal("").append(debugInfo);
        }
        else
        {
            DebugWindowCitizen.outputMessage.append(Component.literal("\n")).append(debugInfo);
        }
    }
}
