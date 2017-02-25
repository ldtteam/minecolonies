package com.minecolonies.coremod.colony.management.requestsystem.requests;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestManager;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestToken;
import com.minecolonies.coremod.colony.management.requestsystem.api.RequestState;
import com.minecolonies.coremod.colony.management.requestsystem.api.requests.IRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstract skeleton implementation of a request.
 *
 * @param <R> The type of request this is.
 */
public abstract class AbstractRequest<R> implements IRequest<R> {

    @NotNull
    protected final IRequestToken token;
    @NotNull
    protected final IColony colony;
    @NotNull
    private RequestState state = RequestState.CREATED;
    @Nullable
    private R result;
    @NotNull
    private final R requested;
    @Nullable
    private IRequestToken parent;
    @NotNull
    private final ArrayList<IRequestToken> children;

    public AbstractRequest(@NotNull IColony colony, @NotNull IRequestToken token, @NotNull R requested) {
        this.colony = colony;
        this.token = token;
        this.requested = requested;

        children = new ArrayList<>();
    }


    /**
     * Used to determine which type of request this is.
     * Only RequestResolvers for this Type are then used to resolve the this.
     *
     * @return The class that represents this Type of Request.
     */
    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends R> getRequestType() {
        return (Class<? extends R>) getRequest().getClass();
    }


    /**
     * The unique token representing the request outside of the management system.
     *
     * @return the token representing the request outside of the management system.
     */
    @Override
    public IRequestToken getToken() {
        return token;
    }

    /**
     * Returns the current state of the request.
     *
     * @return The current state.
     */
    @NotNull
    @Override
    public RequestState getState() {
        return state;
    }

    /**
     * Setter for the current state of this request.
     * <p>
     * The implementing class is responsible for propagating the state change to its parent, if needed.
     *
     * @param manager The request manager that updated the state.
     * @param state   The new state of this request.
     */
    @Override
    public void setState(@NotNull IRequestManager manager, @NotNull RequestState state) {
        this.state = state;
        Log.getLogger().debug("Updated state from: " + getToken() + " to: " + state);

        if (this.hasParent()) {
            try {
                manager.getRequestForToken(getParent()).childStateUpdated(manager, getToken());
            } catch (IllegalArgumentException ex) {
                //Something went wrong... Logging. Continuing however. Might cause parent request to get stuck however......
                Log.getLogger().error(new IllegalStateException("Failed to update parent state.", ex));
            }
        }
    }

    /**
     * Returns the colony this request was made in.
     *
     * @return the colony this request was made in.
     */
    @NotNull
    @Override
    public IColony getColony() {
        return colony;
    }

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
    @Override
    public R getRequest() {
        return requested;
    }

    /**
     * Returns the result of this request.
     *
     * @return The result of this request, or null if it is not available.
     */
    @Nullable
    @Override
    public R getResult() {
        return result;
    }

    /**
     * Setter for the result of the request.
     *
     * @param result The new result of this request.
     */
    @Override
    public void setResult(@NotNull R result) {
        this.result = result;
    }

    /**
     * Returns the parent of this request.
     * If this is set it means that this request is part of request chain.
     *
     * @return The parent of this request, or null if it has no parent.
     */
    @Nullable
    @Override
    public IRequestToken getParent() {
        return parent;
    }

    /**
     * Method used to set the parent of a request.
     *
     * @param parent The new parent, or null to clear the existing one.
     */
    @Override
    public void setParent(@Nullable IRequestToken parent) {
        this.parent = parent;
    }

    /**
     * Returns true if this request has a parent, false if not.
     *
     * @return true if this request has a parent, false if not.
     */
    @Override
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Method used to add a single Child.
     *
     * @param child The new child request to add.
     */
    @Override
    public void addChild(@NotNull IRequestToken child) {
        this.children.add(child);
        Log.getLogger().debug("Added child:" + child + " to: " + getToken());
    }

    /**
     * Method to add multiple children in a single call.
     *
     * @param children An array of children to add.
     */
    @Override
    public void addChildren(@NotNull IRequestToken[] children) {
        for(IRequestToken token : children)
            addChild(token);
    }

    /**
     * Method to add multiple children in a single call.
     *
     * @param children A collection of children to add.
     */
    @Override
    public void addChildren(@NotNull Collection<IRequestToken> children) {
        for(IRequestToken token : children)
            addChild(token);
    }

    /**
     * Method used to remove a single Child.
     *
     * @param child The new child request to remove.
     */
    @Override
    public void removeChild(@NotNull IRequestToken child) {
        this.children.remove(child);
        Log.getLogger().debug("Removed child: " + child + " from: " + getToken());
    }

    /**
     * Method to remove multiple children in a single call.
     *
     * @param children An array of children to remove.
     */
    @Override
    public void removeChildren(@NotNull IRequestToken[] children) {
        for(IRequestToken token : children)  {
            if (this.children.contains(token)) {
                this.removeChild(token);
            }
        }
    }

    /**
     * Method to remove multiple children in a single call.
     *
     * @param children A collection of children to remove.
     */
    @Override
    public void removeChildren(@NotNull Collection<IRequestToken> children) {
        for(IRequestToken token : children)  {
            if (this.children.contains(token)) {
                this.removeChild(token);
            }
        }
    }

    /**
     * Method to check if this request has children.
     *
     * @return
     */
    @Override
    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    /**
     * Method to get the children of this request.
     * Immutable.
     *
     * @return An immutable collection of the children of this request.
     */
    @NotNull
    @Override
    public ImmutableCollection<IRequestToken> getChildren() {
        ImmutableCollection.Builder<IRequestToken> builder = new ImmutableList.Builder<>();

        builder.addAll(this.children);

        return builder.build();
    }

    /**
     * Method called by a child state to indicate that its state has been updated.
     *
     * @param manager The manager that caused the update on the child.
     * @param child   The child that was updated.
     */
    @Override
    public void childStateUpdated(@NotNull IRequestManager manager, @NotNull IRequestToken child) {
        if (!this.children.contains(child)){
            //WHAT? Log and return.
            Log.getLogger().warn("The given child:" + child + " could not update the parent as it was not registered.");
        }

        try {
            IRequest<?> childRequest = manager.getRequestForToken(child);
            if (childRequest.getState() == RequestState.COMPLETED) {
                this.removeChild(child);
                Log.getLogger().debug("Removed child:" + child + " as it was completed!");
            }
        } catch (IllegalArgumentException ex) {
            Log.getLogger().error(new IllegalStateException("Failed to update request data when child changed.", ex));
        }
    }
}
