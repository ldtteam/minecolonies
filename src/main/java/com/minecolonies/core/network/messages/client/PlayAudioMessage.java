package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
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
    private ResourceLocation soundEvent;
    private SoundSource      category;

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
        this.soundEvent = event.getLocation();
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
        this.soundEvent = event.getLocation();
        this.category = category;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeVarInt(category.ordinal());
        buf.writeResourceLocation(soundEvent);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.category = SoundSource.values()[buf.readVarInt()];
        this.soundEvent = buf.readResourceLocation();
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
        final Player player = Minecraft.getInstance().player;

        if (player == null)
        {
            return;
        }

        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
          soundEvent, category,
            1.0F, 1.0F, RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE, player.getX(), player.getY(), player.getZ(), true));
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
