package com.minecolonies.coremod.network.messages.client.colony;

import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Asks the client to play a specific music
 */
public class PlayMusicAtPosMessage implements IMessage
{
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
    private final ResourceLocation dimensionID;

    /**
     * The volume to use
     */
    private final float volume;

    /**
     * pitch to use
     */
    private final float pitch;

    /**
     * Default constructor.
     */
    public PlayMusicAtPosMessage(final PacketBuffer buf)
    {
        this.soundEvent = Registry.SOUND_EVENT.getByValue(buf.readVarInt());
        this.pos = buf.readBlockPos();
        this.dimensionID = new ResourceLocation(buf.readString(32767));
        this.volume = buf.readFloat();
        this.pitch = buf.readFloat();
    }

    /**
     * Create a play music message with a specific sound event.
     *
     * @param event the sound event.
     */
    public PlayMusicAtPosMessage(final SoundEvent event, final BlockPos pos, final World world, final float volume, final float pitch)
    {
        this.soundEvent = event;
        this.pos = pos;
        this.dimensionID = world.getDimensionKey().func_240901_a_();
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeVarInt(Registry.SOUND_EVENT.getId(this.soundEvent));
        buf.writeBlockPos(pos);
        buf.writeString(dimensionID.toString());
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
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
        if (Minecraft.getInstance().world.getDimensionKey().func_240901_a_().equals(dimensionID))
        {
            Minecraft.getInstance().world.playSound(Minecraft.getInstance().player, pos.getX(), pos.getY(), pos.getZ(), soundEvent, SoundCategory.AMBIENT, volume, pitch);
        }
    }
}
