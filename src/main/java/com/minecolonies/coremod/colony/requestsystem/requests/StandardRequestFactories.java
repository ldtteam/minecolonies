package com.minecolonies.coremod.colony.requestsystem.requests;

import com.minecolonies.coremod.colony.requestsystem.RequestState;
import com.minecolonies.coremod.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.coremod.colony.requestsystem.request.IRequestFactory;
import com.minecolonies.coremod.colony.requestsystem.requestable.Delivery;
import com.minecolonies.coremod.colony.requestsystem.requester.IRequester;
import com.minecolonies.coremod.colony.requestsystem.token.IToken;
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
public final class StandardRequestFactories {

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN = "Token";
    private static final String NBT_STATE = "State";
    private static final String NBT_REQUESTED = "Requested";
    private static final String NBT_RESULT = "Result";
    private static final String NBT_PARENT = "Parent";
    private static final String NBT_CHILDREN = "Children";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    public static final class ItemStackFactory implements IRequestFactory<ItemStack, StandardRequests.ItemStackRequest> {
        /**
         * Method to get the request type this factory can produce.
         *
         * @return The type of request this factory can produce.
         */
        @NotNull
        @Override
        public Class<? extends StandardRequests.ItemStackRequest> getFactoryOutputType() {
            return StandardRequests.ItemStackRequest.class;
        }

        /**
         * Used to determine which type of request this can produce.
         *
         * @return The class that represents the Type of Request this can produce.
         */
        @NotNull
        @Override
        public Class<? extends ItemStack> getFactoryInputType() {
            return ItemStack.class;
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
        public NBTTagCompound serialize(@NotNull IFactoryController controller, @NotNull StandardRequests.ItemStackRequest request) {
            NBTTagCompound compound = new NBTTagCompound();

            NBTTagCompound tokenCompound = controller.serialize(request.getToken());
            NBTTagInt stateCompound = request.getState().serializeNBT();
            NBTTagCompound requestedCompound = request.getRequest().serializeNBT();

            NBTTagList childrenCompound = new NBTTagList();
            for (IToken token : request.getChildren()) {
                childrenCompound.appendTag(controller.serialize(token));
            }

            compound.setTag(NBT_TOKEN, tokenCompound);
            compound.setTag(NBT_STATE, stateCompound);
            compound.setTag(NBT_REQUESTED, requestedCompound);

            if (request.hasResult())
                compound.setTag(NBT_RESULT, request.getResult().serializeNBT());

            if (request.hasParent())
                compound.setTag(NBT_PARENT, controller.serialize(request.getParent()));

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
        public StandardRequests.ItemStackRequest deserialize(@NotNull IFactoryController controller, @NotNull NBTTagCompound nbt) {
            IToken token = controller.deserialize(nbt.getCompoundTag(NBT_TOKEN));
            RequestState state = RequestState.deserializeNBT((NBTTagInt) nbt.getTag(NBT_STATE));
            ItemStack requested = new ItemStack(nbt.getCompoundTag(NBT_REQUESTED));

            List<IToken> childTokens = new ArrayList<>();
            NBTTagList childCompound = nbt.getTagList(NBT_CHILDREN, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < childCompound.tagCount(); i++) {
                childTokens.add(controller.deserialize(childCompound.getCompoundTagAt(i)));
            }

            StandardRequests.ItemStackRequest request = controller.getNewInstance(requested, token, state);

            if (nbt.hasKey(NBT_PARENT))
                request.setParent(controller.deserialize(nbt.getCompoundTag(NBT_PARENT)));

            if (nbt.hasKey(NBT_RESULT))
                request.setResult(new ItemStack(nbt.getCompoundTag(NBT_RESULT)));

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
        public StandardRequests.ItemStackRequest getNewInstance(@NotNull ItemStack input, @NotNull IRequester location, @NotNull IToken token, @NotNull RequestState initialState)
        {
            return new StandardRequests.ItemStackRequest(location, token, initialState, input);
        }
    }

    public static final class DeliveryFactory implements IRequestFactory<Delivery, StandardRequests.DeliveryRequest> {

        /**
         * Method to get the request type this factory can produce.
         *
         * @return The type of request this factory can produce.
         */
        @NotNull
        @Override
        public Class<? extends StandardRequests.DeliveryRequest> getFactoryOutputType() {
            return StandardRequests.DeliveryRequest.class;
        }

        /**
         * Used to determine which type of request this can produce.
         *
         * @return The class that represents the Type of Request this can produce.
         */
        @NotNull
        @Override
        public Class<? extends Delivery> getFactoryInputType() {
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
        public NBTTagCompound serialize(@NotNull IFactoryController controller, @NotNull StandardRequests.DeliveryRequest request) {
            NBTTagCompound compound = new NBTTagCompound();

            NBTTagCompound tokenCompound = controller.serialize(request.getToken());
            NBTTagInt stateCompound = request.getState().serializeNBT();
            NBTTagCompound requestedCompound = request.getRequest().serialize(controller);

            NBTTagList childrenCompound = new NBTTagList();
            for (IToken token : request.getChildren()) {
                childrenCompound.appendTag(controller.serialize(token));
            }

            compound.setTag(NBT_TOKEN, tokenCompound);
            compound.setTag(NBT_STATE, stateCompound);
            compound.setTag(NBT_REQUESTED, requestedCompound);

            if (request.hasResult())
                compound.setTag(NBT_RESULT, request.getResult().serialize(controller));

            if (request.hasParent())
                compound.setTag(NBT_PARENT, controller.serialize(request.getParent()));

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
        public StandardRequests.DeliveryRequest deserialize(@NotNull IFactoryController controller, @NotNull NBTTagCompound nbt) {
            IToken token = controller.deserialize(nbt.getCompoundTag(NBT_TOKEN));
            RequestState state = RequestState.deserializeNBT((NBTTagInt) nbt.getTag(NBT_STATE));
            Delivery requested = Delivery.deserialize(controller, nbt.getCompoundTag(NBT_REQUESTED));

            List<IToken> childTokens = new ArrayList<>();
            NBTTagList childCompound = nbt.getTagList(NBT_CHILDREN, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < childCompound.tagCount(); i++) {
                childTokens.add(controller.deserialize(childCompound.getCompoundTagAt(i)));
            }

            StandardRequests.DeliveryRequest request = controller.getNewInstance(requested, token, state);

            if (nbt.hasKey(NBT_PARENT))
                request.setParent(controller.deserialize(nbt.getCompoundTag(NBT_PARENT)));

            if (nbt.hasKey(NBT_RESULT))
                request.setResult(Delivery.deserialize(controller, nbt.getCompoundTag(NBT_RESULT)));

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
        public StandardRequests.DeliveryRequest getNewInstance(@NotNull Delivery input, @NotNull IRequester location, @NotNull IToken token, @NotNull RequestState initialState)
        {
            return new StandardRequests.DeliveryRequest(location, token, initialState, input);
        }
    }
}
