package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.util.ChunkCoordinates;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BuildingHome extends BuildingHut
{
    private Set<UUID> citizens = new HashSet<UUID>();

    public BuildingHome(Colony c, ChunkCoordinates l)
    {
        super(c, l);
        setMaxInhabitants(2);
    }

    @Override
    public void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        if (citizens.size() < getMaxInhabitants())
        {
            //  'Capture' as many citizens into this house as possible
            addHomelessCitizens();
        }
    }

    public void addHomelessCitizens()
    {
        List<EntityCitizen> availableCitizens = getColony().getActiveCitizens();

        for (EntityCitizen c : availableCitizens)
        {
            if (c.getHomeBuilding() == null)
            {
                citizens.add(c.getUniqueID());
                c.setHomeBuilding(this);

                if (citizens.size() >= getMaxInhabitants())
                {
                    break;
                }
            }
        }
    }

    public void removeCitizen(EntityCitizen citizen)
    {
        citizens.remove(citizen.getUniqueID());
        citizen.setHomeBuilding(null);
    }

    public static class View extends BuildingHut.View
    {
        public View(ColonyView c, ChunkCoordinates l)
        {
            super(c, l);
        }
    }
}
