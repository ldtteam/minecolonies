package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.entity.citizen.AbstractCivilianEntity;
import com.minecolonies.api.sounds.SoundManager;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static com.minecolonies.api.util.SoundUtils.PITCH;
import static com.minecolonies.api.util.SoundUtils.VOLUME;

/**
 * Play sounds at a citizen for a certain amount of time, sequentially
 */
public class PlaySoundForCitizenMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "play_sound_for_citizen", PlaySoundForCitizenMessage::new);

    /**
     * The colony id of the citizen.
     */
    private final int entityid;

    /**
     * The sound event to play.
     */
    private final SoundEvent soundEvent;

    /**
     * The sound source to use.
     */
    private final SoundSource soundSource;

    /**
     * The position to play at
     */
    private final BlockPos pos;

    /**
     * The dimension id to play in
     */
    private final ResourceKey<Level> dimensionID;

    /**
     * The volume to use
     */
    private final float volume;

    /**
     * Pitch to use.
     */
    private final float pitch;

    /**
     * Length of the audio in ticks.
     */
    private final int length;

    /**
     * Number of repetitions in ticks.
     */
    private final int repetitions;

    /**
     * Play a sound for a certain citizen.
     * @param entityID the entity id.
     * @param event the sound event to place.
     * @param pos the position to play it at.
     * @param world the world to play it in.
     */
    public PlaySoundForCitizenMessage(final int entityID, final SoundEvent event, final BlockPos pos, final Level world)
    {
        this(entityID, event, SoundSource.NEUTRAL, pos, world, (float) VOLUME, (float) PITCH, 1, 1);
    }

    /**
     * Play a sound for a certain citizen.
     * @param entityID the entity id.
     * @param event the sound event to place.
     * @param soundSource the type of source.
     * @param pos the position to play it at.
     * @param world the world to play it in.
     */
    public PlaySoundForCitizenMessage(final int entityID, final SoundEvent event, final SoundSource soundSource, final BlockPos pos, final Level world)
    {
        this(entityID, event, soundSource, pos, world, (float) VOLUME, (float) PITCH, 1, 1);
    }

    /**
     * Play a sound for a certain citizen.
     * @param entityID the entity id.
     * @param event the sound event to place.
     * @param soundSource the type of source.
     * @param pos the position to play it at.
     * @param world the world to play it in.
     * @param length the length of the music.
     * @param repetitions the number of repetitions.
     */
    public PlaySoundForCitizenMessage(final int entityID, final SoundEvent event, final SoundSource soundSource, final BlockPos pos, final Level world, final int length, final int repetitions)
    {
        this(entityID, event, soundSource, pos, world, (float) VOLUME, (float) PITCH, length, repetitions);
    }

    /**
     * Play a sound for a certain citizen.
     * @param entityID the entity id.
     * @param event the sound event to place.
     * @param soundSource the type of source.
     * @param pos the position to play it at.
     * @param world the world to play it in.
     * @param volume the volume.
     * @param pitch the pitch.
     * @param length the length of the music.
     * @param repetitions the number of repetitions.
     */
    public PlaySoundForCitizenMessage(final int entityID, final SoundEvent event, final SoundSource soundSource, final BlockPos pos, final Level world, final float volume, final float pitch, final int length, final int repetitions)
    {
        super(TYPE);
        this.entityid = entityID;
        this.soundEvent = event;
        this.soundSource = soundSource;
        this.pos = pos;
        this.dimensionID = world.dimension();
        this.volume = volume;
        this.pitch = pitch;
        this.length = length;
        this.repetitions = repetitions;
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(BuiltInRegistries.SOUND_EVENT.getKey(this.soundEvent));
        buf.writeInt(this.soundSource.ordinal());
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.dimensionID.location().toString());
        buf.writeFloat(this.volume);
        buf.writeFloat(this.pitch);
        buf.writeInt(this.length);
        buf.writeInt(this.repetitions);
        buf.writeInt(this.entityid);
    }

    public PlaySoundForCitizenMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.soundEvent = BuiltInRegistries.SOUND_EVENT.get(buf.readResourceLocation());
        this.soundSource = SoundSource.values()[buf.readInt()];
        this.pos = buf.readBlockPos();
        this.dimensionID = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
        this.length = buf.readInt();
        this.repetitions = buf.readInt();
        this.entityid = buf.readInt();
    }

    
    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        if (player.level().getEntity(this.entityid) instanceof final AbstractCivilianEntity citizen)
        {
            SoundManager.addToQueue(citizen.getUUID(), this.soundEvent, this.soundSource, this.repetitions, this.length, this.pos, this.volume, this.pitch);
        }
    }
}
