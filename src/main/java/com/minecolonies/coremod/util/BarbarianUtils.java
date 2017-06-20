package com.minecolonies.coremod.util;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Utils for the Barbarians
 */
public class BarbarianUtils
{

    /**
     * Takes a colony and spits out that colony's RaidLevel
     *
     * @param colony The colony to use
     * @return an int describing the raid level
     */
    public static int getColonyRaidLevel(final Colony colony)
    {
        int levels = 0;

        @NotNull final List<CitizenData> citizensList = new ArrayList<>();
        citizensList.addAll(colony.getCitizens().values());

        for (@NotNull final CitizenData citizen : citizensList)
        {
            if (citizen.getJob() != null && citizen.getWorkBuilding() != null)
            {
                final int buildingLevel = citizen.getWorkBuilding().getBuildingLevel();
                levels += buildingLevel;
            }
        }

        if (colony.getTownHall() != null)
        {
            return (levels + colony.getTownHall().getBuildingLevel());
        }
        else
        {
            return 0;
        }
    }
}
