package com.minecolonies.coremod.test;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.ColonyManager;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Random;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Abstract test class to abstract away some common uses functionality in Tests
 */
@PrepareForTest({ColonyManager.class, LanguageHandler.class, Log.class, ItemStack.class})
@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
public abstract class AbstractTest
{

    private Random random;

    @Before
    public void setupStaticMocks() throws Exception
    {
        mockStatic(ColonyManager.class);
        mockStatic(LanguageHandler.class);
        mockStatic(Log.class);

        doNothing().when(LanguageHandler.class, "sendPlayerMessage", anyObject(), anyString());
        doNothing().when(LanguageHandler.class, "sendPlayersMessage", anyObject(), anyString());

        final Logger logger = LogManager.getLogger(getTestName());
        random = new Random(getTestName().hashCode());

        doReturn(logger).when(Log.class, "getLogger");
    }

    public String getTestName()
    {
        return "AbstractTest";
    }

    public Random getRandom()
    {
        return random;
    }
}
