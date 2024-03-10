package com.minecolonies.core.network.messages.client;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.client.gui.townhall.WindowTownHallCantCreateColony;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.network.NetworkEvent;

/**
 * Message to open the colony founding covenant.
 */
public class OpenCantFoundColonyWarningMessage implements IMessage
{
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
    public OpenCantFoundColonyWarningMessage()
    {
        super();
    }

    public OpenCantFoundColonyWarningMessage(final Component warningMessageTranslationKey, final BlockPos townHallPos, final boolean displayConfigTooltip)
    {
        super();
        this.warningMessageTranslationKey = warningMessageTranslationKey;
        this.townHallPos = townHallPos;
        this.displayConfigTooltip = displayConfigTooltip;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer)
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

    @Override
    public void fromBytes(FriendlyByteBuf buf)
    {
        this.warningMessageTranslationKey = buf.readComponent();
        this.townHallPos = buf.readBlockPos();
        this.displayConfigTooltip = buf.readBoolean();
    }
}
