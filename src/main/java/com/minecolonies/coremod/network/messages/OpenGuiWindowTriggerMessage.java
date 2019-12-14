package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.advancements.AdvancementTriggers;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class OpenGuiWindowTriggerMessage extends AbstractMessage<OpenGuiWindowTriggerMessage, IMessage>
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
    public void fromBytes(final ByteBuf buf)
    {
        this.resource = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.resource);
    }

    @Override
    public void messageOnServerThread(final OpenGuiWindowTriggerMessage message, final EntityPlayerMP player)
    {
        AdvancementTriggers.OPEN_GUI_WINDOW.trigger(player, message.resource);
    }
}
