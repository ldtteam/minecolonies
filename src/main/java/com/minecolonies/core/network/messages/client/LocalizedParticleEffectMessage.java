package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Handles spawning item particle effects on top of a block..
 */
public class LocalizedParticleEffectMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "localized_particle_effect", LocalizedParticleEffectMessage::new);

    /**
     * Random obj.
     */
    private static final Random RAND = new Random();

    /**
     * The itemStack for the particles.
     */
    private final ItemStack stack;

    /**
     * The entity position.
     */
    private final double posX;
    private final double posY;
    private final double posZ;

    /**
     * Constructor to trigger an item particle message for crushing.
     *
     * @param stack the stack.
     * @param pos   the pos.
     */
    public LocalizedParticleEffectMessage(final ItemStack stack, final BlockPos pos)
    {
        super(TYPE);
        this.stack = stack;
        this.posX = pos.getX() + 0.5;
        this.posY = pos.getY() + 0.5;
        this.posZ = pos.getZ() + 0.5;
    }

    protected LocalizedParticleEffectMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        stack = Utils.deserializeCodecMess(buf);;
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        Utils.serializeCodecMess(buf, stack);
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        for (int i = 0; i < 5; ++i)
        {
            final Vec3 randomPos = new Vec3((RAND.nextDouble() - 0.5D) * 0.1D, RAND.nextDouble() * 0.1D + 0.1D, 0.0D);
            final Vec3 randomOffset = new Vec3((RAND.nextDouble() - 0.5D) * 0.1D, RAND.nextDouble() - 0.5D * 0.1D, (RAND.nextDouble() - 0.5D) * 0.1D);
            player.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
              posX + randomOffset.x,
              posY + randomOffset.y,
              posZ + randomOffset.z,
              randomPos.x,
              randomPos.y + 0.05D,
              randomPos.z);
        }
    }
}
