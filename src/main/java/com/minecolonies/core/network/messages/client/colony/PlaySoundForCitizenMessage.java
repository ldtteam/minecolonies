package com.minecolonies.core.network.messages.client.colony;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractCivilianEntity;
import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.SoundUtils.PITCH;
import static com.minecolonies.api.util.SoundUtils.VOLUME;

/**
 * Play sounds at a citizen for a certain amount of time, sequentially
 */
public class PlaySoundForCitizenMessage implements IMessage
{
    /**
     * The colony id of the citizen.
     */
    private int entityid;

    /**
     * The sound event to play.
     */
    private SoundEvent soundEvent;

    /**
     * The sound source to use.
     */
    private SoundSource soundSource;

    /**
     * The position to play at
     */
    private BlockPos pos;

    /**
     * The dimension id to play in
     */
    private ResourceKey<Level> dimensionID;

    /**
     * The volume to use
     */
    private float volume;

    /**
     * Pitch to use.
     */
    private float pitch;

    /**
     * Length of the audio in ticks.
     */
    private int length;

    /**
     * Number of repetitions in ticks.
     */
    private int repetitions;

    /**
     * Default constructor.
     */
    public PlaySoundForCitizenMessage()
    {
        super();
    }

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
        super();
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
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(ForgeRegistries.SOUND_EVENTS.getKey(this.soundEvent));
        buf.writeInt(this.soundSource.ordinal());
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.dimensionID.location().toString());
        buf.writeFloat(this.volume);
        buf.writeFloat(this.pitch);
        buf.writeInt(this.length);
        buf.writeInt(this.repetitions);
        buf.writeInt(this.entityid);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(buf.readResourceLocation());
        this.soundSource = SoundSource.values()[buf.readInt()];
        this.pos = buf.readBlockPos();
        this.dimensionID = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(buf.readUtf(32767)));
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
        this.length = buf.readInt();
        this.repetitions = buf.readInt();
        this.entityid = buf.readInt();
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
        final Entity entity = Minecraft.getInstance().level.getEntity(this.entityid);
        if (entity instanceof AbstractCivilianEntity)
        {
            IColonyManager.getInstance().getSoundManager().addToQueue(entity.getUUID(), this.soundEvent, this.soundSource, this.repetitions, this.length, this.pos, this.volume, this.pitch);
        }
    }
}
