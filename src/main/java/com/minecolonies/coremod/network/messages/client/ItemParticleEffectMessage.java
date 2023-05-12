package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Empty constructor used when registering the
     */
    public ItemParticleEffectMessage()
    {
        super();
    }

    /**
     * Constructor to trigger an item particle message for eating.
     *
     * @param stack         the stack.
     * @param posX          the double x pos.
     * @param posY          the double y pos.
     * @param posZ          the double z pos.
     * @param rotationPitch the rotation pitch.
     * @param rotationYaw   the rotation yaw.
     * @param eyeHeight     the eye height.
     */
    public ItemParticleEffectMessage(
      final ItemStack stack,
      final double posX,
      final double posY,
      final double posZ,
      final double rotationPitch,
      final double rotationYaw,
      final double eyeHeight)
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
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        stack = buf.readItem();
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
        rotationPitch = buf.readDouble();
        rotationYaw = buf.readDouble();
        eyeHeight = buf.readDouble();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeItem(stack);
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
        buf.writeDouble(rotationPitch);
        buf.writeDouble(rotationYaw);
        buf.writeDouble(eyeHeight);
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
        if (localStack.getUseAnimation() == UseAnim.EAT)
        {
            for (int i = 0; i < 5; ++i)
            {
                Vec3 randomPos = new Vec3((RAND.nextDouble() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                randomPos = randomPos.xRot((float) (-rotationPitch * 0.017453292F));
                randomPos = randomPos.yRot((float) (-rotationYaw * 0.017453292F));
                final double d0 = -RAND.nextDouble() * 0.6D - 0.3D;
                Vec3 randomOffset = new Vec3((RAND.nextDouble() - 0.5D) * 0.3D, d0, 0.6D);
                randomOffset = randomOffset.xRot((float) (-rotationPitch * 0.017453292F));
                randomOffset = randomOffset.yRot((float) (-rotationYaw * 0.017453292F));
                randomOffset = randomOffset.add(posX, posY + eyeHeight, posZ);
                world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, localStack),
                  randomOffset.x,
                  randomOffset.y,
                  randomOffset.z,
                  randomPos.x,
                  randomPos.y + 0.05D,
                  randomPos.z);
            }
        }
    }
}
