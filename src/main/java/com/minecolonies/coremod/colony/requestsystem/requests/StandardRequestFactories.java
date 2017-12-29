package com.minecolonies.coremod.colony.requestsystem.requests;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.IRequestFactory;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.*;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
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
                                                                 @NotNull final IToken<?> token,
                                                                 @NotNull final RequestState initialState)
        {
            return new StandardRequests.ItemStackRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        public TypeToken<StandardRequests.ItemStackRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.ItemStackRequest.class);
        }

        @NotNull
        @Override
        public TypeToken<Stack> getFactoryInputType()
        {
            return TypeToken.of(Stack.class);
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
        public StandardRequests.ItemStackRequest deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            return deserializeFromNBT(controller, nbt, Stack::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ItemStackRequest.class),
                requested,
                token,
                requester,
                requestState));
        }
    }

    public static final class DeliveryRequestFactory implements IRequestFactory<Delivery, StandardRequests.DeliveryRequest>
    {

        @NotNull
        @Override
        public TypeToken<StandardRequests.DeliveryRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.DeliveryRequest.class);
        }

        @NotNull
        @Override
        public TypeToken<Delivery> getFactoryInputType()
        {
            return TypeToken.of(Delivery.class);
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
        public StandardRequests.DeliveryRequest deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            return deserializeFromNBT(controller, nbt, Delivery::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.DeliveryRequest.class),
                requested,
                token,
                requester,
                requestState));
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
                                                                @NotNull final IToken<?> token,
                                                                @NotNull final RequestState initialState)
        {
            return new StandardRequests.DeliveryRequest(location, token, initialState, input);
        }
    }

    public static final class ToolRequestFactory implements IRequestFactory<Tool, StandardRequests.ToolRequest>
    {

        @Override
        public StandardRequests.ToolRequest getNewInstance(
                                                            @NotNull final Tool input,
                                                            @NotNull final IRequester location,
                                                            @NotNull final IToken<?> token,
                                                            @NotNull final RequestState initialState)
        {
            return new StandardRequests.ToolRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        public TypeToken<? extends StandardRequests.ToolRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.ToolRequest.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends Tool> getFactoryInputType()
        {
            return TypeToken.of(Tool.class);
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
            return deserializeFromNBT(controller, nbt, Tool::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ToolRequest.class),
                requested,
                token,
                requester,
                requestState));
        }
    }

    public static final class FoodRequestFactory implements IRequestFactory<Food, StandardRequests.FoodRequest>
    {

        @Override
        public StandardRequests.FoodRequest getNewInstance(
                                                            @NotNull final Food input,
                                                            @NotNull final IRequester location,
                                                            @NotNull final IToken<?> token,
                                                            @NotNull final RequestState initialState)
        {
            return new StandardRequests.FoodRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        public TypeToken<? extends StandardRequests.FoodRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.FoodRequest.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends Food> getFactoryInputType()
        {
            return TypeToken.of(Food.class);
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
            return deserializeFromNBT(controller, nbt, Food::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.FoodRequest.class),
                requested,
                token,
                requester,
                requestState));
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class SmeltableOreRequestFactory implements IRequestFactory<SmeltableOre, StandardRequests.SmeltAbleOreRequest>
    {

        @Override
        public StandardRequests.SmeltAbleOreRequest getNewInstance(
                @NotNull final SmeltableOre input,
                @NotNull final IRequester location,
                @NotNull final IToken token,
                @NotNull final RequestState initialState)
        {
            return new StandardRequests.SmeltAbleOreRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        public TypeToken<? extends StandardRequests.SmeltAbleOreRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.SmeltAbleOreRequest.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends SmeltableOre> getFactoryInputType()
        {
            return TypeToken.of(SmeltableOre.class);
        }

        @NotNull
        @Override
        public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.SmeltAbleOreRequest request)
        {
            return serializeToNBT(controller, request, SmeltableOre::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.SmeltAbleOreRequest deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            return deserializeFromNBT(controller, nbt, SmeltableOre::deserialize,
                    (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.SmeltAbleOreRequest.class),
                            requested,
                            token,
                            requester,
                            requestState));
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class BurnableRequestFactory implements IRequestFactory<Burnable, StandardRequests.BurnableRequest>
    {

        @Override
        public StandardRequests.BurnableRequest getNewInstance(
                                                                @NotNull final Burnable input,
                                                                @NotNull final IRequester location,
                                                                @NotNull final IToken<?> token,
                                                                @NotNull final RequestState initialState)
        {
            return new StandardRequests.BurnableRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        public TypeToken<? extends StandardRequests.BurnableRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.BurnableRequest.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends Burnable> getFactoryInputType()
        {
            return TypeToken.of(Burnable.class);
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
            return deserializeFromNBT(controller, nbt, Burnable::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.BurnableRequest.class),
                requested,
                token,
                requester,
                requestState));
        }
    }

    public static <T extends IRequestable> NBTTagCompound serializeToNBT(final IFactoryController controller, final IRequest<T> request, final IObjectToNBTConverter<T> typeSerialization)
    {
        final NBTTagCompound compound = new NBTTagCompound();

        final NBTTagCompound requesterCompound = controller.serialize(request.getRequester());
        final NBTTagCompound tokenCompound = controller.serialize(request.getToken());
        final NBTTagInt stateCompound = request.getState().serializeNBT();
        final NBTTagCompound requestedCompound = typeSerialization.apply(controller, request.getRequest());

        final NBTTagList childrenCompound = new NBTTagList();
        for (final IToken<?> token : request.getChildren())
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

    public static <T extends IRequestable, R extends IRequest<T>> R deserializeFromNBT(
                                                                                        final IFactoryController controller,
                                                                                        final NBTTagCompound compound,
                                                                                        final INBTToObjectConverter<T> typeDeserialization,
                                                                                        final IObjectConstructor<T, R> objectConstructor)
    {
        final IRequester requester = controller.deserialize(compound.getCompoundTag(NBT_REQUESTER));
        final IToken<?> token = controller.deserialize(compound.getCompoundTag(NBT_TOKEN));
        final RequestState state = RequestState.deserializeNBT((NBTTagInt) compound.getTag(NBT_STATE));
        final T requested = typeDeserialization.apply(controller, compound.getCompoundTag(NBT_REQUESTED));

        final List<IToken<?>> childTokens = new ArrayList<>();
        final NBTTagList childCompound = compound.getTagList(NBT_CHILDREN, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < childCompound.tagCount(); i++)
        {
            childTokens.add(controller.deserialize(childCompound.getCompoundTagAt(i)));
        }

        final R request = objectConstructor.construct(requested, token, requester, state);

        request.addChildren(childTokens);

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

    @FunctionalInterface
    public interface IObjectConstructor<T, O>
    {
        O construct(@NotNull final T requested, @NotNull final IToken<?> token, @NotNull final IRequester requester, @NotNull final RequestState requestState);
    }
}
