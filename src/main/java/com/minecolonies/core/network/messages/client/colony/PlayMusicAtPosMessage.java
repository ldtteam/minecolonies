package com.minecolonies.core.network.messages.client.colony;

import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Asks the client to play a specific music
 */
public class PlayMusicAtPosMessage implements IMessage
{
    /**
     * The sound event to play.
     */
    private SoundEvent soundEvent;

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
     * pitch to use
     */
    private float pitch;

    /**
     * Default constructor.
     */
    public PlayMusicAtPosMessage()
    {
        super();
    }

    /**
     * Create a play music message with a specific sound event.
     *
     * @param event the sound event.
     */
    public PlayMusicAtPosMessage(final SoundEvent event, final BlockPos pos, final Level world, final float volume, final float pitch)
    {
        super();
        this.soundEvent = event;
        this.pos = pos;
        this.dimensionID = world.dimension();
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeVarInt(Registry.SOUND_EVENT.getId(this.soundEvent));
        buf.writeBlockPos(pos);
        buf.writeUtf(dimensionID.location().toString());
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.soundEvent = Registry.SOUND_EVENT.byId(buf.readVarInt());
        this.pos = buf.readBlockPos();
        this.dimensionID = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
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
        if (Minecraft.getInstance().level.dimension() == dimensionID)
        {
            Minecraft.getInstance().level.playSound(Minecraft.getInstance().player, pos.getX(), pos.getY(), pos.getZ(), soundEvent, SoundSource.AMBIENT, volume, pitch);
        }
    }
}
