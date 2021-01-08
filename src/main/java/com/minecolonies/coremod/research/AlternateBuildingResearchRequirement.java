package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.util.Log;
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
     * The identifier tag for this type of requirement.
     */
    public static final String type = "alt-buildings";

    /**
     * The list of buildings, by level.
     */
    final private Map<String, Integer> buildings = new HashMap<>();

    /**
     * Create a building-based research requirement, that requires one of multiple buildings be constructed.
     * @param building    the name of the building
     * @param level       the level requirement of the building
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
     * Creates and return an empty alternate building requirement.
     */
    public AlternateBuildingResearchRequirement()
    {
        // Intentionally empty.
    }

    /**
     * Creates an Alternate building requirement from an attributes string array.
     * See getAttributes for the format.
     * @param attributes        An attributes array describing the research requirement.
     */
    public AlternateBuildingResearchRequirement(String[] attributes)
    {
        if(!attributes[0].equals(type) || attributes.length < 3 || (attributes.length % 2) == 0)
        {
            Log.getLogger().error("Error parsing received AlternateBuildingResearch.");
        }
        else
        {
            for(int i = 1; i < attributes.length; i+=2)
            {
                buildings.put(attributes[i], Integer.parseInt(attributes[i + 1]));
            }
        }
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
            else if(colony != null)
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
    public String getAttributes()
    {
        StringBuilder s = new StringBuilder(type);
        for(Map.Entry<String, Integer> building : buildings.entrySet())
        {
            s.append("`").append(building.getKey()).append("`").append(building.getValue());
        }
        return s.toString();
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
