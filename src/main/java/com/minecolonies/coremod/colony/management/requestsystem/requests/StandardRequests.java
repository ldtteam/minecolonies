package com.minecolonies.coremod.colony.management.requestsystem.requests;

import com.minecolonies.coremod.colony.management.requestsystem.api.RequestState;
import com.minecolonies.coremod.colony.management.requestsystem.api.token.IToken;
import com.minecolonies.coremod.colony.management.requestsystem.requestable.Delivery;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Final class holding all the requests for requestables inside minecolonie
 */
public final class StandardRequests {

    public static class ItemStackRequest extends AbstractRequest<ItemStack> {

        public ItemStackRequest(@NotNull IToken token, @NotNull ItemStack requested) {
            super(token, requested);
        }

        public ItemStackRequest(@NotNull IToken token, @NotNull RequestState state, @NotNull ItemStack requested) {
            super(token, state, requested);
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

        public DeliveryRequest(@NotNull IToken token, @NotNull Delivery requested) {
            super(token, requested);
        }

        public DeliveryRequest(@NotNull IToken token, @NotNull RequestState state, @NotNull Delivery requested) {
            super(token, state, requested);
        }
    }
}
