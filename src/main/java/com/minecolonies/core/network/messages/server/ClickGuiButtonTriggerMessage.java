package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class ClickGuiButtonTriggerMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "click_gui_button_trigger", ClickGuiButtonTriggerMessage::new);

    /**
     * The ID of the button clicked;
     */
    private final String buttonId;

    /**
     * The window's Resource
     */
    private final String resource;

    public ClickGuiButtonTriggerMessage(final String buttonId, final String resource)
    {
        super(TYPE);
        this.resource = resource;
        this.buttonId = buttonId;
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeUtf(this.resource);
        buf.writeUtf(this.buttonId);
    }

    protected ClickGuiButtonTriggerMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.resource = buf.readUtf(32767);
        this.buttonId = buf.readUtf(32767);
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player)
    {
        AdvancementTriggers.CLICK_GUI_BUTTON.get().trigger(player, this.buttonId, this.resource);
    }
}
