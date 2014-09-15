package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.*;

public class BuildingHome extends BuildingHut
{
    private Set<UUID> citizens = new HashSet<UUID>();

    public BuildingHome(Colony c, ChunkCoordinates l)
    {
        super(c, l);
    }

    public int getMaxInhabitants() { return 2; }

    public void onDestroyed()
    {
        //  TODO REFACTOR - Ideally we should have a live Map of WeakReferences to our EntityCitizens
        World world = DimensionManager.getWorld(getColony().getDimensionId());
        if (world == null)
        {
            return;
        }

        List<Entity> entityCitizens = Utils.getEntitiesFromUUID(world, new ArrayList<UUID>(citizens));
        if(entityCitizens != null)
        {
            for(Entity entity : entityCitizens)
            {
                if(entity instanceof EntityCitizen)
                {
                    EntityCitizen citizen = (EntityCitizen) entity;
                    citizen.setHomeBuilding(null);
                }
            }
        }

        super.onDestroyed();
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
