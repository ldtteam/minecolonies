package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.items.ISupplyItem;
import com.minecolonies.api.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_SAW_STORY;

/**
 * Mark that for a given item the story was already read.
 */
public class MarkStoryReadOnItem implements IMessage
{
    /**
     * The hand that was holding the item.
     */
    private InteractionHand hand;

    /**
     * Empty constructor used when registering the message
     */
    public MarkStoryReadOnItem()
    {
        super();
    }

    /**
     * Set it on the item on the server side.
     *
     * @param hand the hand with the item.
     */
    public MarkStoryReadOnItem(final InteractionHand hand)
    {
        super();
        this.hand = hand;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        hand = InteractionHand.values()[buf.readInt()];
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(hand.ordinal());
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer player = ctxIn.getSender();
        final ItemStack stackInHand = player.getItemInHand(this.hand);
        if (stackInHand.getItem() instanceof ISupplyItem)
        {
            stackInHand.getOrCreateTag().putBoolean(TAG_SAW_STORY, true);
        }
    }
}
