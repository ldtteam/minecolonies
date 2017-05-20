package com.minecolonies.coremod.colony.requestsystem.requests;

import com.minecolonies.coremod.colony.requestsystem.RequestState;
import com.minecolonies.coremod.colony.requestsystem.requestable.Delivery;
import com.minecolonies.coremod.colony.requestsystem.requestable.Tool;
import com.minecolonies.coremod.colony.requestsystem.requester.IRequester;
import com.minecolonies.coremod.colony.requestsystem.token.IToken;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Final class holding all the requests for requestables inside minecolonie
 */
public final class StandardRequests {

    public static class ItemStackRequest extends AbstractRequest<ItemStack> {

        public ItemStackRequest(@NotNull IRequester requester, @NotNull IToken token, @NotNull ItemStack requested)
        {
            super(requester, token, requested);
        }

        public ItemStackRequest(@NotNull IRequester requester, @NotNull IToken token, @NotNull RequestState state, @NotNull ItemStack requested)
        {
            super(requester, token, state, requested);
        }

        /**
         * Method used to check if the result has been set.
         *
         * @return True when the result has been set, false when not.
         */
        @Override
        public boolean hasResult() {
            return getResult() != null && !getResult().isEmpty();
        }
    }

    public static class DeliveryRequest extends AbstractRequest<Delivery> {

        public DeliveryRequest(@NotNull IRequester requester, @NotNull IToken token, @NotNull Delivery requested)
        {
            super(requester, token, requested);
        }

        public DeliveryRequest(@NotNull IRequester requester, @NotNull IToken token, @NotNull RequestState state, @NotNull Delivery requested)
        {
            super(requester, token, state, requested);
        }

        /**
         * Method to get the ItemStack used for the getDelivery.
         *
         * @return The ItemStack that the Deliveryman transports around. ItemStack.Empty means no delivery possible.
         */
        @NotNull
        @Override
        public ItemStack getDelivery() {
            if (getResult() != null && !getResult().getStack().isEmpty())
                return getResult().getStack();

            return ItemStack.EMPTY;
        }
    }

    public static class ToolRequest extends AbstractRequest<Tool> {

        public ToolRequest(@NotNull IRequester requester, @NotNull IToken token, @NotNull Tool requested)
        {
            super(requester, token, requested);
        }

        public ToolRequest(@NotNull IRequester requester, @NotNull IToken token, @NotNull RequestState state, @NotNull Tool requested)
        {
            super(requester, token, state, requested);
        }

        /**
         * Method to get the ItemStack used for the delivery.
         *
         * @return The ItemStack that the Deliveryman transports around. ItemStack.Empty means no getDelivery possible.
         */
        @NotNull
        @Override
        public ItemStack getDelivery() {
            if (getResult() != null && !getResult().getResult().isEmpty())
                return getResult().getResult();

            return ItemStack.EMPTY;
        }
    }
}
