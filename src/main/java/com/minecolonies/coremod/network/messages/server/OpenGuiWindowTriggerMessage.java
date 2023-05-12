package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public class OpenGuiWindowTriggerMessage implements IMessage
{
    /**
     * The window's Resource
     */
    private String resource;

    /**
     * Empty constructor used when registering the message.
     */
    public OpenGuiWindowTriggerMessage()
    {
        super();
    }

    public OpenGuiWindowTriggerMessage(final String resource)
    {
        super();
        this.resource = resource;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(this.resource);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.resource = buf.readUtf(32767);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer player = ctxIn.getSender();
        if (player != null)
        {
            AdvancementTriggers.OPEN_GUI_WINDOW.trigger(player, this.resource);
        }
    }
}
