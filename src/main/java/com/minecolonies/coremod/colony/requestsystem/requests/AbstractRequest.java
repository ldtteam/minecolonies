package com.minecolonies.coremod.colony.requestsystem.requests;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.LogHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract skeleton implementation of a request.
 *
 * @param <R> The type of request this is.
 */
public abstract class AbstractRequest<R extends IRequestable> implements IRequest<R>
{

    @NotNull
    private final IToken       token;
    @NotNull
    private final R            requested;
    @NotNull
    private final List<IToken> children;
    @NotNull
    private final IRequester   requester;
    @NotNull
    private RequestState state = RequestState.CREATED;
    @Nullable
    private R      result;
    @Nullable
    private IToken parent;
    @SuppressWarnings("squid:S1170")

    private List<ItemStack> deliveries = Lists.newArrayList();

    private ImmutableList<ItemStack> itemExamples;

    protected AbstractRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final R requested)
    {
        this.requester = requester;
        this.token = token;
        this.requested = requested;

        children = new ArrayList<>();
    }

    protected AbstractRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final RequestState state, @NotNull final R requested)
    {
        this.requester = requester;
        this.token = token;
        this.state = state;
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
    public TypeToken<? extends R> getType()
    {
        return TypeToken.of((Class<? extends R>) getRequest().getClass());
    }

    /**
     * The location of the requester.
     * Is generally used in getDelivery requests to produce a getDelivery for a result from this request.
     *
     * @return The location of requester of this request.
     */
    @NotNull
    @Override
    public IRequester getRequester()
    {
        return requester;
    }

    /**
     * The unique token representing the request outside of the management system.
     *
     * @return the token representing the request outside of the management system.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends IToken> T getId()
    {
        return (T) token;
    }

    /**
     * Returns the current state of the request.
     *
     * @return The current state.
     */
    @NotNull
    @Override
    public RequestState getState()
    {
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
    public void setState(@NotNull final IRequestManager manager, @NotNull final RequestState state)
    {
        this.state = state;
        LogHandler.log("Updated state from: " + getId() + " to: " + state);

        if (this.hasParent() && this.getParent() != null)
        {
            try
            {
                manager.getRequestForToken(getParent()).childStateUpdated(manager, getId());
            }
            catch (final IllegalArgumentException ex)
            {
                //Something went wrong... Logging. Continuing however. Might cause parent request to get stuck however......
                Log.getLogger().error(new IllegalStateException("Failed to update parent state.", ex));
            }
        }
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
    public R getRequest()
    {
        return requested;
    }

    /**
     * Returns the result of this request.
     *
     * @return The result of this request, or null if it is not available.
     */
    @Nullable
    @Override
    public R getResult()
    {
        return result;
    }

    /**
     * Setter for the result of the request.
     *
     * @param result The new result of this request.
     */
    @Override
    public void setResult(@NotNull final R result)
    {
        this.result = result;
    }

    /**
     * Method used to check if the result has been set.
     *
     * @return True when the result has been set, false when not.
     */
    @Override
    public boolean hasResult()
    {
        return getResult() != null;
    }

    /**
     * Returns the parent of this request.
     * If this is set it means that this request is part of request chain.
     *
     * @return The parent of this request, or null if it has no parent.
     */
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends IToken> T getParent()
    {
        return (T) parent;
    }

    /**
     * Method used to set the parent of a request.
     *
     * @param parent The new parent, or null to clear the existing one.
     */
    @Override
    public <T extends IToken> void setParent(@Nullable final T parent)
    {
        this.parent = parent;
    }

    /**
     * Returns true if this request has a parent, false if not.
     *
     * @return true if this request has a parent, false if not.
     */
    @Override
    public boolean hasParent()
    {
        return parent != null;
    }

    /**
     * Method used to add a single Child.
     *
     * @param child The new child request to add.
     */
    @Override
    public <T extends IToken> void addChild(@NotNull final T child)
    {
        this.children.add(child);
        LogHandler.log("Added child:" + child + " to: " + getId());
    }

    /**
     * Method to add multiple children in a single call.
     *
     * @param children An array of children to add.
     */
    @Override
    public <T extends IToken> void addChildren(@NotNull final T... children)
    {
        for (final IToken theToken : children)
        {
            addChild(theToken);
        }
    }

    /**
     * Method to add multiple children in a single call.
     *
     * @param children A collection of children to add.
     */
    @Override
    public <T extends IToken> void addChildren(@NotNull final Collection<T> children)
    {
        for (final IToken theToken : children)
        {
            addChild(theToken);
        }
    }

    /**
     * Method used to remove a single Child.
     *
     * @param child The new child request to remove.
     */
    @Override
    public <T extends IToken> void removeChild(@NotNull final T child)
    {
        this.children.remove(child);
        LogHandler.log("Removed child: " + child + " from: " + getId());
    }

    /**
     * Method to remove multiple children in a single call.
     *
     * @param children An array of children to remove.
     */
    @Override
    public <T extends IToken> void removeChildren(@NotNull final T... children)
    {
        for (final IToken theToken : children)
        {
            if (this.children.contains(theToken))
            {
                this.removeChild(theToken);
            }
        }
    }

    /**
     * Method to remove multiple children in a single call.
     *
     * @param children A collection of children to remove.
     */
    @Override
    public <T extends IToken> void removeChildren(@NotNull final Collection<T> children)
    {
        for (final IToken theToken : children)
        {
            if (this.children.contains(theToken))
            {
                this.removeChild(theToken);
            }
        }
    }

    /**
     * Method to check if this request has children.
     */
    @Override
    public boolean hasChildren()
    {
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
    public ImmutableCollection<IToken> getChildren()
    {
        final ImmutableCollection.Builder<IToken> builder = new ImmutableList.Builder<>();

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
    public void childStateUpdated(@NotNull final IRequestManager manager, @NotNull final IToken child)
    {
        if (!this.children.contains(child))
        {
            //WHAT? log and return.
            Log.getLogger().warn("The given child:" + child + " could not update the parent:" + getId() + " as it was not registered.");
        }

        try
        {
            final IRequest<?> childRequest = manager.getRequestForToken(child);
            if (childRequest.getState() == RequestState.IN_PROGRESS && getState().ordinal() < RequestState.IN_PROGRESS.ordinal())
            {
                setState(manager, RequestState.IN_PROGRESS);
                LogHandler.log("First child entering progression: " + child + " setting progression state for: " + getId());
            }
        }
        catch (final IllegalArgumentException ex)
        {
            Log.getLogger().error(new IllegalStateException("Failed to update request data when child changed.", ex));
        }
    }

    /**
     * Method used to indicate that the result of this request can be delivered.
     *
     * @return True for requests that can be delivered, false when not.
     */
    @Override
    public boolean canBeDelivered()
    {
        return !getDeliveries().isEmpty();
    }

    @NotNull
    @Override
    public ImmutableList<ItemStack> getDeliveries()
    {
        return ImmutableList.copyOf(deliveries);
    }

    @Override
    public void overrideCurrentDeliveries(@NotNull final ImmutableList<ItemStack> stacks)
    {
        this.deliveries = Lists.newArrayList(stacks);
    }

    @Override
    public void addDelivery(@NotNull final ItemStack stack)
    {
        this.deliveries.add(stack);
        this.deliveries = InventoryUtils.processItemStackListAndMerge(this.deliveries);
    }

    @NotNull
    @Override
    public ITextComponent getLongDisplayString()
    {
        return getShortDisplayString();
    }

    @NotNull
    @Override
    public ResourceLocation getDisplayIcon()
    {
        return new ResourceLocation("missingno");
    }

    @Override
    public List<ItemStack> getDisplayStacks()
    {
        if (!(getRequest() instanceof IDeliverable))
        {
            return Lists.newArrayList();
        }

        final IDeliverable deliverable = (IDeliverable) getRequest();

        if (itemExamples == null)
        {
            itemExamples = ImmutableList.copyOf(IColonyManager.getInstance().getCompatibilityManager().getBlockList().stream().filter(deliverable::matches).collect(Collectors.toList()));
        }

        return itemExamples;
    }

    @NotNull
    @Override
    public <T> Optional<T> getRequestOfType(final Class<T> tClass)
    {
        final R request = getRequest();
        if (tClass.isInstance(request))
        {
            return Optional.of(tClass.cast(request));
        }

        return Optional.empty();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractRequest))
        {
            return false;
        }

        final AbstractRequest<?> that = (AbstractRequest<?>) o;

        if (!getId().equals(that.getId()))
        {
            return false;
        }
        if (!requested.equals(that.requested))
        {
            return false;
        }
        if (!getChildren().equals(that.getChildren()))
        {
            return false;
        }
        if (!getRequester().equals(that.getRequester()))
        {
            return false;
        }
        if (getState() != that.getState())
        {
            return false;
        }
        if (getResult() != null ? !getResult().equals(that.getResult()) : that.getResult() != null)
        {
            return false;
        }
        if (getParent() != null ? !getParent().equals(that.getParent()) : that.getParent() != null)
        {
            return false;
        }
        if (!getDeliveries().equals(that.getDeliveries()))
        {
            return false;
        }
        return Objects.equals(itemExamples, that.itemExamples);
    }

    @Override
    public int hashCode()
    {
        int result1 = getId().hashCode();
        result1 = 31 * result1 + requested.hashCode();
        result1 = 31 * result1 + getChildren().hashCode();
        result1 = 31 * result1 + getRequester().hashCode();
        result1 = 31 * result1 + getState().hashCode();
        result1 = 31 * result1 + (getResult() != null ? getResult().hashCode() : 0);
        result1 = 31 * result1 + (getParent() != null ? getParent().hashCode() : 0);
        result1 = 31 * result1 + getDeliveries().hashCode();
        result1 = 31 * result1 + (itemExamples != null ? itemExamples.hashCode() : 0);
        return result1;
    }
}
