package com.minecolonies.core.network.messages.client.colony;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Asks the client to play a specific music
 */
public class PlayMusicAtPosMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "play_music_at_pos", PlayMusicAtPosMessage::new);

    /**
     * The sound event to play.
     */
    private final SoundEvent soundEvent;

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
     * pitch to use
     */
    private final float pitch;

    /**
     * Create a play music message with a specific sound event.
     *
     * @param event the sound event.
     */
    public PlayMusicAtPosMessage(final SoundEvent event, final BlockPos pos, final Level world, final float volume, final float pitch)
    {
        super(TYPE);
        this.soundEvent = event;
        this.pos = pos;
        this.dimensionID = world.dimension();
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeResourceLocation(BuiltInRegistries.SOUND_EVENT.getKey(this.soundEvent));
        buf.writeBlockPos(pos);
        buf.writeUtf(dimensionID.location().toString());
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
    }

    protected PlayMusicAtPosMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.soundEvent = BuiltInRegistries.SOUND_EVENT.get(buf.readResourceLocation());
        this.pos = buf.readBlockPos();
        this.dimensionID = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)));
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
    }

    
    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        if (player.level().dimension() == dimensionID)
        {
            player.level().playSound(player, pos.getX(), pos.getY(), pos.getZ(), soundEvent, SoundSource.AMBIENT, volume, pitch);
        }
    }
}
