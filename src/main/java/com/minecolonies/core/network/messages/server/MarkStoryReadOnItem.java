package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.items.ISupplyItem;
import com.minecolonies.api.items.ModDataComponents;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.items.ItemSupplyChestDeployer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
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
        final ItemStack stackInHand = sender.getItemInHand(this.hand);
        final ItemSupplyChestDeployer.SupplyData currentComponent = stackInHand.getOrDefault(ModDataComponents.SUPPLY_COMPONENT, ItemSupplyChestDeployer.SupplyData.EMPTY);
        stackInHand.set(ModDataComponents.SUPPLY_COMPONENT, new ItemSupplyChestDeployer.SupplyData(true, currentComponent.instantPlacement(), currentComponent.randomKey()));
    }
}
