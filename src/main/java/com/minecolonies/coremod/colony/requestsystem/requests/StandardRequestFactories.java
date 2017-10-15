package com.minecolonies.coremod.colony.requestsystem.requests;

import com.minecolonies.api.colony.requestsystem.RequestState;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.IRequestFactory;
import com.minecolonies.api.colony.requestsystem.requestable.*;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Suppression;
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
    private static final String NBT_REQUESTER = "Requester";
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

    @FunctionalInterface
    public interface IObjectToNBTConverter<O>
    {
        NBTTagCompound apply(IFactoryController controller, O object);
    }
    
    @FunctionalInterface
    public interface INBTToObjectConverter<O>
    {
        O apply(IFactoryController controller, NBTTagCompound compound);
    }
    
    private static <T> NBTTagCompound serializeToNBT(IFactoryController controller, IRequest<T> request, IObjectToNBTConverter<T> typeSerialization)
    {
        final NBTTagCompound compound = new NBTTagCompound();

        final NBTTagCompound requesterCompound = controller.serialize(request.getRequester());
        final NBTTagCompound tokenCompound = controller.serialize(request.getToken());
        final NBTTagInt stateCompound = request.getState().serializeNBT();
        final NBTTagCompound requestedCompound = typeSerialization.apply(controller, request.getRequest());

        final NBTTagList childrenCompound = new NBTTagList();
        for (final IToken token : request.getChildren())
        {
            childrenCompound.appendTag(controller.serialize(token));
        }

        compound.setTag(NBT_REQUESTER, requesterCompound);
        compound.setTag(NBT_TOKEN, tokenCompound);
        compound.setTag(NBT_STATE, stateCompound);
        compound.setTag(NBT_REQUESTED, requestedCompound);

        if (request.hasResult())
        {
            compound.setTag(NBT_RESULT, typeSerialization.apply(controller, request.getResult()));
        }

        if (request.hasParent())
        {
            compound.setTag(NBT_PARENT, controller.serialize(request.getParent()));
        }

        compound.setTag(NBT_CHILDREN, childrenCompound);

        return compound;
    }
    
    private static <T, R extends IRequest<T>> R deserializeFromNBT(IFactoryController controller, NBTTagCompound compound, INBTToObjectConverter<T> typeDeserialization)
    {
        final IRequester requester = controller.deserialize(compound.getCompoundTag(NBT_REQUESTER));
        final IToken token = controller.deserialize(compound.getCompoundTag(NBT_TOKEN));
        final RequestState state = RequestState.deserializeNBT((NBTTagInt) compound.getTag(NBT_STATE));
        final T requested = typeDeserialization.apply(controller, compound.getCompoundTag(NBT_REQUESTED));

        final List<IToken> childTokens = new ArrayList<>();
        final NBTTagList childCompound = compound.getTagList(NBT_CHILDREN, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < childCompound.tagCount(); i++)
        {
            childTokens.add(controller.deserialize(childCompound.getCompoundTagAt(i)));
        }

        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        final R request = controller.getNewInstance(requested, token, requester, state);

        if (compound.hasKey(NBT_PARENT))
        {
            request.setParent(controller.deserialize(compound.getCompoundTag(NBT_PARENT)));
        }

        if (compound.hasKey(NBT_RESULT))
        {
            request.setResult(typeDeserialization.apply(controller, compound.getCompoundTag(NBT_RESULT)));
        }

        return request;
    }
    
    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class ItemStackRequestFactory implements IRequestFactory<Stack, StandardRequests.ItemStackRequest>
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
                                                                 @NotNull final Stack input,
                                                                 @NotNull final IRequester location,
                                                                 @NotNull final IToken token,
                                                                 @NotNull final RequestState initialState)
        {
            return new StandardRequests.ItemStackRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public Class<StandardRequests.ItemStackRequest> getFactoryOutputType()
        {
            return StandardRequests.ItemStackRequest.class;
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public Class<Stack> getFactoryInputType()
        {
            return Stack.class;
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
            return serializeToNBT(controller, request, Stack::serialize);    
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
            return deserializeFromNBT(controller, nbt, Stack::deserialize);
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class DeliveryRequestFactory implements IRequestFactory<Delivery, StandardRequests.DeliveryRequest>
    {

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public Class<StandardRequests.DeliveryRequest> getFactoryOutputType()
        {
            return StandardRequests.DeliveryRequest.class;
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public Class<Delivery> getFactoryInputType()
        {
            return Delivery.class;
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
            return serializeToNBT(controller, request, Delivery::serialize);
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
            return deserializeFromNBT(controller, nbt, Delivery::deserialize);
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

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class ToolRequestFactory implements IRequestFactory<Tool, StandardRequests.ToolRequest>
    {

        @Override
        public StandardRequests.ToolRequest getNewInstance(
                                                            @NotNull final Tool input,
                                                            @NotNull final IRequester location,
                                                            @NotNull final IToken token,
                                                            @NotNull final RequestState initialState)
        {
            return new StandardRequests.ToolRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        public Class<? extends StandardRequests.ToolRequest> getFactoryOutputType()
        {
            return StandardRequests.ToolRequest.class;
        }

        @NotNull
        @Override
        public Class<? extends Tool> getFactoryInputType()
        {
            return Tool.class;
        }

        @NotNull
        @Override
        public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ToolRequest request)
        {
            return serializeToNBT(controller, request, Tool::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.ToolRequest deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            return deserializeFromNBT(controller, nbt, Tool::deserialize);
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class FoodRequestFactory implements IRequestFactory<Food, StandardRequests.FoodRequest>
    {

        @Override
        public StandardRequests.FoodRequest getNewInstance(
                                                            @NotNull final Food input,
                                                            @NotNull final IRequester location,
                                                            @NotNull final IToken token,
                                                            @NotNull final RequestState initialState)
        {
            return new StandardRequests.FoodRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        public Class<? extends StandardRequests.FoodRequest> getFactoryOutputType()
        {
            return StandardRequests.FoodRequest.class;
        }

        @NotNull
        @Override
        public Class<? extends Food> getFactoryInputType()
        {
            return Food.class;
        }

        @NotNull
        @Override
        public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.FoodRequest request)
        {
            return serializeToNBT(controller, request, Food::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.FoodRequest deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            return deserializeFromNBT(controller, nbt, Food::deserialize);
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class BurnableRequestFactory implements IRequestFactory<Burnable, StandardRequests.BurnableRequest>
    {

        @Override
        public StandardRequests.BurnableRequest getNewInstance(
                                                            @NotNull final Burnable input,
                                                            @NotNull final IRequester location,
                                                            @NotNull final IToken token,
                                                            @NotNull final RequestState initialState)
        {
            return new StandardRequests.BurnableRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        public Class<? extends StandardRequests.BurnableRequest> getFactoryOutputType()
        {
            return StandardRequests.BurnableRequest.class;
        }

        @NotNull
        @Override
        public Class<? extends Burnable> getFactoryInputType()
        {
            return Burnable.class;
        }

        @NotNull
        @Override
        public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.BurnableRequest request)
        {
            return serializeToNBT(controller, request, Burnable::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.BurnableRequest deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            return deserializeFromNBT(controller, nbt, Burnable::deserialize);
        }
    }


}
