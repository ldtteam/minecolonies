package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.advancements.AdvancementTriggers;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ClickGuiButtonTriggerMessage extends AbstractMessage<ClickGuiButtonTriggerMessage, IMessage>
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
    public void fromBytes(final ByteBuf buf)
    {
        this.buttonId = ByteBufUtils.readUTF8String(buf);
        this.resource = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.buttonId);
        ByteBufUtils.writeUTF8String(buf, this.resource);
    }

    @Override
    public void messageOnServerThread(final ClickGuiButtonTriggerMessage message, final EntityPlayerMP player)
    {
        AdvancementTriggers.CLICK_GUI_BUTTON.trigger(player, message.buttonId, message.resource);
    }
}
