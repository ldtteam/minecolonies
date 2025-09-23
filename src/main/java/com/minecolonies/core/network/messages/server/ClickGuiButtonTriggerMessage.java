package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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
    private final ResourceLocation resource;

    public ClickGuiButtonTriggerMessage(final String buttonId, final ResourceLocation resource)
    {
        super(TYPE);
        this.resource = resource;
        this.buttonId = buttonId;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeResourceLocation(this.resource);
        buf.writeUtf(this.buttonId);
    }

    protected ClickGuiButtonTriggerMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.resource = buf.readResourceLocation();
        this.buttonId = buf.readUtf();
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer player)
    {
        AdvancementTriggers.CLICK_GUI_BUTTON.get().trigger(player, this.buttonId, this.resource);
    }
}
