package com.minecolonies.coremod.test;

import com.minecolonies.api.compatibility.CompatibilityManager;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.ColonyManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Abstract test class to abstract away some common uses functionality in Tests
 */
@PrepareForTest({ColonyManager.class, LanguageHandler.class, Log.class})
@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
public abstract class AbstractMockStaticsTest
{
    @Mock
    private Logger logger;

    @Before
    public void setupStaticMocks() throws Exception
    {
        CompatibilityManager testedClass = Mockito.spy(new CompatibilityManager());
        doNothing().when(testedClass).discover();

        mockStatic(ColonyManager.class);
        mockStatic(LanguageHandler.class);
        mockStatic(Log.class);

        doNothing().when(LanguageHandler.class, "sendPlayerMessage", anyObject(), anyString());
        doNothing().when(LanguageHandler.class, "sendPlayersMessage", anyObject(), anyString());

        doReturn(logger).when(Log.class, "getLogger");
    }
}
