package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static com.minecolonies.api.util.constant.WindowConstants.HAPPINESS_LABEL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

//@RunWith(MockitoJUnitRunner.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest({ColonyView.class})
@PowerMockIgnore( {"javax.management.*"})
public class WindowTownHallTest {

    @Mock
    private ITownHallView townHall;
    @Mock
    private ITownHallView building;

    @Mock
    private IColonyView colony;

    final private Map<Integer, CitizenDataView> citizensMap = new HashMap<>();
    final private List<CitizenDataView> citizensArray = new ArrayList<>();

    @Before
    public void setUp()
    {
        final String[] jobs =
        {
                "com.minecolonies.coremod.gui.townHall.population.deliverymen",
                "com.minecolonies.coremod.gui.townHall.population.miners",
                "com.minecolonies.coremod.gui.townHall.population.fishermen",
                "com.minecolonies.coremod.gui.townHall.population.guards",
                "com.minecolonies.coremod.gui.townHall.population.lumberjacks",
                "com.minecolonies.coremod.gui.townHall.population.farmers",
                "com.minecolonies.coremod.gui.townHall.population.bakers",
                "com.minecolonies.coremod.gui.townHall.population.builders",
                "com.minecolonies.coremod.gui.townHall.population.cooks"
        };

        for (int i = 0; i < jobs.length; i++)
        {
            final CitizenDataView citizen = mock(CitizenDataView.class);
            when(citizen.getId()).thenReturn(i);
            when(citizen.getJob()).thenReturn(jobs[i]);
            citizensMap.put(citizen.getId(), citizen);
            citizensArray.add(citizen);
        }

    }

    @Test
    public void testJobs() throws Exception {
        final WindowTownHall windowTownHall = mock(WindowTownHall.class);

        when(colony.getCitizens()).thenReturn(Collections.unmodifiableMap(citizensMap));
        when(colony.getCitizenCount()).thenReturn(4);
        when(townHall.getColony()).thenReturn(colony);
        when(building.getColony()).thenReturn(colony);
        when(windowTownHall.findPaneOfTypeByID(HAPPINESS_LABEL, Label.class)).thenReturn(new Label());
        when(windowTownHall.findPaneOfTypeByID("citizen-stats", ScrollingList.class)).thenReturn(null);

        Whitebox.setInternalState(windowTownHall, "townHall", townHall);
        Whitebox.setInternalState(windowTownHall, "citizens", citizensArray);
        Whitebox.setInternalState(windowTownHall, "building", building);

        Whitebox.setInternalState(windowTownHall, "townHall", townHall);
        Whitebox.invokeMethod(windowTownHall, "createAndSetStatistics");
        assertEquals(1L, citizensMap.get(1).getId());

        verify(windowTownHall, times(1)).findPaneOfTypeByID("citizen-stats", ScrollingList.class);
    }
}