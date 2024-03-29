package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.items.ISupplyItem;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_SAW_STORY;

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
    public MarkStoryReadOnItem(final FriendlyByteBuf buf, final PlayMessageType<?> type)
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
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(hand.ordinal());
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer sender)
    {
        final ItemStack stackInHand = sender.getItemInHand(this.hand);
        if (stackInHand.getItem() instanceof ISupplyItem)
        {
            stackInHand.getOrCreateTag().putBoolean(TAG_SAW_STORY, true);
        }
    }
}
