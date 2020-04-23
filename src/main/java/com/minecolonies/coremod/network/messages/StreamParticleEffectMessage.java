package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Handles spawning item particle effects in a stream between two targets.
 */
public class StreamParticleEffectMessage implements IMessage
{
    /**
     * Random obj.
     */
    private static final Random RAND = new Random();

    /**
     * The itemStack for the particles.
     */
    private BasicParticleType type;

    /**
     * The start position.
     */
    private double sPosX;
    private double sPosY;
    private double sPosZ;

    /**
     * The end position.
     */
    private double ePosX;
    private double ePosY;
    private double ePosZ;

    /**
     * The stage of the transfer.
     */
    private int stage;

    /**
     * The max stage of the transfer.
     */
    private int maxStage;

    /**
     * Empty constructor used when registering the 
     */
    public StreamParticleEffectMessage()
    {
        super();
    }

    /**
     * Start a particle stream.
     * @param start the starting position.
     * @param end the end position.
     * @param type the particle type.
     * @param maxStage the max stage of the stream
     * @param stage the current stage.
     */
    public StreamParticleEffectMessage(final Vec3d start, final Vec3d end, final BasicParticleType type, final int stage, final int maxStage)
    {
        super();
        this.sPosX = start.x;
        this.sPosY = start.y -0.5;
        this.sPosZ = start.z;

        this.ePosX = end.x;
        this.ePosY = end.y -0.5;
        this.ePosZ = end.z;

        this.stage = stage;
        this.maxStage = maxStage;

        this.type = type;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.sPosX = buf.readDouble();
        this.sPosY = buf.readDouble();
        this.sPosZ = buf.readDouble();

        this.ePosX = buf.readDouble();
        this.ePosY = buf.readDouble();
        this.ePosZ = buf.readDouble();

        this.stage = buf.readInt();
        this.maxStage = buf.readInt();
        this.type = (BasicParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(buf.readResourceLocation());

    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeDouble(this.sPosX);
        buf.writeDouble(this.sPosY);
        buf.writeDouble(this.sPosZ);

        buf.writeDouble(this.ePosX);
        buf.writeDouble(this.ePosY);
        buf.writeDouble(this.ePosZ);

        buf.writeInt(this.stage);
        buf.writeInt(this.maxStage);
        buf.writeResourceLocation(this.type.getRegistryName());
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
        final ClientWorld world = Minecraft.getInstance().world;

        final Vec3d end = new Vec3d(ePosX, ePosY, ePosZ);

        double xDif = ( sPosX - ePosX ) / maxStage ;
        double yDif = ( sPosY - ePosY ) / maxStage;
        double zDif = ( sPosZ - ePosZ ) / maxStage;

        final double curve = maxStage / 3.0;

        for (int step = Math.max(0, stage - 1); step <= Math.min(maxStage, stage + 1); step++)
        {
            double minDif = Math.min(step, Math.abs(step-maxStage))/curve;

            for (int i = 0; i < 10; ++i)
            {
                final Vec3d randomPos = new Vec3d(RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D);
                final Vec3d randomOffset = new Vec3d((RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D);
                world.addParticle(type,
                  end.x + randomOffset.x + xDif * step,
                  end.y + randomOffset.y + yDif *step + minDif,
                  end.z + randomOffset.z + zDif * step,
                  randomPos.x,
                  randomPos.y + 0.05D,
                  randomPos.z);
            }
        }
    }
}
