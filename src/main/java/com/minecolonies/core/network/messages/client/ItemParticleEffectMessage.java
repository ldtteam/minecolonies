package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Handles spawning item particle effects close to an entity.
 */
public class ItemParticleEffectMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "item_particle_effect", ItemParticleEffectMessage::new);

    /**
     * Random obj.
     */
    private static final Random RAND = new Random();

    /**
     * The itemStack for the particles.
     */
    private final ItemStack stack;

    /**
     * The entity rotation pitch.
     */
    private final double rotationPitch;

    /**
     * The entity rotation yaw.
     */
    private final double rotationYaw;

    /**
     * The entity eye height.
     */
    private final double eyeHeight;

    /**
     * The entity position.
     */
    private final double posX;
    private final double posY;
    private final double posZ;

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
        super(TYPE);
        this.stack = stack;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.rotationPitch = rotationPitch;
        this.rotationYaw = rotationYaw;
        this.eyeHeight = eyeHeight;
    }

    protected ItemParticleEffectMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        stack = buf.readItem();
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
        rotationPitch = buf.readDouble();
        rotationYaw = buf.readDouble();
        eyeHeight = buf.readDouble();
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeItem(stack);
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
        buf.writeDouble(rotationPitch);
        buf.writeDouble(rotationYaw);
        buf.writeDouble(eyeHeight);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        if (stack.getUseAnimation() == UseAnim.EAT)
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
                player.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
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
