package com.minecolonies.core.debug.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.debug.DebugPlayerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Enables debug mode for the client
 */
public class DebugEnableMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "debug_enable", DebugEnableMessage::new);

    /**
     * Whether to enable or disable debug mode
     */
    private boolean enable = true;

    public DebugEnableMessage(final boolean enable)
    {
        super(TYPE);
        this.enable = enable;
    }

    protected DebugEnableMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        enable = buf.readBoolean();
    }

    @Override
    protected void onExecute(final IPayloadContext iPayloadContext, final Player player)
    {
        DebugPlayerManager.setDebugModeFor(Minecraft.getInstance().player.getUUID(), enable);
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeBoolean(enable);
    }
}
