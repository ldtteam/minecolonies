package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Asks the client to play a specific music
 */
public class PlayAudioMessage implements IMessage
{
    /**
     * The sound event to play.
     */
    private SoundEvent soundEvent;
    private SoundSource category = SoundSource.MUSIC;

    /**
     * Default constructor.
     */
    public PlayAudioMessage()
    {
        super();
    }

    /**
     * Create a play music message with a specific sound event.
     *
     * @param event the sound event.
     */
    public PlayAudioMessage(final SoundEvent event)
    {
        super();
        this.soundEvent = event;
    }

    /**
     * Create a play music message with a specific sound event.
     *
     * @param event the sound event.
     * @param category the sound category to play on
     */
    public PlayAudioMessage(final SoundEvent event, final SoundSource category)
    {
        super();
        this.soundEvent = event;
        this.category = category;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        // TODO: switch to proper registry
        buf.writeVarInt(category.ordinal());
        buf.writeResourceLocation(BuiltInRegistries.SOUND_EVENT.getKey(this.soundEvent));
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.category = SoundSource.values()[buf.readVarInt()];
        this.soundEvent = BuiltInRegistries.SOUND_EVENT.get(buf.readResourceLocation());
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
          soundEvent, category,
          1.0F, 1.0F, RandomSource.create(), 0.0, 0.0, 0.0));
    }

    /**
     * Plays a sound event to everyone in the colony
     * @param col the colony
     * @param important if the audio is sent to important message players only
     * @param stop if all other sounds should be stopped first
     * @param messages one or more messages to send to each player.
     */
    public static void sendToAll(IColony col, boolean important, boolean stop, PlayAudioMessage... messages)
    {
        List<Player> players = important
          ? col.getImportantMessageEntityPlayers()
          : col.getMessagePlayerEntities();

        for (Player player : players)
        {
            if (stop)
            {
                Network.getNetwork().sendToPlayer(new StopMusicMessage(), (ServerPlayer) player);
            }

            for (PlayAudioMessage pam : messages)
            {
                Network.getNetwork().sendToPlayer(pam, (ServerPlayer) player);
            }
        }
    }
}
