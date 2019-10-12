package com.minecolonies.coremod.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Handles spawning item particle effects on top of a block..
 */
public class LocalizedParticleEffectMessage extends AbstractMessage<LocalizedParticleEffectMessage, IMessage>
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
     * Empty constructor used when registering the message.
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
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        stack = ByteBufUtils.readItemStack(buf);
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeItemStack(buf, stack);
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
    }

    @Override
    protected void messageOnClientThread(final LocalizedParticleEffectMessage message, final MessageContext ctx)
    {
        final WorldClient world = Minecraft.getMinecraft().world;
        final ItemStack localStack = message.stack;

        for (int i = 0; i < 5; ++i)
        {
            final Vec3d randomPos = new Vec3d((RAND.nextDouble() - 0.5D) * 0.1D, RAND.nextDouble() * 0.1D + 0.1D, 0.0D);
            final Vec3d randomOffset = new Vec3d((RAND.nextDouble() - 0.5D) * 0.1D, RAND.nextDouble() - 0.5D * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D);
            if (localStack.getHasSubtypes())
            {
                world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
                  message.posX + randomOffset.x,
                  message.posY + randomOffset.y,
                  message.posZ + randomOffset.z,
                  randomPos.x,
                  randomPos.y + 0.05D,
                  randomPos.z,
                  Item.getIdFromItem(localStack.getItem()), localStack.getMetadata());
            }
            else
            {
                world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
                  message.posX + randomOffset.x,
                  message.posY + randomOffset.y,
                  message.posZ + randomOffset.z,
                  randomPos.x,
                  randomPos.y + 0.05D,
                  randomPos.z,
                  Item.getIdFromItem(localStack.getItem()));
            }
        }
    }
}
