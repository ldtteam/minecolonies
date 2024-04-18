package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Creates a message to switch recipe outputs when multiple are available.
 */
public class SwitchRecipeCraftingTeachingMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "switch_recipe_crafting", SwitchRecipeCraftingTeachingMessage::new);

    /**
     * Create message.
     */
    public SwitchRecipeCraftingTeachingMessage()
    {
        super(TYPE);
    }

    protected SwitchRecipeCraftingTeachingMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
    }

    @Override
    public void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player)
    {
        if (player.containerMenu instanceof final ContainerCrafting container)
        {
            container.switchRecipes();
        }
    }
}
