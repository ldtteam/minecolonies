package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.network.IMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

public class ClickGuiButtonTriggerMessage implements IMessage
{
    /**
     * The ID of the button clicked;
     */
    private String buttonId;

    /**
     * The window's Resource
     */
    private String resource;

    /**
     * Empty constructor used when registering the message.
     */
    public ClickGuiButtonTriggerMessage()
    {
        super();
    }

    public ClickGuiButtonTriggerMessage(final String buttonId, final String resource)
    {
        super();
        this.resource = resource;
        this.buttonId = buttonId;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(this.resource);
        buf.writeUtf(this.buttonId);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.resource = buf.readUtf(32767);
        this.buttonId = buf.readUtf(32767);
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
            AdvancementTriggers.CLICK_GUI_BUTTON.trigger(player, this.buttonId, this.resource);
        }
    }
}
