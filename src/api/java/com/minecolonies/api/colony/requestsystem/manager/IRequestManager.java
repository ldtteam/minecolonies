package com.minecolonies.api.colony.requestsystem.manager;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.data.IDataStoreManager;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Interface used to describe classes that function as managers for requests inside a colony.
 * Extends INBTSerializable to allow for easy reading and writing from NBT.
 */
public interface IRequestManager extends INBTSerializable<CompoundNBT>, ITickableTileEntity
{

    /**
     * The colony this manager manages the requests for.
     *
     * @return The colony this manager manages the requests for.
     */
    @NotNull
    IColony getColony();

    /**
     * Method used to get the FactoryController of the RequestManager.
     *
     * @return The FactoryController of this RequestManager.
     */
    @NotNull
    IFactoryController getFactoryController();

    /**
     * Method to create a request for a given object
     *
     * @param requester The requester.
     * @param object    The Object that is being requested.
     * @param <T>       The type of request.
     * @return The token representing the request.
     *
     * @throws IllegalArgumentException is thrown when this manager cannot produce a request for the given types.
     */
    @NotNull
    <T extends IRequestable> IToken<?> createRequest(@NotNull IRequester requester, @NotNull T object) throws IllegalArgumentException;

    /**
     * Method used to assign a request to a resolver.
     *
     * @param token The token of the request to assign.
     * @throws IllegalArgumentException when the token is not registered to a request, or is already assigned to a resolver.
     */
    @NotNull
    void assignRequest(@NotNull IToken<?> token) throws IllegalArgumentException;

    /**
     * Method used to create and immediately assign a request.
     *
     * @param requester The requester of the requestable.
     * @param object    The requestable
     * @param <T>       The type of the requestable
     * @return The token that represents the request.
     *
     * @throws IllegalArgumentException when either createRequest or assignRequest have thrown an IllegalArgumentException
     */
    @NotNull
    default <T extends IRequestable> IToken<?> createAndAssignRequest(@NotNull IRequester requester, @NotNull T object) throws IllegalArgumentException
    {
        final IToken<?> token = createRequest(requester, object);
        assignRequest(token);
        return token;
    }

    /**
     * Method used to reassign a given request.
     *
     * @param token The token of the request that should be reassigned.
     * @param resolverTokenBlackList the blacklist.
     * @return The token of the resolver that has gotten the assignment, null if none was found.
     *
     * @throws IllegalArgumentException when the token is not known to this manager.
     */
    @Nullable
    IToken<?> reassignRequest(@NotNull IToken<?> token, @NotNull Collection<IToken<?>> resolverTokenBlackList) throws IllegalArgumentException;

    /**
     * Method to get a request for a given token.
     *
     * @param token The token to get a request for.
     * @return The request of the given type for that token.
     *
     * @throws IllegalArgumentException when the token does not produce a request of the given type T.
     */
    @Nullable
    IRequest<?> getRequestForToken(@NotNull final IToken<?> token) throws IllegalArgumentException;

    /**
     * Method to get a resolver from its token.
     * @param token the token.
     * @return The resolver registered with the given token.
     *
     * @throws IllegalArgumentException when the token is unknown.
     */
    @NotNull
    IRequestResolver<?> getResolverForToken(@NotNull final IToken<?> token) throws IllegalArgumentException;

    /**
     * Method to get a resolver for a given request.
     *
     * @param requestToken The token of the request to get resolver for.
     * @return Null if the request is not yet resolved, or else the assigned resolver.
     *
     * @throws IllegalArgumentException Thrown when the token is unknown.
     */
    @Nullable
    IRequestResolver<?> getResolverForRequest(@NotNull final IToken<?> requestToken) throws IllegalArgumentException;

    /**
     * Method to update the state of a given request.
     *
     * @param token The token that represents a given request to update.
     * @param state The new state of that request.
     * @throws IllegalArgumentException when the token is unknown to this manager.
     */
    @NotNull
    void updateRequestState(@NotNull IToken<?> token, @NotNull RequestState state) throws IllegalArgumentException;

    /**
     * Method used to overrule a request.
     * Updates the state and sets the delivery if applicable.
     *
     * @param token The token of the request that is being overruled.
     * @param stack The stack that should be treated as delivery. If no delivery is possible, this is null.
     * @throws IllegalArgumentException Thrown when either token does not match to a request.
     */
    void overruleRequest(@NotNull IToken<?> token, @Nullable ItemStack stack) throws IllegalArgumentException;

    /**
     * Method used to indicate to this manager that a new Provider has been added to the colony.
     *
     * @param provider The new provider.
     * @throws IllegalArgumentException is thrown when a provider with the same token is already registered.
     */
    void onProviderAddedToColony(@NotNull IRequestResolverProvider provider) throws IllegalArgumentException;

    /**
     * Method used to indicate to this manager that Provider has been removed from the colony.
     *
     * @param provider The removed provider.
     * @throws IllegalArgumentException is thrown when no provider with the same token is registered.
     */
    void onProviderRemovedFromColony(@NotNull IRequestResolverProvider provider) throws IllegalArgumentException;

    /**
     * Method used to indicate that a colony has updated their available items.
     * @param shouldTriggerReassign The request assigned
     */
    void onColonyUpdate(@NotNull final Predicate<IRequest> shouldTriggerReassign);

    /**
     * Get the player resolve.
     *
     * @return the player resolver object.
     */
    @NotNull
    IPlayerRequestResolver getPlayerResolver();

    /**
     * Get the retrying request resolver.
     *
     * @return The retrying request resolver.
     */
    @NotNull
    IRetryingRequestResolver getRetryingRequestResolver();

    /**
     * Get the data store manager.
     * @return The data store manager.
     */
    @NotNull
    IDataStoreManager getDataStoreManager();

    /**
     * Called to reset the RS.
     */
    void reset();

    /**
     * Checks if dirty and needs to be updated.
     * @return true if so.
     */
    boolean isDirty();

    /**
     * Sets if dirty and needs to be updated.
     * @param isDirty true if so.
     */
    void setDirty(boolean isDirty);

    /**
     * Marks this manager dirty.
     */
    void markDirty();

    Logger getLogger();
}
