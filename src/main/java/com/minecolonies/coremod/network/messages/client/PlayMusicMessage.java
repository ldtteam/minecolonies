package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Asks the client to play a specific music
 */
public class PlayMusicMessage implements IMessage
{
    /**
     * The sound event to play.
     */
    private SoundEvent soundEvent;

    /**
     * Default constructor.
     */
    public PlayMusicMessage()
    {
        super();
    }

    /**
     * Create a play music message with a specific sound event.
     * @param event the sound event.
     */
    public PlayMusicMessage(final SoundEvent event)
    {
        super();
        this.soundEvent = event;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeVarInt(Registry.SOUND_EVENT.getId(this.soundEvent));
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        this.soundEvent = Registry.SOUND_EVENT.getByValue(buf.readVarInt());
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
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.music(soundEvent));
    }
}
