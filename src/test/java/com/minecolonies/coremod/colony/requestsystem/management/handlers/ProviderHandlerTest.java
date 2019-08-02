package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.data.IProviderResolverAssignmentDataStore;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.requestsystem.token.StandardToken;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.test.AbstractMockStaticsTest;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import scala.sys.process.processInternal;
import scala.tools.nsc.interpreter.Power;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({ResolverHandler.class, ProviderHandler.class})
public class ProviderHandlerTest extends AbstractMockStaticsTest
{

    @Mock
    private IStandardRequestManager requestManager;
    @Mock
    private IColony colony;
    @Mock
    private IProviderResolverAssignmentDataStore providerResolverAssignmentDataStore;
    @Mock
    private IRequestResolverProvider requestResolverProvider;

    @Before
    public void setUp() throws Exception
    {
        when(requestResolverProvider.getId()).thenReturn(new StandardToken());
        when(requestManager.getColony()).thenReturn(colony);
        when(requestManager.getProviderResolverAssignmentDataStore()).thenReturn(providerResolverAssignmentDataStore);
    }

    @Test
    public void getRegisteredResolversDirectViaId()
    {
        final Collection<IToken<?>> input = Lists.newArrayList(new StandardToken(), new StandardToken());
        final IToken<?> inputId = new StandardToken();

        final Map<IToken<?>, Collection<IToken<?>>> assigmentMap = ImmutableMap.<IToken<?>, Collection<IToken<?>>>builder().put(inputId, input).build();

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(assigmentMap);

        assertEquals(input, ProviderHandler.getRegisteredResolvers(requestManager, inputId));
    }

    @Test
    public void getRegisteredResolversViaProvider()
    {
        final Collection<IToken<?>> input = Lists.newArrayList(new StandardToken(), new StandardToken());
        final IToken<?> inputId = requestResolverProvider.getId();

        final Map<IToken<?>, Collection<IToken<?>>> assigmentMap = ImmutableMap.<IToken<?>, Collection<IToken<?>>>builder().put(inputId, input).build();

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(assigmentMap);
        when(requestResolverProvider.getId()).thenReturn(inputId);

        assertEquals(input, ProviderHandler.getRegisteredResolvers(requestManager, requestResolverProvider));
    }

    @Test
    public void getUnknownResolversDirectViaId()
    {
        final Collection<IToken<?>> input = Lists.newArrayList(new StandardToken(), new StandardToken());
        final IToken<?> inputId = new StandardToken();
        final IToken<?> mappedId = new StandardToken();

        final Map<IToken<?>, Collection<IToken<?>>> assigmentMap = ImmutableMap.<IToken<?>, Collection<IToken<?>>>builder().put(mappedId, input).build();

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(assigmentMap);

        assertEquals(ImmutableList.of(), ProviderHandler.getRegisteredResolvers(requestManager, inputId));
    }

    @Test
    public void getUnknownResolversViaProvider()
    {
        final Collection<IToken<?>> input = Lists.newArrayList(new StandardToken(), new StandardToken());
        final IToken<?> mappedId = new StandardToken();

        final Map<IToken<?>, Collection<IToken<?>>> assigmentMap = ImmutableMap.<IToken<?>, Collection<IToken<?>>>builder().put(mappedId, input).build();

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(assigmentMap);

        assertEquals(ImmutableList.of(), ProviderHandler.getRegisteredResolvers(requestManager, requestResolverProvider));
    }

    @Test
    public void registerProvider()
    {
        final Map<IToken<?>, Collection<IToken<?>>> providerResolverAssignments = Mockito.spy(Maps.newHashMap());

        mockStatic(ResolverHandler.class);
        when(ResolverHandler.registerResolvers(Mockito.any(), Mockito.anyCollection())).thenAnswer(invocation -> {
            final Collection<IRequestResolver<?>> resolvers = invocation.getArgumentAt(1, Collection.class);
            return resolvers.stream().map(IRequester::getId).collect(Collectors.toList());
        });

        final IRequestResolver<IRequestable> resolver1 = Mockito.mock(IRequestResolver.class);
        final IRequestResolver<IRequestable> resolver2 = Mockito.mock(IRequestResolver.class);

        final IToken<?> resolver1Token = new StandardToken();
        final IToken<?> resolver2Token = new StandardToken();

        when(resolver1.getId()).thenAnswer(invocation -> resolver1Token);
        when(resolver2.getId()).thenAnswer(invocation -> resolver2Token);

        when(requestResolverProvider.getResolvers()).thenReturn(ImmutableList.of(resolver1, resolver2));

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(providerResolverAssignments);

        ProviderHandler.registerProvider(requestManager, requestResolverProvider);

        verify(colony, times(1)).markDirty();

        assertEquals(1, providerResolverAssignments.size());
        assertEquals(2, providerResolverAssignments.get(requestResolverProvider.getId()).size());
        assertTrue(providerResolverAssignments.get(requestResolverProvider.getId()).contains(resolver1Token));
        assertTrue(providerResolverAssignments.get(requestResolverProvider.getId()).contains(resolver2Token));
    }

    @Test
    public void removeProviderUsingId() throws Exception
    {
        mockStatic(ProviderHandler.class);

        PowerMockito.when(ProviderHandler.class, "removeProvider", requestManager, requestResolverProvider.getId()).thenCallRealMethod();

        ProviderHandler.removeProvider(requestManager, requestResolverProvider.getId());

        PowerMockito.verifyStatic();
        ProviderHandler.removeProviderInternal(requestManager, requestResolverProvider.getId());
    }

    @Test
    public void removeProviderUsingInstance() throws Exception
    {
        mockStatic(ProviderHandler.class);

        PowerMockito.when(ProviderHandler.class, "removeProvider", requestManager, requestResolverProvider).thenCallRealMethod();

        ProviderHandler.removeProvider(requestManager, requestResolverProvider);

        PowerMockito.verifyStatic();
        ProviderHandler.removeProviderInternal(requestManager, requestResolverProvider.getId());
    }

    @Test
    public void removeProviderInternal() throws Exception
    {
        Map<IToken<?>, Collection<IToken<?>>> providerResolverAssignmentsMap = Maps.newHashMap();
        providerResolverAssignmentsMap.put(requestResolverProvider.getId(), Lists.newArrayList());

        when(providerResolverAssignmentDataStore.getAssignments()).thenReturn(providerResolverAssignmentsMap);
        mockStatic(ProviderHandler.class);

        PowerMockito.when(ProviderHandler.class, "getRegisteredResolvers", requestManager, requestResolverProvider.getId()).thenReturn(Lists.newArrayList());
        PowerMockito.when(ProviderHandler.class, "removeProviderInternal", requestManager, requestResolverProvider.getId()).thenCallRealMethod();

        ProviderHandler.removeProviderInternal(requestManager, requestResolverProvider.getId());

        assertEquals(0, providerResolverAssignmentsMap.size());
        verifyStatic();
        ProviderHandler.processResolversForRemoval(requestManager, Lists.newArrayList());
        verify(colony).markDirty();
    }

    @Test
    public void processResolversForRemoval()
    {
    }

    @Test
    public void processResolverForRemoval()
    {
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