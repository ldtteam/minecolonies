package com.minecolonies.api.colony.requestsystem.request;

import com.google.common.collect.ImmutableCollection;
import com.minecolonies.api.colony.requestsystem.IRequestManager;
import com.minecolonies.api.colony.requestsystem.RequestState;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Used to represent requests, of type R, made to the internal market of the colony.
 *
 * @param <R> The type of request, eg ItemStack, FluidStack etc.
 */
public interface IRequest<R>
{

    /**
     * The unique token representing the request outside of the management system.
     *
     * @return the token representing the request outside of the management system.
     */
    <T extends IToken> T getToken();

    /**
     * Used to determine which type of request this is.
     * Only RequestResolvers for this Type are then used to resolve the this.
     *
     * @return The class that represents this Type of Request.
     */
    @NotNull
    Class<? extends R> getRequestType();

    /**
     * Returns the current state of the request.
     *
     * @return The current state.
     */
    @NotNull
    RequestState getState();

    /**
     * Setter for the current state of this request.
     * It is not recommended to call this method from outside of the request management system.
     *
     * @param state The new state of this request.
     */
    void setState(@NotNull IRequestManager manager, @NotNull RequestState state);

    /**
     * The element of the colony that requested this request.
     *
     * @return The requester of this request.
     */
    @NotNull
    IRequester getRequester();

    /**
     * Return the object that is actually requested.
     * A RequestResolver can compare this object however way it sees fit.
     * <p>
     * During the resolving process this object is called multiple times. But at least twice.
     * A cached implementation is preferred.
     *
     * @return The object that is actually requested.
     */
    @NotNull
    R getRequest();

    /**
     * Returns the result of this request.
     *
     * @return The result of this request, or null if it is not available.
     */
    @Nullable
    R getResult();

    /**
     * Setter for the result of the request.
     *
     * @param result The new result of this request.
     */
    void setResult(@NotNull R result);

    /**
     * Method used to check if the result has been set.
     *
     * @return True when the result has been set, false when not.
     */
    boolean hasResult();

    /**
     * Returns the parent of this request.
     * If this is set it means that this request is part of request chain.
     *
     * @return The parent of this request, or null if it has no parent.
     */
    @Nullable
    <T extends IToken> T getParent();

    /**
     * Method used to set the parent of a request.
     *
     * @param parent The new parent, or null to clear the existing one.
     */
    <T extends IToken> void setParent(@Nullable T parent);

    /**
     * Returns true if this request has a parent, false if not.
     *
     * @return true if this request has a parent, false if not.
     */
    boolean hasParent();

    /**
     * Method used to add a single Child.
     *
     * @param child The new child request to add.
     */
    <T extends IToken> void addChild(@NotNull T child);

    /**
     * Method to add multiple children in a single call.
     *
     * @param children An array of children to add.
     */
    <T extends IToken> void addChildren(@NotNull T... children);

    /**
     * Method to add multiple children in a single call.
     *
     * @param children A collection of children to add.
     */
    <T extends IToken> void addChildren(@NotNull Collection<T> children);

    /**
     * Method used to remove a single Child.
     *
     * @param child The new child request to remove.
     */
    <T extends IToken> void removeChild(@NotNull T child);

    /**
     * Method to remove multiple children in a single call.
     *
     * @param children An array of children to remove.
     */
    <T extends IToken> void removeChildren(@NotNull T... children);

    /**
     * Method to remove multiple children in a single call.
     *
     * @param children A collection of children to remove.
     */
    <T extends IToken> void removeChildren(@NotNull Collection<T> children);

    /**
     * Method to check if this request has children.
     */
    boolean hasChildren();

    /**
     * Method to get the children of this request.
     * Immutable.
     *
     * @return An immutable collection of the children of this request.
     */
    @NotNull
    ImmutableCollection<IToken> getChildren();

    /**
     * Method called by a child state to indicate that its state has been updated.
     *
     * @param manager The manager that caused the update on the child.
     * @param child   The child that was updated.
     */
    void childStateUpdated(@NotNull IRequestManager manager, @NotNull IToken child);

    /**
     * Method used to indicate that the result of this request can be delivered.
     *
     * @return True for requests that can be delivered, false when not.
     */
    boolean canBeDelivered();

    /**
     * Method to get the ItemStack used for the getDelivery.
     *
     * @return The ItemStack that the Deliveryman transports around. ItemStack.Empty means no delivery possible.
     */
    @NotNull
    ItemStack getDelivery();
}
