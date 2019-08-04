package com.minecolonies.coremod.network.messages;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract class for all messages having to schedule a task on the server thread.
 *
 * @param <A> This is the request type - it is the message you expect to <em>receive</em> from remote.
 * @param <B> This is the reply type - it is the message you expect to <em>send</em> in reply. You can use IMessage as the type here
 *            if you don't anticipate sending a reply.
 */
public abstract class AbstractMessage<A extends IMessage, B extends IMessage> implements IMessage, IMessageHandler<A, B>
{

    @Nullable
    @Override
    public B onMessage(final A message, final MessageContext ctx)
    {
        if(ctx.side.isServer())
        {
            final ServerPlayerEntity player = ctx.getServerHandler().player;
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> messageOnServerThread(message, player));
        }
        else
        {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> messageOnClientThread(message, ctx));
        }
        return null;
    }

    /**
     * Override this to schedule actions taken in the client thread.
     *
     * @param message the original message.
     * @param ctx  the context associated.
     */
    protected void messageOnClientThread(final A message, final MessageContext ctx)
    {
        //NOOP
    }

    /**
     * Override this to schedule actions taken in the server thread.
     *
     * @param message the original message.
     * @param player  the player associated.
     */
    public void messageOnServerThread(final A message, final ServerPlayerEntity player)
    {
        //NOOP
    }
}
