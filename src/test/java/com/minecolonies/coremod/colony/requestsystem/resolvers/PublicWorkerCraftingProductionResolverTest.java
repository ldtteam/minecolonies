package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.manager.RequestMappingHandler;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.requestsystem.token.StandardToken;
import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.colony.requestsystem.locations.StaticLocation;
import net.minecraft.util.math.BlockPos;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import scala.tools.nsc.doc.model.Public;

import javax.tools.StandardLocation;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

public class PublicWorkerCraftingProductionResolverTest
{

    private PublicWorkerCraftingProductionResolver testSubject = new PublicWorkerCraftingProductionResolver(
      new StaticLocation(new BlockPos(0,0,0), 0),
      new StandardToken(UUID.randomUUID())
    );

    private IRequestManager requestManager;

    private BiMap<IToken<?>, IRequest<?>> mockedRequests = HashBiMap.create();

    @Before
    public void setUp() throws Exception
    {
        StandardFactoryControllerInitializer.onPreInit();
        requestManager = Mockito.mock(IRequestManager.class);

        when(requestManager.createRequest(Mockito.any(IRequester.class), Mockito.any())).thenAnswer((Answer<IToken<?>>) invocation -> {
            final IToken<?> token = new StandardToken(UUID.randomUUID());
            final IRequester requester = (IRequester) invocation.getArguments()[0];
            final Object requestable = invocation.getArguments()[1];

            final IRequest<?> request = StandardFactoryController.getInstance().getNewInstance(TypeToken.of((Class<? extends IRequest<?>>) RequestMappingHandler.getRequestableMappings()
                                                                                                                               .get(requestable.getClass())), requestable, token, requester);

            return request.getId();
        });
    }



    @Test
    public void canResolveRequest()
    {
    }

    @After
    public void tearDown() throws Exception
    {
        StandardFactoryController.reset();
    }
}