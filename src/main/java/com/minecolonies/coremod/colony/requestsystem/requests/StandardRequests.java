package com.minecolonies.coremod.colony.requestsystem.requests;

import com.minecolonies.api.colony.requestsystem.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.*;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Final class holding all the requests for requestables inside minecolonie
 */
public final class StandardRequests
{

    /**
     * private constructor to hide the implicit public one.
     */
    private StandardRequests()
    {
    }


    public static class ItemStackRequest extends AbstractRequest<Stack>
    {

        public ItemStackRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final Stack requested)
        {
            super(requester, token, requested);
        }

        public ItemStackRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final RequestState state, @NotNull final Stack requested)
        {
            super(requester, token, state, requested);
        }
    }

    public static class DeliveryRequest extends AbstractRequest<Delivery>
    {

        public DeliveryRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final Delivery requested)
        {
            super(requester, token, requested);
        }

        public DeliveryRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final RequestState state, @NotNull final Delivery requested)
        {
            super(requester, token, state, requested);
        }

        /**
         * Method to get the ItemStack used for the getDelivery.
         *
         * @return The ItemStack that the Deliveryman transports around. ItemStack.Empty means no delivery possible.
         */
        @Nullable
        @Override
        public ItemStack getDelivery()
        {
            if (getResult() != null && !ItemStackUtils.isEmpty(getResult().getStack()))
            {
                return getResult().getStack();
            }

            return ItemStackUtils.EMPTY;
        }
    }

    public static class ToolRequest extends AbstractRequest<Tool>
    {

        public ToolRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final Tool requested)
        {
            super(requester, token, requested);
        }

        public ToolRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final RequestState state, @NotNull final Tool requested)
        {
            super(requester, token, state, requested);
        }
    }

    public static class FoodRequest extends AbstractRequest<Food>
    {

        FoodRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final Food requested)
        {
            super(requester, token, requested);
        }

        FoodRequest(
                     @NotNull final IRequester requester,
                     @NotNull final IToken token,
                     @NotNull final RequestState state,
                     @NotNull final Food requested)
        {
            super(requester, token, state, requested);
        }
    }

    public static class BurnableRequest extends AbstractRequest<Burnable>
    {

        BurnableRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final Burnable requested)
        {
            super(requester, token, requested);
        }

        BurnableRequest(
                         @NotNull final IRequester requester,
                         @NotNull final IToken token,
                         @NotNull final RequestState state,
                         @NotNull final Burnable requested)
        {
            super(requester, token, state, requested);
        }
    }
}
