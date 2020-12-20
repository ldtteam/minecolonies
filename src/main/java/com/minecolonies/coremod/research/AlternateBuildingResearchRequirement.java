package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.research.IResearchRequirement;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Requires one out of a list of buildings to be present.
 */
public class AlternateBuildingResearchRequirement implements IResearchRequirement
{
    /**
     * The list of buildings, by level.
     */
    final private Map<String, Integer> buildings = new HashMap<>();

    /**
     * Create a building-based research requirement, that requires one of multiple buildings be constructed.
     *
     */
    public AlternateBuildingResearchRequirement add(String building, int level)
    {
        if(buildings.containsKey(building))
        {
            buildings.put(building, buildings.get(building) + level);
        }
        else
        {
            buildings.put(building, level);
        }
        return this;
    }


    /**
     * @return the building description
     */
    public Map<String, Integer> getBuildings()
    {
        return buildings;
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        for(Map.Entry<String, Integer> requirement : buildings.entrySet())
        {
            int sum = 0;
            if(colony instanceof IColonyView)
            {
                for (final IBuildingView building : ((IColonyView) colony).getBuildings())
                {
                    if (building.getSchematicName().equals(requirement.getKey()))
                    {
                        sum += building.getBuildingLevel();

                        if(sum >= requirement.getValue())
                        {
                            return true;
                        }
                    }
                }
            }
            else if(colony instanceof IColony)
            {
                for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
                {
                    if (building.getSchematicName().equalsIgnoreCase(requirement.getKey()))
                    {
                        sum += building.getBuildingLevel();

                        if (sum >= requirement.getValue())
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        final TranslationTextComponent requirementList = new TranslationTextComponent("");
        final Iterator<Map.Entry<String, Integer>> iterator = buildings.entrySet().iterator();
        while(iterator.hasNext())
        {
            final Map.Entry<String, Integer> kvp = iterator.next();
            requirementList.append(new TranslationTextComponent("com.minecolonies.coremod.research.requirement.building.level", new TranslationTextComponent("block.minecolonies.blockhut" + kvp.getKey()), kvp.getValue()));
            if(iterator.hasNext())
            {
                requirementList.append(new TranslationTextComponent("com.minecolonies.coremod.research.requirement.building.or"));
            }
        }
        return requirementList;
    }
}