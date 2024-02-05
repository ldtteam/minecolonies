package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Asks the client to stop its music
 */
public class StopMusicMessage implements IMessage
{
    public StopMusicMessage()
    {
        super();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {

    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {

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
        Minecraft.getInstance().getSoundManager().stop();
        Minecraft.getInstance().getMusicManager().stopPlaying();
    }
}
