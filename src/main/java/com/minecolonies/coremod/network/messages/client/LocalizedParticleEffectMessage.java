package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
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
     * @param stack the stack.
     * @param pos   the pos.
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
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        stack = buf.readItem();
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeItem(stack);
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
        final ClientLevel world = Minecraft.getInstance().level;
        final ItemStack localStack = stack;

        for (int i = 0; i < 5; ++i)
        {
            final Vec3 randomPos = new Vec3((RAND.nextDouble() - 0.5D) * 0.1D, RAND.nextDouble() * 0.1D + 0.1D, 0.0D);
            final Vec3 randomOffset = new Vec3((RAND.nextDouble() - 0.5D) * 0.1D, RAND.nextDouble() - 0.5D * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D);
            world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, localStack),
              posX + randomOffset.x,
              posY + randomOffset.y,
              posZ + randomOffset.z,
              randomPos.x,
              randomPos.y + 0.05D,
              randomPos.z);
        }
    }
}
