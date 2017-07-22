package com.minecolonies.coremod.colony.requestsystem.requests;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.RequestState;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequestFactory;
import com.minecolonies.api.colony.requestsystem.requestable.Delivery;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Suppression;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Final class holding all the factories for requestables inside minecolonie
 */
public final class StandardRequestFactories
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN     = "Token";
    private static final String NBT_STATE     = "State";
    private static final String NBT_REQUESTED = "Requested";
    private static final String NBT_RESULT    = "Result";
    private static final String NBT_PARENT    = "Parent";
    private static final String NBT_CHILDREN  = "Children";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    /**
     * Private constructor to hide the implicit public one.
     */
    private StandardRequestFactories()
    {
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class ItemStackFactory implements IRequestFactory<ItemStack, StandardRequests.ItemStackRequest>
    {
        /**
         * Method to get a new instance of a request given the input and token.
         *
         * @param input        The input to build a new request for.
         * @param location     The location of the requester.
         * @param token        The token to build the request from.
         * @param initialState The initial state of the request request.
         * @return The new output instance for a given input.
         */
        @Override
        public StandardRequests.ItemStackRequest getNewInstance(
                                                                 @NotNull final ItemStack input,
                                                                 @NotNull final IRequester location,
                                                                 @NotNull final IToken token,
                                                                 @NotNull final RequestState initialState)
        {
            return new StandardRequests.ItemStackRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<StandardRequests.ItemStackRequest> getFactoryOutputType()
        {
            return new TypeToken<StandardRequests.ItemStackRequest>() {};
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<ItemStack> getFactoryInputType()
        {
            return new TypeToken<ItemStack>() {};
        }

        /**
         * Method to serialize a given Request.
         *
         * @param controller The controller that can be used to serialize complicated types.
         * @param request    The request to serialize.
         * @return The serialized data of the given requets.
         */
        @NotNull
        @Override
        public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ItemStackRequest request)
        {
            final NBTTagCompound compound = new NBTTagCompound();

            final NBTTagCompound tokenCompound = controller.serialize(request.getToken());
            final NBTTagInt stateCompound = request.getState().serializeNBT();
            final NBTTagCompound requestedCompound = request.getRequest().serializeNBT();

            final NBTTagList childrenCompound = new NBTTagList();
            for (final IToken token : request.getChildren())
            {
                childrenCompound.appendTag(controller.serialize(token));
            }

            compound.setTag(NBT_TOKEN, tokenCompound);
            compound.setTag(NBT_STATE, stateCompound);
            compound.setTag(NBT_REQUESTED, requestedCompound);

            if (request.hasResult())
            {
                compound.setTag(NBT_RESULT, request.getResult().serializeNBT());
            }

            if (request.hasParent())
            {
                compound.setTag(NBT_PARENT, controller.serialize(request.getParent()));
            }

            compound.setTag(NBT_CHILDREN, childrenCompound);

            return compound;
        }

        /**
         * Method to deserialize a given Request.
         *
         * @param controller The controller that can be used to deserialize complicated types.
         * @param nbt        The data of the request that should be deserialized.
         * @return The request that corresponds with the given data in the nbt
         */
        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public StandardRequests.ItemStackRequest deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            final IToken token = controller.deserialize(nbt.getCompoundTag(NBT_TOKEN));
            final RequestState state = RequestState.deserializeNBT((NBTTagInt) nbt.getTag(NBT_STATE));
            final ItemStack requested = new ItemStack(nbt.getCompoundTag(NBT_REQUESTED));

            final List<IToken> childTokens = new ArrayList<>();
            final NBTTagList childCompound = nbt.getTagList(NBT_CHILDREN, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < childCompound.tagCount(); i++)
            {
                childTokens.add(controller.deserialize(childCompound.getCompoundTagAt(i)));
            }

            @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
            final StandardRequests.ItemStackRequest request = controller.getNewInstance(requested, new TypeToken<StandardRequests.ItemStackRequest>() {}, token, state);

            if (nbt.hasKey(NBT_PARENT))
            {
                request.setParent(controller.deserialize(nbt.getCompoundTag(NBT_PARENT)));
            }

            if (nbt.hasKey(NBT_RESULT))
            {
                request.setResult(new ItemStack(nbt.getCompoundTag(NBT_RESULT)));
            }

            return request;
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class DeliveryFactory implements IRequestFactory<Delivery, StandardRequests.DeliveryRequest>
    {

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<StandardRequests.DeliveryRequest> getFactoryOutputType()
        {
            return new TypeToken<StandardRequests.DeliveryRequest>() {};
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<Delivery> getFactoryInputType()
        {
            return new TypeToken<Delivery>() {};
        }

        /**
         * Method to serialize a given constructable.
         *
         * @param controller The controller that can be used to serialize complicated types.
         * @param request    The request to serialize.
         * @return The serialized data of the given requets.
         */
        @NotNull
        @Override
        public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.DeliveryRequest request)
        {
            final NBTTagCompound compound = new NBTTagCompound();

            final NBTTagCompound tokenCompound = controller.serialize(request.getToken());
            final NBTTagInt stateCompound = request.getState().serializeNBT();
            final NBTTagCompound requestedCompound = request.getRequest().serialize(controller);

            final NBTTagList childrenCompound = new NBTTagList();
            for (final IToken token : request.getChildren())
            {
                childrenCompound.appendTag(controller.serialize(token));
            }

            compound.setTag(NBT_TOKEN, tokenCompound);
            compound.setTag(NBT_STATE, stateCompound);
            compound.setTag(NBT_REQUESTED, requestedCompound);

            if (request.hasResult())
            {
                compound.setTag(NBT_RESULT, request.getResult().serialize(controller));
            }

            if (request.hasParent())
            {
                compound.setTag(NBT_PARENT, controller.serialize(request.getParent()));
            }

            compound.setTag(NBT_CHILDREN, childrenCompound);

            return compound;
        }

        /**
         * Method to deserialize a given constructable.
         *
         * @param controller The controller that can be used to deserialize complicated types.
         * @param nbt        The data of the request that should be deserialized.
         * @return The request that corresponds with the given data in the nbt
         */
        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public StandardRequests.DeliveryRequest deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            final IToken token = controller.deserialize(nbt.getCompoundTag(NBT_TOKEN));
            final RequestState state = RequestState.deserializeNBT((NBTTagInt) nbt.getTag(NBT_STATE));
            final Delivery requested = Delivery.deserialize(controller, nbt.getCompoundTag(NBT_REQUESTED));

            final List<IToken> childTokens = new ArrayList<>();
            final NBTTagList childCompound = nbt.getTagList(NBT_CHILDREN, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < childCompound.tagCount(); i++)
            {
                childTokens.add(controller.deserialize(childCompound.getCompoundTagAt(i)));
            }

            @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
            final StandardRequests.DeliveryRequest request = controller.getNewInstance(requested, new TypeToken<StandardRequests.DeliveryRequest>() {}, token, state);

            if (nbt.hasKey(NBT_PARENT))
            {
                request.setParent(controller.deserialize(nbt.getCompoundTag(NBT_PARENT)));
            }

            if (nbt.hasKey(NBT_RESULT))
            {
                request.setResult(Delivery.deserialize(controller, nbt.getCompoundTag(NBT_RESULT)));
            }

            return request;
        }

        /**
         * Method to get a new instance of a request given the input and token.
         *
         * @param input        The input to build a new request for.
         * @param location     The location of the requester.
         * @param token        The token to build the request from.
         * @param initialState The initial state of the request request.
         * @return The new output instance for a given input.
         */
        @Override
        public StandardRequests.DeliveryRequest getNewInstance(
                                                                @NotNull final Delivery input,
                                                                @NotNull final IRequester location,
                                                                @NotNull final IToken token,
                                                                @NotNull final RequestState initialState)
        {
            return new StandardRequests.DeliveryRequest(location, token, initialState, input);
        }
    }
}
