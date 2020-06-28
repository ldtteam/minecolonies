package com.minecolonies.coremod.colony.requestsystem.requests;

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
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestable.SmeltableOre;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;

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
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ItemStackRequest request)
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
        public StandardRequests.ItemStackRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
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
        public void serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ItemStackRequest itemStackRequest, final PacketBuffer packetBuffer) {
            serializeToPacketBuffer(controller, itemStackRequest, packetBuffer, Stack::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.ItemStackRequest deserialize(@NotNull final IFactoryController controller, @NotNull final PacketBuffer buffer) throws Throwable {
            return deserializeFromPacketBuffer(controller, buffer, Stack::deserialize, (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ItemStackRequest.class),
                            requested,
                            token,
                            requester,
                            requestState));
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
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ItemStackListRequest request)
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
        public StandardRequests.ItemStackListRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            return deserializeFromNBT(controller, nbt, StackList::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ItemStackListRequest.class),
                requested,
                token,
                requester,
                requestState));
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
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.DeliveryRequest request)
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
        public StandardRequests.DeliveryRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
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
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.PickupRequest request)
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
        public StandardRequests.PickupRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
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
    }

    @SuppressWarnings(Suppression.BIG_CLASS)
    public static abstract class AbstractCraftingRequestFactory<C extends AbstractCrafting, R extends StandardRequests.AbstractCraftingRequest<C>> implements IRequestFactory<C, R>
    {
        private final IObjectConstructor<C, R> constructor;
        private final Class<C>                 cClass;
        private final Class<R>                 rClass;
        private final IObjectToNBTConverter<C> serializer;
        private final INBTToObjectConverter<C> deserializer;

        protected AbstractCraftingRequestFactory(
          final IObjectConstructor<C, R> constructor,
          final Class<C> cClass,
          final Class<R> rClass,
          final IObjectToNBTConverter<C> serializer, final INBTToObjectConverter<C> deserializer)
        {
            this.constructor = constructor;
            this.cClass = cClass;
            this.rClass = rClass;
            this.serializer = serializer;
            this.deserializer = deserializer;
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
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final R r)
        {
            return serializeToNBT(controller, r, serializer);
        }

        @NotNull
        @Override
        public R deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt) throws Throwable
        {
            return deserializeFromNBT(controller, nbt, deserializer, (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(rClass),
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
              PrivateCrafting::deserialize);
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
              PublicCrafting::deserialize);
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
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.ToolRequest request)
        {
            return serializeToNBT(controller, request, Tool::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.ToolRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            return deserializeFromNBT(controller, nbt, Tool::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.ToolRequest.class),
                requested,
                token,
                requester,
                requestState));
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
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.FoodRequest request)
        {
            return serializeToNBT(controller, request, Food::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.FoodRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
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
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.SmeltAbleOreRequest request)
        {
            return serializeToNBT(controller, request, SmeltableOre::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.SmeltAbleOreRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
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
        public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final StandardRequests.BurnableRequest request)
        {
            return serializeToNBT(controller, request, Burnable::serialize);
        }

        @NotNull
        @Override
        public StandardRequests.BurnableRequest deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
        {
            return deserializeFromNBT(controller, nbt, Burnable::deserialize,
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StandardRequests.BurnableRequest.class),
                requested,
                token,
                requester,
                requestState));
        }
    }

    /**
     * Private constructor to hide the implicit public one.
     */
    private StandardRequestFactories()
    {
    }

    public static <T extends IRequestable> CompoundNBT serializeToNBT(
      final IFactoryController controller,
      final IRequest<T> request,
      final IObjectToNBTConverter<T> typeSerialization)
    {
        final CompoundNBT compound = new CompoundNBT();

        final CompoundNBT requesterCompound = controller.serialize(request.getRequester());
        final CompoundNBT tokenCompound = controller.serialize(request.getId());
        final IntNBT stateCompound = request.getState().serialize();
        final CompoundNBT requestedCompound = typeSerialization.apply(controller, request.getRequest());

        final ListNBT childrenCompound = new ListNBT();
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

        final ListNBT deliveriesList = new ListNBT();
        request.getDeliveries().forEach(itemStack -> deliveriesList.add(itemStack.write(new CompoundNBT())));

        compound.put(NBT_DELIVERIES, deliveriesList);

        return compound;
    }

    public static <T extends IRequestable> void serializeToPacketBuffer(
                    final IFactoryController controller,
                    final IRequest<T> request,
                    final PacketBuffer packetBuffer,
                    final IObjectToPackBufferWriter<T> typeSerialization)
    {

        controller.serialize(packetBuffer, request.getRequester());
        controller.serialize(packetBuffer, request.getId());
        request.getState().serialize(packetBuffer);
        typeSerialization.apply(controller, request.getRequest(), packetBuffer);

        packetBuffer.writeInt(request.getChildren().size());
        for (final IToken<?> token : request.getChildren())
        {
            controller.serialize(packetBuffer, token);
        }

        packetBuffer.writeBoolean(request.hasResult());
        if (request.hasResult())
        {
            typeSerialization.apply(controller, request.getResult(), packetBuffer);
        }

        packetBuffer.writeBoolean(request.hasParent());
        if (request.hasParent())
        {
            controller.serialize(packetBuffer, request.getParent());
        }

        packetBuffer.writeInt(request.getDeliveries().size());
        request.getDeliveries().forEach(packetBuffer::writeItemStack);
    }

    public static <T extends IRequestable, R extends IRequest<T>> R deserializeFromNBT(
      final IFactoryController controller,
      final CompoundNBT compound,
      final INBTToObjectConverter<T> typeDeserialization,
      final IObjectConstructor<T, R> objectConstructor)
    {
        final IRequester requester = controller.deserialize(compound.getCompound(NBT_REQUESTER));
        final IToken<?> token = controller.deserialize(compound.getCompound(NBT_TOKEN));
        final RequestState state = RequestState.deserialize((IntNBT) compound.get(NBT_STATE));
        final T requested = typeDeserialization.apply(controller, compound.getCompound(NBT_REQUESTED));

        final List<IToken<?>> childTokens = new ArrayList<>();
        final ListNBT childCompound = compound.getList(NBT_CHILDREN, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < childCompound.size(); i++)
        {
            childTokens.add(controller.deserialize(childCompound.getCompound(i)));
        }

        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE) final R request = objectConstructor.construct(requested, token, requester, state);

        request.addChildren(childTokens);

        if (compound.keySet().contains(NBT_PARENT))
        {
            request.setParent(controller.deserialize(compound.getCompound(NBT_PARENT)));
        }

        if (compound.keySet().contains(NBT_RESULT))
        {
            request.setResult(typeDeserialization.apply(controller, compound.getCompound(NBT_RESULT)));
        }

        if (compound.keySet().contains(NBT_DELIVERIES))
        {
            final ImmutableList.Builder<ItemStack> stackBuilder = ImmutableList.builder();
            final ListNBT deliveriesList = compound.getList(NBT_DELIVERIES, Constants.NBT.TAG_COMPOUND);
            NBTUtils.streamCompound(deliveriesList).forEach(itemStackCompound -> stackBuilder.add(ItemStack.read(itemStackCompound)));

            request.overrideCurrentDeliveries(stackBuilder.build());
        }

        return request;
    }

    public static <T extends IRequestable, R extends IRequest<T>> R deserializeFromPacketBuffer(
                    final IFactoryController controller,
                    final PacketBuffer buffer,
                    final IPacketBufferToObjectReader<T> typeDeserialization,
                    final IObjectConstructor<T, R> objectConstructor)
    {
        final IRequester requester = controller.deserialize(buffer);
        final IToken<?> token = controller.deserialize(buffer);
        final RequestState state = RequestState.deserialize(buffer);
        final T requested = typeDeserialization.apply(controller, buffer);

        final List<IToken<?>> childTokens = new ArrayList<>();
        for (int i = 0; i < buffer.readInt(); i++)
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
        for (int i = 0; i < buffer.readInt(); i++)
        {
            deliveries.add(buffer.readItemStack());
        }

        request.overrideCurrentDeliveries(ImmutableList.copyOf(deliveries));
        return request;
    }

    @FunctionalInterface
    public interface IObjectToNBTConverter<O>
    {
        CompoundNBT apply(IFactoryController controller, O object);
    }

    @FunctionalInterface
    public interface INBTToObjectConverter<O>
    {
        O apply(IFactoryController controller, CompoundNBT compound);
    }

    @FunctionalInterface
    public interface IObjectToPackBufferWriter<O>
    {
        void apply(IFactoryController controller, O object, PacketBuffer buffer);
    }

    @FunctionalInterface
    public interface IPacketBufferToObjectReader<O>
    {
        O apply(IFactoryController controller, PacketBuffer buffer);
    }

    @FunctionalInterface
    public interface IObjectConstructor<T, O>
    {
        O construct(@NotNull final T requested, @NotNull final IToken<?> token, @NotNull final IRequester requester, @NotNull final RequestState requestState);
    }
}
