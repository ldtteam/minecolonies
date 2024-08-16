package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.ItemSupplyChestDeployer.SupplyData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Mark that for a given item the story was already read.
 */
public class MarkStoryReadOnItem extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "mark_story_read", MarkStoryReadOnItem::new);

    /**
     * The hand that was holding the item.
     */
    private InteractionHand hand;

    /**
     * Empty constructor used when registering the message
     */
    public MarkStoryReadOnItem(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(type);
        hand = InteractionHand.values()[buf.readInt()];
    }

    /**
     * Set it on the item on the server side.
     *
     * @param hand the hand with the item.
     */
    public MarkStoryReadOnItem(final InteractionHand hand)
    {
        super(TYPE);
        this.hand = hand;
    }

    @Override
    public void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(hand.ordinal());
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer sender)
    {
        SupplyData.updateItemStack(sender.getItemInHand(this.hand), supply -> supply.withSawStory(true));
    }
}
