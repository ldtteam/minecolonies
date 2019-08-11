package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.init.StandardFactoryControllerInitializer;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.test.AbstractMockStaticsTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class TokenHandlerTest extends AbstractMockStaticsTest
{

    @Mock
    private IStandardRequestManager requestManager;

    @Before
    public void setUp() throws Exception
    {
        StandardFactoryControllerInitializer.onPreInit();
    }

    @After
    public void tearDown() throws Exception
    {
        StandardFactoryController.reset();
    }

    @Test
    public void generateNewToken()
    {
        when(requestManager.getFactoryController()).thenReturn(StandardFactoryController.getInstance());

        final ITokenHandler tokenHandler = new TokenHandler(requestManager);
        final IToken<?> token = tokenHandler.generateNewToken();

        assertNotNull(token);
    }
}