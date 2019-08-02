package com.minecolonies.coremod.colony.requestsystem.management.handlers;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.test.AbstractMockStaticsTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest(LogManager.class)
public class LogHandlerTest extends AbstractMockStaticsTest
{

    private Logger mockedLogger;

    @Before
    public void setUp() throws Exception
    {
        mockedLogger = Mockito.mock(Logger.class);
        mockStatic(LogManager.class);
        when(LogManager.getLogger(Mockito.anyString())).thenReturn(mockedLogger);

        Configurations.requestSystem.enableDebugLogging = false;
    }

    @Test
    public void logWhenEnabledInConfig()
    {
        Configurations.requestSystem.enableDebugLogging = true;
        LogHandler.log("Test");

        verify(mockedLogger, times(1)).info("Test");
    }

    @Test
    public void doNotLogWhenDisabledInConfig()
    {
        Configurations.requestSystem.enableDebugLogging = false;
        LogHandler.log("Test");

        verify(mockedLogger, never()).info("Test");
    }

    @After
    public void tearDown()
    {
        Configurations.requestSystem.enableDebugLogging = false;
    }
}