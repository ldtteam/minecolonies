package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Handles spawning item particle effects on top of a block..
 */
public class LocalizedParticleEffectMessage implements IMessage
{
    /**
     * Random obj.
     */
    private static final Random RAND = new Random();

    /**
     * The itemStack for the particles.
     */
    private ItemStack stack;

    /**
     * The entity position.
     */
    private double posX;
    private double posY;
    private double posZ;

    /**
     * Empty constructor used when registering the 
     */
    public LocalizedParticleEffectMessage()
    {
        super();
    }

    /**
     * Constructor to trigger an item particle message for crushing.
     *
     * @param stack         the stack.
     * @param pos          the pos.
     */
    public LocalizedParticleEffectMessage(final ItemStack stack, final BlockPos pos)
    {
        super();
        this.stack = stack;
        this.posX = pos.getX() + 0.5;
        this.posY = pos.getY() + 0.5;
        this.posZ = pos.getZ() + 0.5;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        stack = buf.readItemStack();
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeItemStack(stack);
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
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
        final ItemStack localStack = stack;

        for (int i = 0; i < 5; ++i)
        {
            final Vec3d randomPos = new Vec3d((RAND.nextDouble() - 0.5D) * 0.1D, RAND.nextDouble() * 0.1D + 0.1D, 0.0D);
            final Vec3d randomOffset = new Vec3d((RAND.nextDouble() - 0.5D) * 0.1D, RAND.nextDouble() - 0.5D * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D);
                world.addParticle(new ItemParticleData(ParticleTypes.ITEM, localStack),
                  posX + randomOffset.x,
                  posY + randomOffset.y,
                  posZ + randomOffset.z,
                  randomPos.x,
                  randomPos.y + 0.05D,
                  randomPos.z);
        }
    }
}
