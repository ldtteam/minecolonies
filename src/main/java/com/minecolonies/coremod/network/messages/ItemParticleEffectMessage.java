package com.minecolonies.coremod.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Handles spawning item particle effects close to an entity.
 */
public class ItemParticleEffectMessage implements IMessage
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
     * The entity rotation pitch.
     */
    private double rotationPitch;

    /**
     * The entity rotation yaw.
     */
    private double rotationYaw;

    /**
     * The entity eye height.
     */
    private double eyeHeight;

    /**
     * The entity position.
     */
    private double posX;
    private double posY;
    private double posZ;

    /**
     * Empty constructor used when registering the message.
     */
    public ItemParticleEffectMessage()
    {
        super();
    }

    /**
     * Constructor to trigger an item particle message for eating.
     * @param stack the stack.
     * @param posX the double x pos.
     * @param posY the double y pos.
     * @param posZ the double z pos.
     * @param rotationPitch the rotation pitch.
     * @param rotationYaw the rotation yaw.
     * @param eyeHeight the eye height.
     */
    public ItemParticleEffectMessage(final ItemStack stack, final double posX, final double posY, final double posZ, final double rotationPitch, final double rotationYaw, final double eyeHeight)
    {
        this.stack = stack;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.rotationPitch = rotationPitch;
        this.rotationYaw = rotationYaw;
        this.eyeHeight = eyeHeight;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        stack = ByteBufUtils.readItemStack(buf);
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
        rotationPitch = buf.readDouble();
        rotationYaw = buf.readDouble();
        eyeHeight = buf.readDouble();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeItemStack(buf, stack);
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
        buf.writeDouble(rotationPitch);
        buf.writeDouble(rotationYaw);
        buf.writeDouble(eyeHeight);
    }

    @Override
    protected void messageOnClientThread(final ItemParticleEffectMessage message, final MessageContext ctx)
    {
        final WorldClient world = Minecraft.getMinecraft().world;
        final ItemStack localStack = message.stack;
        if (localStack.getItemUseAction() == EnumAction.EAT)
        {
            for (int i = 0; i < 5; ++i)
            {
                Vec3d randomPos = new Vec3d((RAND.nextDouble() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                randomPos = randomPos.rotatePitch((float) (-message.rotationPitch * 0.017453292F));
                randomPos = randomPos.rotateYaw((float) (-message.rotationYaw * 0.017453292F));
                final double d0 =  -RAND.nextDouble() * 0.6D - 0.3D;
                Vec3d randomOffset = new Vec3d((RAND.nextDouble() - 0.5D) * 0.3D, d0, 0.6D);
                randomOffset = randomOffset.rotatePitch((float) (-message.rotationPitch * 0.017453292F));
                randomOffset = randomOffset.rotateYaw((float) (-message.rotationYaw * 0.017453292F));
                randomOffset = randomOffset.add(message.posX, message.posY + message.eyeHeight, message.posZ);
                if (localStack.getHasSubtypes())
                {
                    world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
                      randomOffset.x,
                      randomOffset.y,
                      randomOffset.z,
                      randomPos.x,
                      randomPos.y + 0.05D,
                      randomPos.z,
                      Item.getIdFromItem(localStack.getItem()), localStack.getMetadata());
                }
                else
                {
                    world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
                      randomOffset.x,
                      randomOffset.y,
                      randomOffset.z,
                      randomPos.x,
                      randomPos.y + 0.05D,
                      randomPos.z,
                      Item.getIdFromItem(localStack.getItem()));
                }
            }
        }
    }
}
