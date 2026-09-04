package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Asks the client to play a specific music
 */
public class PlayAudioMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "play_audio", PlayAudioMessage::new);

    /**
     * The sound event to play.
     */
    private final Identifier soundEvent;
    private final SoundSource      category;
    private static volatile BiConsumer<PlayAudioMessage, Player> clientHandler = (message, player) -> { };

    /**
     * Create a play music message with a specific sound event.
     *
     * @param event the sound event.
     */
    public PlayAudioMessage(final SoundEvent event)
    {
        this(event, SoundSource.MUSIC);
    }

    /**
     * Create a play music message with a specific sound event.
     *
     * @param event the sound event.
     * @param category the sound category to play on
     */
    public PlayAudioMessage(final SoundEvent event, final SoundSource category)
    {
        super(TYPE);
        this.soundEvent = event.location();
        this.category = category;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeVarInt(category.ordinal());
        buf.writeIdentifier(soundEvent);
    }

    protected PlayAudioMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.category = SoundSource.values()[buf.readVarInt()];
        this.soundEvent = buf.readIdentifier();
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        clientHandler.accept(this, player);
    }

    /**
     * Installs the client-side playback implementation without making this
     * common payload class load Minecraft's client sound classes on a server.
     */
    public static void setClientHandler(final BiConsumer<PlayAudioMessage, Player> handler)
    {
        clientHandler = Objects.requireNonNull(handler, "handler");
    }

    Identifier soundEventId()
    {
        return soundEvent;
    }

    SoundSource category()
    {
        return category;
    }

    /**
     * Plays a sound event to everyone in the colony
     * @param col the colony
     * @param important if the audio is sent to important message players only
     * @param stop if all other sounds should be stopped first
     * @param messages one or more messages to send to each player.
     */
    public static void sendToAll(final IColony col, final boolean important, final boolean stop, final PlayAudioMessage... messages)
    {
        final List<Player> players = important
          ? col.getImportantMessageEntityPlayers()
          : col.getMessagePlayerEntities();

        for (final Player player : players)
        {
            if (stop)
            {
                new StopMusicMessage().sendToPlayer((ServerPlayer) player);
            }

            for (final PlayAudioMessage pam : messages)
            {
                pam.sendToPlayer((ServerPlayer) player);
            }
        }
    }
}
