package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.inventory.container.ContainerExpeditionSheet;
import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkHooks;

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
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final Context ctxIn, final boolean isLogicalServer)
    {
        NetworkHooks.openScreen(ctxIn.getSender(),
          new SimpleMenuProvider((windowId, inventory, player1) -> new ContainerExpeditionSheet(windowId, inventory, player1.getItemInHand(hand)),
            Component.literal("Expedition Sheet Inventory")),
          packetBuffer -> packetBuffer.writeEnum(hand));
    }
}
