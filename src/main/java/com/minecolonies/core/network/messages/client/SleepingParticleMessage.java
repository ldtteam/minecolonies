package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.initializer.ModParticleTypesInitializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Message for sleeping particles
 */
public class SleepingParticleMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "sleeping_particle", SleepingParticleMessage::new);

    /**
     * Position the particles spawn at
     */
    private final double x;
    private final double y;
    private final double z;

    public SleepingParticleMessage(final double x, final double y, final double z)
    {
        super(TYPE);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    protected SleepingParticleMessage(final FriendlyByteBuf byteBuf, final PlayMessageType<?> type)
    {
        super(byteBuf, type);
        x = byteBuf.readDouble();
        y = byteBuf.readDouble();
        z = byteBuf.readDouble();
    }

    @Override
    protected void toBytes(final FriendlyByteBuf byteBuf)
    {
        byteBuf.writeDouble(x);
        byteBuf.writeDouble(y);
        byteBuf.writeDouble(z);
    }

    @Override
    
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        player.level().addParticle(ModParticleTypesInitializer.SLEEPINGPARTICLE_TYPE,
          x,
          y,
          z,
          1.0f,
          1.0f,
          1.0f);
    }
}
