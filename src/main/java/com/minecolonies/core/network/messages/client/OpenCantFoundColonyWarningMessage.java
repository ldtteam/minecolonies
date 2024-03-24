package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.townhall.WindowTownHallCantCreateColony;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Message to open the colony founding covenant.
 */
public class OpenCantFoundColonyWarningMessage  extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "open_cant_found_colony_warning", LocalizedParticleEffectMessage::new);

    /**
     * Colony pos at which we are trying to place.
     */
    private BlockPos townHallPos;

    /**
     * Warning message to display why colony creation is not possible.
     */
    private Component warningMessageTranslationKey;

    /**
     * If we need to set the config setting tooltip.
     */
    private boolean displayConfigTooltip;

    /**
     * Default constructor
     **/
    public OpenCantFoundColonyWarningMessage(FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(type);
        this.warningMessageTranslationKey = buf.readComponent();
        this.townHallPos = buf.readBlockPos();
        this.displayConfigTooltip = buf.readBoolean();
    }

    public OpenCantFoundColonyWarningMessage(final Component warningMessageTranslationKey, final BlockPos townHallPos, final boolean displayConfigTooltip)
    {
        super(TYPE);
        this.warningMessageTranslationKey = warningMessageTranslationKey;
        this.townHallPos = townHallPos;
        this.displayConfigTooltip = displayConfigTooltip;
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        new WindowTownHallCantCreateColony(townHallPos, (MutableComponent) warningMessageTranslationKey, displayConfigTooltip).open();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeComponent(warningMessageTranslationKey);
        buf.writeBlockPos(townHallPos);
        buf.writeBoolean(displayConfigTooltip);
    }
}
