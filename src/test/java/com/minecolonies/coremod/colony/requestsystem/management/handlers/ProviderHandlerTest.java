package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.data.IProviderResolverAssignmentDataStore;
import com.minecolonies.api.colony.requestsystem.data.IRequestResolverRequestAssignmentDataStore;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.requestsystem.token.StandardToken;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.test.AbstractMockStaticsTest;
import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class ProviderHandlerTest extends AbstractMockStaticsTest
{

    @Mock
    private IStandardRequestManager                    requestManager;
    @Mock
    private IColony                                    colony;
    @Mock
    private IProviderResolverAssignmentDataStore       providerResolverAssignmentDataStore;
    @Mock
    private IRequestResolverRequestAssignmentDataStore requestResolverRequestAssignmentDataStore;
    @Mock
    private IRequestResolverProvider                   requestResolverProvider;

    private ProviderHandler providerHandler;


    @Before
    public void setUp() throws Exception
    {
        when(requestResolverProvider.getId()).thenReturn(new StandardToken());
        when(requestManager.getColony()).thenReturn(colony);
        when(requestManager.getProviderResolverAssignmentDataStore()).thenReturn(providerResolverAssignmentDataStore);
        when(requestManager.getRequestResolverRequestAssignmentDataStore()).thenReturn(requestResolverRequestAssignmentDataStore);
        when(requestManager.getLogger()).thenReturn(LogManager.getLogger("minecolonies.requestsystem.test"));

        providerHandler = Mockito.spy(new ProviderHandler(requestManager));
    }

    @Test
    public void getRegisteredResolversDirectViaId()
    {
        final Collection<IToken<?>> input = Lists.newArrayList(new StandardToken(), new StandardToken());
        final IToken<?> inputId = new StandardToken();

        final Map<IToken<?>, Collection<IToken<?>>> assigmentMap = ImmutableMap.<IToken<?>, Collection<IToken<?>>>builder().put(inputId, input).build();

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(assigmentMap);

        final IProviderHandler providerHandler = new ProviderHandler(requestManager);
        assertEquals(input, providerHandler.getRegisteredResolvers(inputId));
    }

    @Test
    public void getRegisteredResolversViaProvider()
    {
        final Collection<IToken<?>> input = Lists.newArrayList(new StandardToken(), new StandardToken());
        final IToken<?> inputId = requestResolverProvider.getId();

        final Map<IToken<?>, Collection<IToken<?>>> assigmentMap = ImmutableMap.<IToken<?>, Collection<IToken<?>>>builder().put(inputId, input).build();

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(assigmentMap);
        when(requestResolverProvider.getId()).thenReturn(inputId);

        final IProviderHandler providerHandler = new ProviderHandler(requestManager);
        assertEquals(input, providerHandler.getRegisteredResolvers(requestResolverProvider));
    }

    @Test
    public void getUnknownResolversDirectViaId()
    {
        final Collection<IToken<?>> input = Lists.newArrayList(new StandardToken(), new StandardToken());
        final IToken<?> inputId = new StandardToken();
        final IToken<?> mappedId = new StandardToken();

        final Map<IToken<?>, Collection<IToken<?>>> assigmentMap = ImmutableMap.<IToken<?>, Collection<IToken<?>>>builder().put(mappedId, input).build();

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(assigmentMap);
        assertEquals(ImmutableList.of(), providerHandler.getRegisteredResolvers(inputId));
    }

    @Test
    public void getUnknownResolversViaProvider()
    {
        final Collection<IToken<?>> input = Lists.newArrayList(new StandardToken(), new StandardToken());
        final IToken<?> mappedId = new StandardToken();

        final Map<IToken<?>, Collection<IToken<?>>> assigmentMap = ImmutableMap.<IToken<?>, Collection<IToken<?>>>builder().put(mappedId, input).build();

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(assigmentMap);
        assertEquals(ImmutableList.of(), providerHandler.getRegisteredResolvers(requestResolverProvider));
    }

    //@Test Disabled due to weird mockito issue.
    public void registerProvider()
    {
        final Map<IToken<?>, Collection<IToken<?>>> providerResolverAssignments = Mockito.spy(Maps.newHashMap());

        final IResolverHandler resolverHandler = Mockito.mock(IResolverHandler.class);
        when(resolverHandler.registerResolvers(Mockito.anyCollection())).thenAnswer(invocation -> {
            final Collection<IRequestResolver<?>> resolvers = invocation.getArgumentAt(1, Collection.class);
            return resolvers.stream().map(IRequester::getId).collect(Collectors.toList());
        });

        when(requestManager.getResolverHandler()).thenReturn(resolverHandler);

        final IRequestResolver<IRequestable> resolver1 = Mockito.mock(IRequestResolver.class);
        final IRequestResolver<IRequestable> resolver2 = Mockito.mock(IRequestResolver.class);

        final IToken<?> resolver1Token = new StandardToken();
        final IToken<?> resolver2Token = new StandardToken();

        when(resolver1.getId()).thenAnswer(invocation -> resolver1Token);
        when(resolver2.getId()).thenAnswer(invocation -> resolver2Token);

        when(requestResolverProvider.getResolvers()).thenReturn(ImmutableList.of(resolver1, resolver2));

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(providerResolverAssignments);
        providerHandler.registerProvider(requestResolverProvider);

        verify(colony, times(1)).markDirty();

        assertEquals(1, providerResolverAssignments.size());
        assertEquals(2, providerResolverAssignments.get(requestResolverProvider.getId()).size());
        assertTrue(providerResolverAssignments.get(requestResolverProvider.getId()).contains(resolver1Token));
        assertTrue(providerResolverAssignments.get(requestResolverProvider.getId()).contains(resolver2Token));
    }

    @Test
    public void removeProviderUsingId() throws Exception
    {
        doCallRealMethod().when(providerHandler).removeProvider(Mockito.any(IToken.class));

        providerHandler.removeProvider(requestResolverProvider.getId());

        verify(providerHandler).removeProviderInternal(requestResolverProvider.getId());
    }

    @Test
    public void removeProviderUsingInstance() throws Exception
    {
        doCallRealMethod().when(providerHandler).removeProvider(Mockito.any(IRequestResolverProvider.class));

        providerHandler.removeProvider(requestResolverProvider);

        verify(providerHandler).removeProviderInternal(requestResolverProvider.getId());
    }

    //@Test Disabled due to some weird Mockito issue with stubbing......
    public void removeProviderInternal() throws Exception
    {
        Map<IToken<?>, Collection<IToken<?>>> providerResolverAssignmentsMap = Maps.newHashMap();
        providerResolverAssignmentsMap.put(requestResolverProvider.getId(), Lists.newArrayList());

        doReturn(providerResolverAssignmentsMap).when(providerResolverAssignmentDataStore).getAssignments();

        when(providerHandler.getRegisteredResolvers(requestResolverProvider.getId())).thenReturn(Lists.newArrayList());
        doCallRealMethod().when(providerHandler).removeProviderInternal(requestResolverProvider.getId());

        providerHandler.removeProviderInternal(requestResolverProvider.getId());

        assertEquals(0, providerResolverAssignmentsMap.size());
        verify(providerHandler).processResolversForRemoval(Lists.newArrayList());
        verify(colony).markDirty();
    }

    @Test
    public void processResolversForRemoval() throws Exception
    {
        final IToken<?> token = new StandardToken();
        final List<IToken<?>> tokens = Lists.newArrayList(token);

        doNothing().when(providerHandler).processResolverForRemoval(tokens, token);

        providerHandler.processResolversForRemoval(null);
        verify(providerHandler, times(0)).processResolverForRemoval(Lists.newArrayList(), null);

        providerHandler.processResolversForRemoval(Lists.newArrayList());
        verify(providerHandler, times(0)).processResolverForRemoval(Lists.newArrayList(), null);

        providerHandler.processResolversForRemoval(tokens);
        verify(providerHandler, times(1)).processResolverForRemoval(tokens, token);

        final IToken<?> tokenTwo = new StandardToken();
        tokens.add(tokenTwo);
        doNothing().when(providerHandler).processResolverForRemoval(tokens, tokenTwo);
        providerHandler.processResolversForRemoval(tokens);
        verify(providerHandler, times(2)).processResolverForRemoval(tokens, token);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processResolverForRemovalNotContained()
    {
        final IToken<?> token = new StandardToken();
        final List<IToken<?>> tokens = Lists.newArrayList();

        providerHandler.processResolverForRemoval(tokens, token);
    }

    @Test
    public void processResolverForRemovalNoAssignedNotContained()
    {
        final IToken<?> token = new StandardToken();
        final List<IToken<?>> tokens = Lists.newArrayList(token);

        when(requestResolverRequestAssignmentDataStore.getAssignments()).thenReturn(Maps.newHashMap());

        doNothing().when(providerHandler).removeResolverWithoutAssignedRequests(token);
        doNothing().when(providerHandler).removeResolverWithAssignedRequests(tokens, token);

        providerHandler.processResolverForRemoval(tokens, token);
        verify(providerHandler).removeResolverWithoutAssignedRequests(token);
        verify(providerHandler, times(0)).removeResolverWithAssignedRequests(tokens, token);
    }

    @Test
    public void processResolverForRemovalNoAssignedEmptyList()
    {
        final IToken<?> token = new StandardToken();
        final List<IToken<?>> tokens = Lists.newArrayList(token);

        final Map<IToken<?>, Collection<IToken<?>>> assignments = new HashMap<>();
        assignments.put(token, Lists.newArrayList());

        when(requestResolverRequestAssignmentDataStore.getAssignments()).thenReturn(assignments);

        doNothing().when(providerHandler).removeResolverWithoutAssignedRequests(token);
        doNothing().when(providerHandler).removeResolverWithAssignedRequests(tokens, token);

        providerHandler.processResolverForRemoval(tokens, token);
        verify(providerHandler).removeResolverWithoutAssignedRequests(token);
        verify(providerHandler, times(0)).removeResolverWithAssignedRequests(tokens, token);
    }

    @Test
    public void processResolverForRemovalAssigned()
    {
        final IToken<?> token = new StandardToken();
        final List<IToken<?>> tokens = Lists.newArrayList(token);

        final Map<IToken<?>, Collection<IToken<?>>> assignments = new HashMap<>();
        assignments.put(token, Lists.newArrayList(token));

        when(requestResolverRequestAssignmentDataStore.getAssignments()).thenReturn(assignments);

        doNothing().when(providerHandler).removeResolverWithoutAssignedRequests(token);
        doNothing().when(providerHandler).removeResolverWithAssignedRequests(tokens, token);

        providerHandler.processResolverForRemoval(tokens, token);
        verify(providerHandler, times(0)).removeResolverWithoutAssignedRequests(token);
        verify(providerHandler, times(1)).removeResolverWithAssignedRequests(tokens, token);
    }

    @Test
    public void removeResolverWithAssignedRequests()
    {
    }

    @Test
    public void removeResolverWithoutAssignedRequests()
    {
    }
}