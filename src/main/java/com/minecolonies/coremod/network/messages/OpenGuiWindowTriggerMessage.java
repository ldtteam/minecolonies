package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
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
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeString(this.resource);
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        this.resource = buf.readString(32767);
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
            AdvancementTriggers.OPEN_GUI_WINDOW.trigger(player, this.resource);
        }
    }
}
