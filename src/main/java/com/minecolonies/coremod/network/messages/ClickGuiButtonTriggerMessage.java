package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
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
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeString(this.resource);
        buf.writeString(this.buttonId);
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        this.resource = buf.readString(32767);
        this.buttonId = buf.readString(32767);
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
        final ServerPlayerEntity player = ctxIn.getSender();
        if (player != null)
        {
            AdvancementTriggers.CLICK_GUI_BUTTON.trigger(player, this.buttonId, this.resource);
        }
    }
}
