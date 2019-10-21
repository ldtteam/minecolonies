package com.minecolonies.coremod.colony.requestsystem.management.testutils;

import com.google.common.collect.*;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.manager.RequestMappingHandler;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.requestsystem.token.StandardToken;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.testutils.ColonyMock;
import com.minecolonies.coremod.util.ModifyableLambdaWrapper;
import com.minecolonies.testutils.MatcherUtils;
import org.apache.logging.log4j.LogManager;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static com.minecolonies.testutils.MockitoUtils.doVoidAnswer;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * This class allows for the setup of mocked request managers during testing.
 * This class defines the behaviour of the request manager in all cases, regardless of initializer.
 */
public final class RequestManagerMock
{
    private RequestManagerMock()
    {
        throw new IllegalArgumentException("Utility");
    }

    public static IStandardRequestManager mockBlank()
    {
        return Mockito.mock(IStandardRequestManager.class);
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static IStandardRequestManager mock()
    {
        final ModifyableLambdaWrapper<Boolean> dirty = new ModifyableLambdaWrapper<>(false);
        final BiMap<IToken<?>, IRequest<?>> requestBiMap = HashBiMap.create();
        final BiMap<IToken<?>, IRequestResolver<?>> requestResolverBiMap = HashBiMap.create();
        final BiMap<IToken<?>, IRequestResolverProvider> requestResolverProviderBiMap = HashBiMap.create();
        final Multimap<IToken<?>, IToken<?>> requestResolverProviderToRequestResolverMultiMap = HashMultimap.create();
        final Map<IToken<?>, IToken<?>> requestResolverToRequestResolverProviderMap = Maps.newHashMap();
        final Multimap<IToken<?>, IToken<?>> requestResolverToAssignedRequestMultiMap = HashMultimap.create();
        final Map<IToken<?>, IToken<?>> assignedRequestsToRequestResolverMap = Maps.newHashMap();

        final IColony colony = ColonyMock.mockBlank();
        final IStandardRequestManager requestManager = mockBlank();

        when(requestManager.getLogger()).thenReturn(LogManager.getLogger("minecolonies.requestsystem.test"));

        when(colony.getRequestManager()).thenReturn(requestManager);
        when(requestManager.getColony()).thenReturn(colony);

        when(requestManager.getFactoryController()).thenReturn(StandardFactoryController.getInstance());

        when(requestManager.createRequest(Mockito.any(IRequester.class), Mockito.any())).thenAnswer((Answer<IToken<?>>) invocation -> {
            final IToken<?> token = new StandardToken(UUID.randomUUID());
            final IRequester requester = (IRequester) invocation.getArguments()[0];
            final Object requestable = invocation.getArguments()[1];

            final IRequest<?> request = StandardFactoryController.getInstance().getNewInstance(TypeToken.of((Class<? extends IRequest<?>>) RequestMappingHandler.getRequestableMappings()
                                                                                                                                             .get(requestable.getClass())), requestable, token, requester);

            requestBiMap.put(request.getId(), request);

            return request.getId();
        });

        when(requestManager.isDirty()).thenAnswer(invocation -> dirty.getValue());

        doVoidAnswer(invocationOnMock -> {
            dirty.setValue(invocationOnMock.getArgumentAt(0, Boolean.class));
        }).when(requestManager).setDirty(Matchers.anyBoolean());

        doVoidAnswer(invocationOnMock -> {
            requestManager.setDirty(true);
            colony.markDirty();
        }).when(requestManager).markDirty();

        //TODO: AssignRequest.

        //TODO: ReassignRequest.


        when(requestManager.getRequestForToken(Matchers.argThat(MatcherUtils.mapContainsKey(requestBiMap)))).thenAnswer(invocation -> requestBiMap.get(invocation.getArgumentAt(0, IToken.class)));

        when(requestManager.getRequestForToken(Matchers.argThat(MatcherUtils.mapDoesNotContainsKey(requestBiMap)))).thenThrow(new IllegalArgumentException("Unknown token"));

        when(requestManager.getResolverForToken(Matchers.argThat(MatcherUtils.mapContainsKey(requestResolverBiMap)))).thenAnswer(invocation -> requestResolverBiMap.containsKey(invocation.getArgumentAt(0, IToken.class)));

        when(requestManager.getResolverForToken(Matchers.argThat(MatcherUtils.mapDoesNotContainsKey(requestResolverBiMap)))).thenThrow(new IllegalArgumentException("Unknown token"));

        when(requestManager.getResolverForRequest(Matchers.argThat(MatcherUtils.mapContainsKey(assignedRequestsToRequestResolverMap)))).thenAnswer(invocation -> requestResolverBiMap.get(assignedRequestsToRequestResolverMap.get(invocation.getArgumentAt(0, IToken.class))));

        when(requestManager.getResolverForRequest(Matchers.argThat(MatcherUtils.mapDoesNotContainsKey(assignedRequestsToRequestResolverMap)))).thenThrow(new IllegalArgumentException("Unknown token. Request possibly not assigned yet."));

        doVoidAnswer(invocationOnMock -> {
            final IToken<?> requestToken = invocationOnMock.getArgumentAt(0, IToken.class);
            final RequestState requestState = invocationOnMock.getArgumentAt(1, RequestState.class);
        });

        doVoidAnswer(invocation -> {
            final IRequestResolverProvider provider = invocation.getArgumentAt(0, IRequestResolverProvider.class);

            requestResolverProviderBiMap.put(provider.getId(), provider);

            provider.getResolvers().forEach(resolver -> {
                requestResolverBiMap.put(resolver.getId(), resolver);
                requestResolverProviderToRequestResolverMultiMap.put(provider.getId(), resolver.getId());
                requestResolverToRequestResolverProviderMap.put(resolver.getId(), provider.getId());
            });
        }).when(requestManager).onProviderAddedToColony(Mockito.any());

        doVoidAnswer(invocation -> {
            final IRequestResolverProvider requestResolverProvider = invocation.getArgumentAt(0, IRequestResolverProvider.class);

            //Get the resolvers that are being removed.
            final Collection<IToken<?>> assignedResolvers = requestResolverProviderToRequestResolverMultiMap.get(requestResolverProvider.getId());

            if(assignedResolvers == null)
            {
                return;
            }

            for (final IToken<?> resolverToken : assignedResolvers)
            {
                //Skip if the resolver has no requests assigned.
                if (!requestResolverToAssignedRequestMultiMap.containsKey(resolverToken))
                {
                    requestResolverBiMap.remove(resolverToken);
                    requestResolverProviderToRequestResolverMultiMap.remove(requestResolverProvider.getId(), resolverToken);
                    requestResolverToRequestResolverProviderMap.remove(resolverToken);

                    continue;
                }

                //Clone the original list to modify it during iteration, if need be.
                final Collection<IToken<?>> assignedRequests = new ArrayList<>(requestResolverToAssignedRequestMultiMap.get(resolverToken));

                //Get all assigned requests and reassign them.
                for (final IToken<?> requestToken : assignedRequests)
                {
                    requestManager.reassignRequest(requestToken, assignedResolvers);
                }

                requestResolverBiMap.remove(resolverToken);
                requestResolverProviderToRequestResolverMultiMap.remove(requestResolverProvider.getId(), resolverToken);
                requestResolverToRequestResolverProviderMap.remove(resolverToken);

                requestManager.getLogger().info("Finished reassignment of already registered requests registered to resolver with token: " + resolverToken);
            }

            requestResolverProviderBiMap.remove(requestResolverProvider.getId());
        }).when(requestManager).onProviderRemovedFromColony(Mockito.any());

        doVoidAnswer(invocation -> {
            final Predicate<IRequest> shouldReassignPredicate = invocation.getArgumentAt(0, Predicate.class);

            requestResolverBiMap.values().forEach(resolver -> resolver.onColonyUpdate(requestManager, shouldReassignPredicate));
        }).when(requestManager).onColonyUpdate(Mockito.any());


        return requestManager;
    }
}
