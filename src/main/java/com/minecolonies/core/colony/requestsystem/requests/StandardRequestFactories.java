package com.minecolonies.core.colony.requestsystem.requests;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.IRequestFactory;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.*;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.AbstractCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PrivateCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Pickup;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.requestable.SmeltableOre;
import com.minecolonies.core.colony.requestsystem.requests.StandardRequests.*;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Final class holding all the factories for requestables inside minecolonie
 */
public final class StandardRequestFactories
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_REQUESTER  = "Requester";
    private static final String NBT_TOKEN      = "Token";
    private static final String NBT_STATE      = "State";
    private static final String NBT_REQUESTED  = "Requested";
    private static final String NBT_RESULT     = "Result";
    private static final String NBT_PARENT     = "Parent";
    private static final String NBT_CHILDREN   = "Children";
    private static final String NBT_DELIVERIES = "Deliveries";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

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
          @NotNull final IToken<?> token,
          @NotNull final RequestState initialState)
        {
            return new StandardRequests.ItemStackRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<StandardRequests.ItemStackRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.ItemStackRequest.class);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
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
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ItemStackRequest request)
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
        public StandardRequests.ItemStackRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            return deserializeFromNBT(controller, nbt, Stack::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ItemStackRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @NotNull
        @Override
        public void serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ItemStackRequest input, final FriendlyByteBuf packetBuffer)
        {
            serializeToFriendlyByteBuf(controller, input, packetBuffer, Stack::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.ItemStackRequest deserialize(@NotNull final IFactoryController controller, @NotNull final FriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromFriendlyByteBuf(controller,
              buffer,
              Stack::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ItemStackRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.ITEM_STACK_REQUEST_ID;
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class ItemStackListRequestFactory implements IRequestFactory<StackList, StandardRequests.ItemStackListRequest>
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
        public StandardRequests.ItemStackListRequest getNewInstance(
          @NotNull final StackList input,
          @NotNull final IRequester location,
          @NotNull final IToken<?> token,
          @NotNull final RequestState initialState)
        {
            return new StandardRequests.ItemStackListRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<StandardRequests.ItemStackListRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.ItemStackListRequest.class);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<StackList> getFactoryInputType()
        {
            return TypeToken.of(StackList.class);
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
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ItemStackListRequest request)
        {
            return serializeToNBT(controller, request, StackList::serialize);
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
        public StandardRequests.ItemStackListRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            return deserializeFromNBT(controller, nbt, StackList::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ItemStackListRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public void serialize(IFactoryController controller, ItemStackListRequest input, FriendlyByteBuf packetBuffer)
        {
            serializeToFriendlyByteBuf(controller, input, packetBuffer, StackList::serialize);
        }

        @Override
        public ItemStackListRequest deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromFriendlyByteBuf(controller,
              buffer,
              StackList::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ItemStackListRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.ITEM_STACK_LIST_REQUEST_ID;
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class ItemTagRequestFactory implements IRequestFactory<RequestTag, StandardRequests.ItemTagRequest>
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
        public StandardRequests.ItemTagRequest getNewInstance(
          @NotNull final RequestTag input,
          @NotNull final IRequester location,
          @NotNull final IToken<?> token,
          @NotNull final RequestState initialState)
        {
            return new StandardRequests.ItemTagRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<StandardRequests.ItemTagRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.ItemTagRequest.class);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<RequestTag> getFactoryInputType()
        {
            return TypeToken.of(RequestTag.class);
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
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ItemTagRequest request)
        {
            return serializeToNBT(controller, request, RequestTag::serialize);
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
        public StandardRequests.ItemTagRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            return deserializeFromNBT(controller, nbt, RequestTag::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ItemTagRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @NotNull
        @Override
        public void serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ItemTagRequest itemTagRequest, final FriendlyByteBuf packetBuffer)
        {
            serializeToFriendlyByteBuf(controller, itemTagRequest, packetBuffer, RequestTag::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.ItemTagRequest deserialize(@NotNull final IFactoryController controller, @NotNull final FriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromFriendlyByteBuf(controller,
              buffer,
              RequestTag::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ItemTagRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.ITEM_TAG_REQUEST_ID;
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class DeliveryRequestFactory implements IRequestFactory<Delivery, StandardRequests.DeliveryRequest>
    {

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<StandardRequests.DeliveryRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.DeliveryRequest.class);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<Delivery> getFactoryInputType()
        {
            return TypeConstants.DELIVERY;
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
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.DeliveryRequest request)
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
        public StandardRequests.DeliveryRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
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

        @Override
        public void serialize(IFactoryController controller, DeliveryRequest input, FriendlyByteBuf packetBuffer)
        {
            serializeToFriendlyByteBuf(controller, input, packetBuffer, Delivery::serialize);
        }

        @Override
        public DeliveryRequest deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromFriendlyByteBuf(controller,
              buffer,
              Delivery::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.DeliveryRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.DELIVERY_REQUEST_ID;
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class PickupRequestFactory implements IRequestFactory<Pickup, StandardRequests.PickupRequest>
    {

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<StandardRequests.PickupRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.PickupRequest.class);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<Pickup> getFactoryInputType()
        {
            return TypeConstants.PICKUP;
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
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.PickupRequest request)
        {
            return serializeToNBT(controller, request, Pickup::serialize);
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
        public StandardRequests.PickupRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            return deserializeFromNBT(controller, nbt, Pickup::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.PickupRequest.class),
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
        public StandardRequests.PickupRequest getNewInstance(
          @NotNull final Pickup input,
          @NotNull final IRequester location,
          @NotNull final IToken<?> token,
          @NotNull final RequestState initialState)
        {
            return new StandardRequests.PickupRequest(location, token, initialState, input);
        }

        @Override
        public void serialize(IFactoryController controller, PickupRequest input, FriendlyByteBuf packetBuffer)
        {
            serializeToFriendlyByteBuf(controller, input, packetBuffer, Pickup::serialize);
        }

        @Override
        public PickupRequest deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromFriendlyByteBuf(controller,
              buffer,
              Pickup::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.PickupRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.PICKUP_REQUEST_ID;
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static abstract class AbstractCraftingRequestFactory<C extends AbstractCrafting, R extends StandardRequests.AbstractCraftingRequest<C>>
      implements IRequestFactory<C, R>
    {
        private final IObjectConstructor<C, R>       constructor;
        private final Class<C>                       cClass;
        private final Class<R>                       rClass;
        private final IObjectToNBTConverter<C>       nbtSerializer;
        private final INBTToObjectConverter<C>       nbtDeserializer;
        private final IObjectToPackBufferWriter<C>   packetSerializer;
        private final IFriendlyByteBufToObjectReader<C> packetDeserializer;

        protected AbstractCraftingRequestFactory(
          final IObjectConstructor<C, R> constructor,
          final Class<C> cClass,
          final Class<R> rClass,
          final IObjectToNBTConverter<C> nbtSerializer,
          final INBTToObjectConverter<C> nbtDeserializer,
          final IObjectToPackBufferWriter<C> packetSerializer,
          final IFriendlyByteBufToObjectReader<C> packetDeserializer)
        {
            this.constructor = constructor;
            this.cClass = cClass;
            this.rClass = rClass;
            this.nbtSerializer = nbtSerializer;
            this.nbtDeserializer = nbtDeserializer;
            this.packetSerializer = packetSerializer;
            this.packetDeserializer = packetDeserializer;
        }

        @Override
        public R getNewInstance(
          @NotNull final C input, @NotNull final IRequester location, @NotNull final IToken<?> token, @NotNull final RequestState initialState)
        {
            return constructor.construct(input, token, location, initialState);
        }

        @NotNull
        @Override
        public TypeToken<? extends R> getFactoryOutputType()
        {
            return TypeToken.of(rClass);
        }

        @NotNull
        @Override
        public TypeToken<? extends C> getFactoryInputType()
        {
            return TypeToken.of(cClass);
        }

        @NotNull
        @Override
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final R r)
        {
            return serializeToNBT(controller, r, nbtSerializer);
        }

        @NotNull
        @Override
        public R deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt) throws Throwable
        {
            return deserializeFromNBT(controller, nbt, nbtDeserializer, (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(rClass),
              requested,
              token,
              requester,
              requestState));
        }

        @Override
        public void serialize(IFactoryController controller, R input, FriendlyByteBuf packetBuffer)
        {
            serializeToFriendlyByteBuf(controller, input, packetBuffer, packetSerializer);
        }

        @Override
        public R deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromFriendlyByteBuf(controller,
              buffer,
              packetDeserializer,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(rClass),
                requested,
                token,
                requester,
                requestState));
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class PrivateCraftingRequestFactory
      extends StandardRequestFactories.AbstractCraftingRequestFactory<PrivateCrafting, StandardRequests.PrivateCraftingRequest>
    {

        public PrivateCraftingRequestFactory()
        {
            super((requested, token, requester, requestState) -> new StandardRequests.PrivateCraftingRequest(requester, token, requestState, requested),
              PrivateCrafting.class,
              StandardRequests.PrivateCraftingRequest.class,
              PrivateCrafting::serialize,
              PrivateCrafting::deserialize,
              PrivateCrafting::serialize,
              PrivateCrafting::deserialize);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.PRIVATE_CRAFTING_REQUEST_ID;
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class PublicCraftingRequestFactory extends StandardRequestFactories.AbstractCraftingRequestFactory<PublicCrafting, StandardRequests.PublicCraftingRequest>
    {

        public PublicCraftingRequestFactory()
        {
            super((requested, token, requester, requestState) -> new StandardRequests.PublicCraftingRequest(requester, token, requestState, requested),
              PublicCrafting.class,
              StandardRequests.PublicCraftingRequest.class,
              PublicCrafting::serialize,
              PublicCrafting::deserialize,
              PublicCrafting::serialize,
              PublicCrafting::deserialize);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.PUBLIC_CRAFTING_REQUEST_ID;
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
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
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ToolRequest request)
        {
            return serializeToNBT(controller, request, Tool::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.ToolRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            return deserializeFromNBT(controller, nbt, Tool::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ToolRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public void serialize(IFactoryController controller, ToolRequest input, FriendlyByteBuf packetBuffer)
        {
            serializeToFriendlyByteBuf(controller, input, packetBuffer, Tool::serialize);
        }

        @Override
        public ToolRequest deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromFriendlyByteBuf(controller,
              buffer,
              Tool::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ToolRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.TOOL_REQUEST_ID;
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
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
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.FoodRequest request)
        {
            return serializeToNBT(controller, request, Food::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.FoodRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            return deserializeFromNBT(controller, nbt, Food::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.FoodRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public void serialize(IFactoryController controller, FoodRequest input, FriendlyByteBuf packetBuffer)
        {
            serializeToFriendlyByteBuf(controller, input, packetBuffer, Food::serialize);
        }

        @Override
        public FoodRequest deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromFriendlyByteBuf(controller,
              buffer,
              Food::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.FoodRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.FOOD_REQUEST_ID;
        }
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static final class SmeltableOreRequestFactory implements IRequestFactory<SmeltableOre, StandardRequests.SmeltAbleOreRequest>
    {

        @Override
        public StandardRequests.SmeltAbleOreRequest getNewInstance(
          @NotNull final SmeltableOre input,
          @NotNull final IRequester location,
          @NotNull final IToken<?> token,
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
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.SmeltAbleOreRequest request)
        {
            return serializeToNBT(controller, request, SmeltableOre::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.SmeltAbleOreRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            return deserializeFromNBT(controller, nbt, SmeltableOre::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.SmeltAbleOreRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public void serialize(IFactoryController controller, SmeltAbleOreRequest input, FriendlyByteBuf packetBuffer)
        {
            serializeToFriendlyByteBuf(controller, input, packetBuffer, SmeltableOre::serialize);
        }

        @Override
        public SmeltAbleOreRequest deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromFriendlyByteBuf(controller,
              buffer,
              SmeltableOre::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.SmeltAbleOreRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.SMELTABLE_ORE_REQUEST_ID;
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
        public CompoundTag serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.BurnableRequest request)
        {
            return serializeToNBT(controller, request, Burnable::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.BurnableRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            return deserializeFromNBT(controller, nbt, Burnable::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.BurnableRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public void serialize(IFactoryController controller, BurnableRequest input, FriendlyByteBuf packetBuffer)
        {
            serializeToFriendlyByteBuf(controller, input, packetBuffer, Burnable::serialize);
        }

        @Override
        public BurnableRequest deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromFriendlyByteBuf(controller,
              buffer,
              Burnable::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.BurnableRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.BURNABLE_REQUEST_ID;
        }
    }

    public static final class MinimumStackRequestFactory implements IRequestFactory<MinimumStack, StandardRequests.MinStackRequest>
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
        public StandardRequests.MinStackRequest getNewInstance(
          @NotNull final MinimumStack input,
          @NotNull final IRequester location,
          @NotNull final IToken<?> token,
          @NotNull final RequestState initialState)
        {
            return new StandardRequests.MinStackRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<StandardRequests.MinStackRequest> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequests.MinStackRequest.class);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<MinimumStack> getFactoryInputType()
        {
            return TypeToken.of(MinimumStack.class);
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
        public CompoundTag serialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final StandardRequests.MinStackRequest request)
        {
            return serializeToNBT(provider, controller, request, Stack::serialize);
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
        public StandardRequests.MinStackRequest deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            return deserializeFromNBT(provider, controller, nbt, MinimumStack::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.MinStackRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @NotNull
        @Override
        public void serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.MinStackRequest input, final RegistryFriendlyByteBuf packetBuffer)
        {
            serializeToRegistryFriendlyByteBuf(controller, input, packetBuffer, Stack::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.MinStackRequest deserialize(@NotNull final IFactoryController controller, @NotNull final RegistryFriendlyByteBuf buffer) throws Throwable
        {
            return deserializeFromRegistryFriendlyByteBuf(controller,
              buffer,
              MinimumStack::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.MinStackRequest.class),
                requested,
                token,
                requester,
                requestState));
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.MIN_STACK_REQUEST_ID;
        }
    }

    /**
     * Private constructor to hide the implicit public one.
     */
    private StandardRequestFactories()
    {
    }

    public static <T extends IRequestable> CompoundTag serializeToNBT(
      final IFactoryController controller,
      final IRequest<T> request,
      final IObjectToNBTConverter<T> typeSerialization)
    {
        final CompoundTag compound = new CompoundTag();

        final CompoundTag requesterCompound = controller.serialize(request.getRequester());
        final CompoundTag tokenCompound = controller.serialize(request.getId());
        final IntTag stateCompound = request.getState().serialize();
        final CompoundTag requestedCompound = typeSerialization.apply(controller, request.getRequest());

        final ListTag childrenCompound = new ListTag();
        for (final IToken<?> token : request.getChildren())
        {
            childrenCompound.add(controller.serialize(token));
        }

        compound.put(NBT_REQUESTER, requesterCompound);
        compound.put(NBT_TOKEN, tokenCompound);
        compound.put(NBT_STATE, stateCompound);
        compound.put(NBT_REQUESTED, requestedCompound);

        if (request.hasResult())
        {
            compound.put(NBT_RESULT, typeSerialization.apply(controller, request.getResult()));
        }

        if (request.hasParent())
        {
            compound.put(NBT_PARENT, controller.serialize(request.getParent()));
        }

        compound.put(NBT_CHILDREN, childrenCompound);

        final ListTag deliveriesList = new ListTag();
        request.getDeliveries().forEach(itemStack -> deliveriesList.add(itemStack.save(new CompoundTag())));

        compound.put(NBT_DELIVERIES, deliveriesList);

        return compound;
    }

    public static <T extends IRequestable> void serializeToFriendlyByteBuf(
      final IFactoryController controller,
      final IRequest<T> request,
      final FriendlyByteBuf packetBuffer,
      final IObjectToPackBufferWriter<T> typeSerialization)
    {

        controller.serialize(packetBuffer, request.getRequester());
        controller.serialize(packetBuffer, request.getId());
        request.getState().serialize(packetBuffer);
        typeSerialization.apply(controller, packetBuffer, request.getRequest());

        packetBuffer.writeInt(request.getChildren().size());
        for (final IToken<?> token : request.getChildren())
        {
            controller.serialize(packetBuffer, token);
        }

        packetBuffer.writeBoolean(request.hasResult());
        if (request.hasResult())
        {
            typeSerialization.apply(controller, packetBuffer, request.getResult());
        }

        packetBuffer.writeBoolean(request.hasParent());
        if (request.hasParent())
        {
            controller.serialize(packetBuffer, request.getParent());
        }

        packetBuffer.writeInt(request.getDeliveries().size());
        request.getDeliveries().forEach(packetBuffer::writeItem);
    }

    public static <T extends IRequestable, R extends IRequest<T>> R deserializeFromNBT(
      final IFactoryController controller,
      final CompoundTag compound,
      final INBTToObjectConverter<T> typeDeserialization,
      final IObjectConstructor<T, R> objectConstructor)
    {
        final IRequester requester = controller.deserialize(compound.getCompound(NBT_REQUESTER));
        final IToken<?> token = controller.deserialize(compound.getCompound(NBT_TOKEN));
        final RequestState state = RequestState.deserialize((IntTag) compound.get(NBT_STATE));
        final T requested = typeDeserialization.apply(controller, compound.getCompound(NBT_REQUESTED));

        final List<IToken<?>> childTokens = new ArrayList<>();
        final ListTag childCompound = compound.getList(NBT_CHILDREN, Tag.TAG_COMPOUND);
        for (int i = 0; i < childCompound.size(); i++)
        {
            childTokens.add(controller.deserialize(childCompound.getCompound(i)));
        }

        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE) final R request = objectConstructor.construct(requested, token, requester, state);

        request.addChildren(childTokens);

        if (compound.contains(NBT_PARENT))
        {
            request.setParent(controller.deserialize(compound.getCompound(NBT_PARENT)));
        }

        if (compound.contains(NBT_RESULT))
        {
            request.setResult(typeDeserialization.apply(controller, compound.getCompound(NBT_RESULT)));
        }

        if (compound.contains(NBT_DELIVERIES))
        {
            final ImmutableList.Builder<ItemStack> stackBuilder = ImmutableList.builder();
            final ListTag deliveriesList = compound.getList(NBT_DELIVERIES, Tag.TAG_COMPOUND);
            NBTUtils.streamCompound(deliveriesList).forEach(itemStackCompound -> stackBuilder.add(ItemStack.of(itemStackCompound)));

            request.overrideCurrentDeliveries(stackBuilder.build());
        }

        return request;
    }

    public static <T extends IRequestable, R extends IRequest<T>> R deserializeFromFriendlyByteBuf(
      final IFactoryController controller,
      final FriendlyByteBuf buffer,
      final IFriendlyByteBufToObjectReader<T> typeDeserialization,
      final IObjectConstructor<T, R> objectConstructor)
    {
        final IRequester requester = controller.deserialize(buffer);
        final IToken<?> token = controller.deserialize(buffer);
        final RequestState state = RequestState.deserialize(buffer);
        final T requested = typeDeserialization.apply(controller, buffer);

        final List<IToken<?>> childTokens = new ArrayList<>();
        final int size = buffer.readInt();
        for (int i = 0; i < size; i++)
        {
            childTokens.add(controller.deserialize(buffer));
        }

        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE) final R request = objectConstructor.construct(requested, token, requester, state);
        request.addChildren(childTokens);

        if (buffer.readBoolean())
        {
            request.setResult(typeDeserialization.apply(controller, buffer));
        }

        if (buffer.readBoolean())
        {
            request.setParent(controller.deserialize(buffer));
        }

        final List<ItemStack> deliveries = new ArrayList<>();
        final int size2 = buffer.readInt();
        for (int i = 0; i < size2; i++)
        {
            deliveries.add(buffer.readItem());
        }

        request.overrideCurrentDeliveries(ImmutableList.copyOf(deliveries));
        return request;
    }

    @FunctionalInterface
    public interface IObjectToNBTConverter<O>
    {
        CompoundTag apply(IFactoryController controller, O object);
    }

    @FunctionalInterface
    public interface INBTToObjectConverter<O>
    {
        O apply(IFactoryController controller, CompoundTag compound);
    }

    @FunctionalInterface
    public interface IObjectToPackBufferWriter<O>
    {
        void apply(IFactoryController controller, FriendlyByteBuf buffer, O input);
    }

    @FunctionalInterface
    public interface IFriendlyByteBufToObjectReader<O>
    {
        O apply(IFactoryController controller, FriendlyByteBuf buffer);
    }

    @FunctionalInterface
    public interface IObjectConstructor<T, O>
    {
        O construct(@NotNull final T requested, @NotNull final IToken<?> token, @NotNull final IRequester requester, @NotNull final RequestState requestState);
    }
}
