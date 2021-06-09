package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
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
    private SoundCategory category = SoundCategory.MUSIC;

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
    public PlayAudioMessage(final SoundEvent event, final SoundCategory category)
    {
        super();
        this.soundEvent = event;
        this.category = category;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        // TODO: switch to proper registry
        buf.writeVarInt(category.ordinal());
        buf.writeVarInt(Registry.SOUND_EVENT.getId(this.soundEvent));
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        this.category = SoundCategory.values()[buf.readVarInt()];
        this.soundEvent = Registry.SOUND_EVENT.byId(buf.readVarInt());
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
        Minecraft.getInstance().getSoundManager().play(new SimpleSound(
          soundEvent.getLocation(), category,
          1.0F, 1.0F, false, 0,
          ISound.AttenuationType.NONE, 0, 0, 0, true));
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
        List<PlayerEntity> players = important
          ? col.getImportantMessageEntityPlayers()
          : col.getMessagePlayerEntities();

        for (PlayerEntity player : players)
        {
            if (stop)
            {
                Network.getNetwork().sendToPlayer(new StopMusicMessage(), (ServerPlayerEntity) player);
            }

            for (PlayAudioMessage pam : messages)
            {
                Network.getNetwork().sendToPlayer(pam, (ServerPlayerEntity) player);
            }
        }
    }
}
