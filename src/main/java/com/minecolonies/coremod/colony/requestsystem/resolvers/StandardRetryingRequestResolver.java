package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRetryable;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.RSConstants.CONST_RETRYING_RESOLVER_PRIORITY;

public class StandardRetryingRequestResolver implements IRetryingRequestResolver
{

    private static final Integer CONST_RETRYING_ID_SCALE = -20000;

    private       IRequestManager             manager;
    private final ILocation                   location;
    private final IToken<?>                   id;
    private       IToken<?>                   current;
    private final HashMap<IToken<?>, Integer> delays           = new HashMap<>();
    private final HashMap<IToken<?>, Integer> assignedRequests = new HashMap<>();

    public StandardRetryingRequestResolver(final IFactoryController factoryController, final IRequestManager manager)
    {
        this.updateManager(manager);

        this.id = factoryController.getNewInstance(TypeConstants.ITOKEN, manager.getColony().getID() * CONST_RETRYING_ID_SCALE);
        this.location = factoryController.getNewInstance(TypeConstants.ILOCATION, manager.getColony().getCenter(), manager.getColony().getDimension());
    }

    public StandardRetryingRequestResolver(final IToken<?> id, final ILocation location)
    {
        this.id = id;
        this.location = location;
    }

    @Override
    public void updateManager(final IRequestManager manager)
    {
        this.manager = manager;
    }

    @Override
    public int getMaximalTries()
    {
        return MineColonies.getConfig().getServer().maximalRetries.get();
    }

    @Override
    public int getMaximalDelayBetweenRetriesInTicks()
    {
        return MineColonies.getConfig().getServer().delayBetweenRetries.get();
    }

    @Override
    public int getCurrentReassignmentAttempt()
    {
        return isReassigning() ? -1 : assignedRequests.get(getCurrentlyBeingReassignedRequest()) + 1;
    }

    @Nullable
    @Override
    public IToken<?> getCurrentlyBeingReassignedRequest()
    {
        return current;
    }

    @Override
    public TypeToken<? extends IRetryable> getRequestType()
    {
        return TypeConstants.RETRYABLE;
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends IRetryable> requestToCheck)
    {
        return getCurrentlyBeingReassignedRequest() == null || requestToCheck.getId() != getCurrentlyBeingReassignedRequest()
                 || getCurrentReassignmentAttempt() < getMaximalTries();
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request)
    {
        return ImmutableList.of();
    }

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request) throws RuntimeException
    {
        delays.put(request.getId(), getMaximalDelayBetweenRetriesInTicks());
        assignedRequests.put(request.getId(), assignedRequests.containsKey(request.getId()) ? assignedRequests.get(request.getId()) + 1 : 1);
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> completedRequest)
    {
        //Gets never called, since these will never get completed, only overruled or reassigned.
        return null;
    }

    @Nullable
    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request)
    {
        if (assignedRequests.containsKey(request.getId()))
        {
            delays.remove(request.getId());
            assignedRequests.remove(request.getId());
        }
    }

    @Override
    public void onAssignedRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRetryable> request)
    {

    }

    @Override
    public int getPriority()
    {
        return CONST_RETRYING_RESOLVER_PRIORITY;
    }

    @Override
    public void tick()
    {
        manager.getLogger().debug("Starting reassignment.");

        //Lets decrement all delays
        getAllAssignedRequests().forEach(t -> {
            Integer current = delays.remove(t);
            delays.put(t, --current);
        });

        //Lets get all keys with 0 residual delay:
        final Set<IToken<?>> retryables = delays.keySet().stream().filter(t -> delays.get(t) == 0).collect(Collectors.toSet());
        final Set<IToken<?>> successfully = retryables.stream().filter(t -> {
            final Set<IToken<?>> blackList = assignedRequests.get(t) < getMaximalTries() ? ImmutableSet.of() : ImmutableSet.of(id);

            Integer currentAttempt = assignedRequests.get(t);

            this.setCurrent(t);
            final IToken<?> resultingResolver;

            try
            {
                resultingResolver = manager.reassignRequest(t, blackList);
            }
            catch (Exception ex)
            {
                assignedRequests.remove(t);
                delays.remove(t);
                return false;
            }

            this.setCurrent(null);

            assignedRequests.put(t, ++currentAttempt);

            if (resultingResolver != null && !resultingResolver.equals(getId()))
            {
                assignedRequests.remove(t);
                delays.remove(t);
            }

            return resultingResolver != null;
        }).collect(Collectors.toSet());

        successfully.forEach(t -> {
            manager.getLogger().debug("Failed to reassign a retryable request: " + id);
        });

        manager.getLogger().debug("Finished reassignment.");
    }

    @Override
    public ImmutableList<IToken<?>> getAllAssignedRequests()
    {
        return ImmutableList.copyOf(assignedRequests.keySet());
    }

    @Override
    public void onSystemReset()
    {
        assignedRequests.clear();
        delays.clear();
    }

    public void setCurrent(@Nullable final IToken<?> token)
    {
        this.current = token;
    }

    @Override
    public IToken<?> getId()
    {
        return id;
    }

    @NotNull
    @Override
    public ILocation getLocation()
    {
        return location;
    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //Noop, we do not schedule child requests. So this is never called.
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        //Noop, see onRequestComplete.
    }

    @NotNull
    @Override
    public IFormattableTextComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return new StringTextComponent("Player");
    }

    public void updateData(@NotNull final Map<IToken<?>, Integer> newAssignedRequests, @NotNull final Map<IToken<?>, Integer> newDelays)
    {
        this.assignedRequests.clear();
        this.assignedRequests.putAll(newAssignedRequests);

        this.delays.clear();
        this.delays.putAll(newDelays);
    }

    public Map<IToken<?>, Integer> getDelays()
    {
        return delays;
    }

    public Map<IToken<?>, Integer> getAssignedRequests()
    {
        return assignedRequests;
    }

    @Override
    public void onColonyUpdate(@NotNull final IRequestManager manager, @NotNull final Predicate<IRequest<?>> shouldTriggerReassign)
    {
        new ArrayList<>(assignedRequests.keySet()).stream()
          .map(manager::getRequestForToken)
          .forEach(request ->
          {
              if (request != null)
              {
                  if (shouldTriggerReassign.test(request))
                  {
                      final IToken<?> newResolverToken = manager.reassignRequest(request.getId(), ImmutableList.of(getId()));
                      if (newResolverToken != null && !newResolverToken.equals(getId()))
                      {
                          assignedRequests.remove(request.getId());
                      }
                  }
                  else
                  {
                      IRequest<?> req = request;
                      while (req.hasParent())
                      {
                          req = manager.getRequestForToken(req.getParent());
                          if (req != null && shouldTriggerReassign.test(req))
                          {
                              if (req.hasChildren())
                              {
                                  final ImmutableCollection<IToken<?>> currentChildren = req.getChildren();
                                  currentChildren.forEach(((IStandardRequestManager) manager).getRequestHandler()::onRequestCancelledDirectly);
                              }

                              IToken<?> newResolverToken = manager.reassignRequest(req.getId(), ImmutableList.of(getId()));
                              if (newResolverToken != getId())
                              {
                                  assignedRequests.remove(request.getId());
                                  break;
                              }
                          }
                      }
                  }
              }
          });
    }
}
