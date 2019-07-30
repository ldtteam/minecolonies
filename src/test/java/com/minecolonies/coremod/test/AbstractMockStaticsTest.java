package com.minecolonies.coremod.test;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.compatibility.CompatibilityManager;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.apiimp.MinecoloniesApiImpl;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ICitizenDataManager;
import com.minecolonies.coremod.colony.IColonyManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
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

    protected IColonyManager colonyManagerToMock;
    protected ICitizenDataManager citizenDataManagerToMock;

    @Before
    public void setupStaticMocks() throws Exception
    {
        IMinecoloniesAPI mockedMinecoloniesApi = Mockito.mock(IMinecoloniesAPI.class);

        colonyManagerToMock = Mockito.mock(IColonyManager.class);
        citizenDataManagerToMock = Mockito.mock(ICitizenDataManager.class);

        when(mockedMinecoloniesApi.getColonyManager()).thenReturn(colonyManagerToMock);
        when(mockedMinecoloniesApi.getCitizenDataManager()).thenReturn(citizenDataManagerToMock);

        MinecoloniesAPIProxy.getInstance().setApiInstance(mockedMinecoloniesApi);

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
