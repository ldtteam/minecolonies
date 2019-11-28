package com.minecolonies.coremod.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_HEIGHT;
import static com.minecolonies.api.util.constant.CitizenConstants.CITIZEN_WIDTH;

/**
 * Message for vanilla particles around a citizen, in villager-like shape.
 */
public class VanillaParticleMessage extends AbstractMessage<VanillaParticleMessage, IMessage>
{
    /**
     * Citizen Position
     */
    private double x;
    private double y;
    private double z;

    /**
     * Particle id
     */
    private int enumParticleID;

    public VanillaParticleMessage() {super();}

    public VanillaParticleMessage(final double x, final double y, final double z, final int enumParticleID)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.enumParticleID = enumParticleID;
    }

    @Override
    public void fromBytes(final ByteBuf byteBuf)
    {
        x = byteBuf.readDouble();
        y = byteBuf.readDouble();
        z = byteBuf.readDouble();
        enumParticleID = byteBuf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeDouble(x);
        byteBuf.writeDouble(y);
        byteBuf.writeDouble(z);
        byteBuf.writeInt(enumParticleID);
    }

    @Override
    protected void messageOnClientThread(final VanillaParticleMessage message, final MessageContext ctx)
    {
        spawnParticles(EnumParticleTypes.getParticleFromId(message.enumParticleID), Minecraft.getMinecraft().world, message.x, message.y, message.z);
    }

    /**
     * Spawns the given particle randomly around the position.
     *
     * @param particleType praticle to spawn
     * @param world        world to use
     * @param x            x pos
     * @param y            y pos
     * @param z            z pos
     */
    private void spawnParticles(EnumParticleTypes particleType, World world, double x, double y, double z)
    {
        final Random rand = new Random();
        for (int i = 0; i < 5; ++i)
        {
            double d0 = rand.nextGaussian() * 0.02D;
            double d1 = rand.nextGaussian() * 0.02D;
            double d2 = rand.nextGaussian() * 0.02D;
            world.spawnParticle(particleType,
              x + (rand.nextFloat() * CITIZEN_WIDTH * 2.0F) - CITIZEN_WIDTH,
              y + 1.0D + (rand.nextFloat() * CITIZEN_HEIGHT),
              z + (rand.nextFloat() * CITIZEN_WIDTH * 2.0F) - CITIZEN_WIDTH,
              d0,
              d1,
              d2);
        }
    }
}
