package com.minecolonies.network.messages;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMessage<A extends IMessage, B extends IMessage> implements IMessage, IMessageHandler<A, B>
{

    @Nullable
    @Override
    public B onMessage(final A message, final MessageContext ctx)
    {
        final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        player.getServerWorld().addScheduledTask(() -> messageOnServerThread(message, player));
        return null;
    }

    /**
     * Override this to schedule actions taken in the server thread.
     *
     * @param message the original message.
     * @param player  the player associated.
     */
    public abstract void messageOnServerThread(final A message, final EntityPlayerMP player);
}
