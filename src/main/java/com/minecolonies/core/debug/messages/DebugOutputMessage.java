package com.minecolonies.core.debug.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.debug.gui.DebugWindowCitizen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Message for sending debug text to the client. Used for citizen debug window for now
 */
public class DebugOutputMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "debug_output", DebugOutputMessage::new);

    /**
     * The debug information to be displayed in the output
     */
    private Component debugInfo;

    /**
     * Whether to clear the output first
     */
    private boolean clear = false;

    public DebugOutputMessage(final Component message, final boolean clear)
    {
        super(TYPE);
        this.debugInfo = message;
        this.clear = clear;
    }

    protected DebugOutputMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, TYPE);
        debugInfo = Utils.deserializeCodecMess(ComponentSerialization.STREAM_CODEC, buf);
        clear = buf.readBoolean();
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        Utils.serializeCodecMess(ComponentSerialization.STREAM_CODEC, buf, debugInfo);
        buf.writeBoolean(clear);
    }

    @Override
    protected void onExecute(final IPayloadContext iPayloadContext, final Player player)
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
