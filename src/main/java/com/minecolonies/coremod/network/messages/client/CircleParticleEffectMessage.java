package com.minecolonies.coremod.network.messages.client;

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
 * Handles spawning item particle effects in a circle around a target.
 */
public class CircleParticleEffectMessage implements IMessage
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
    private double posX;
    private double posY;
    private double posZ;

    /**
     * The current stage of the circle.
     */
    private int stage;

    /**
     * Empty constructor used when registering the
     */
    public CircleParticleEffectMessage()
    {
        super();
    }

    /**
     * Start a circling particle effect.
     *
     * @param pos   the position.
     * @param type  the particle type.
     * @param stage the stage.
     */
    public CircleParticleEffectMessage(final Vec3d pos, final BasicParticleType type, final int stage)
    {
        super();
        this.posX = pos.x;
        this.posY = pos.y - 0.5;
        this.posZ = pos.z;
        this.stage = stage;
        this.type = type;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.posX = buf.readDouble();
        this.posY = buf.readDouble();
        this.posZ = buf.readDouble();
        this.stage = buf.readInt();
        this.type = (BasicParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(buf.readResourceLocation());
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeInt(this.stage);
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

        double x = 1.0 * Math.cos(stage * 45.0) + posX;
        double z = 1.0 * Math.sin(stage * 45.0) + posZ;

        for (int i = 0; i < 5; ++i)
        {
            final Vec3d randomPos = new Vec3d(RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D, RAND.nextDouble() * 0.1D + 0.1D);
            final Vec3d randomOffset = new Vec3d((RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D);
            world.addParticle(type,
              x + randomOffset.x,
              posY + randomOffset.y,
              z + randomOffset.z,
              randomPos.x,
              randomPos.y + 0.05D,
              randomPos.z);
        }
    }
}
