package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.inventory.container.ContainerExpeditionSheet;
import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class OpenExpeditionSheetInventoryMessage implements IMessage
{
    private InteractionHand hand;

    public OpenExpeditionSheetInventoryMessage()
    {
    }

    public OpenExpeditionSheetInventoryMessage(final InteractionHand hand)
    {
        this.hand = hand;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeEnum(hand);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        hand = buf.readEnum(InteractionHand.class);
    }

    @Override
    public void onExecute(final Context ctxIn, final boolean isLogicalServer)
    {
        NetworkHooks.openScreen(ctxIn.getSender(), new MenuProvider()
        {
            @Override
            @NotNull
            public Component getDisplayName()
            {
                return Component.literal("Expedition Sheet Inventory");
            }

            @Override
            @NotNull
            public AbstractContainerMenu createMenu(final int windowId, final @NotNull Inventory inventory, final @NotNull Player player)
            {
                return new ContainerExpeditionSheet(windowId, inventory, player.getItemInHand(hand));
            }
        }, packetBuffer -> packetBuffer.writeEnum(hand));
    }
}
