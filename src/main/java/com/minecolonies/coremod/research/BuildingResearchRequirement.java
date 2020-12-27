package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.util.Log;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Certain building research requirements.
 */
public class BuildingResearchRequirement implements IResearchRequirement
{
    /**
     * The identifier tag for this type of requirement.
     */
    public static final String type = "building";
    /**
     * The building level.
     */
    private final int    buildingLevel;
    /**
     * The building desc.
     */
    private final String building;

    /**
     * Create a building based research requirement.
     *
     * @param buildingLevel the required building level.
     * @param building      the required building class.
     */
    public BuildingResearchRequirement(final int buildingLevel, final String building)
    {
        this.buildingLevel = buildingLevel;
        this.building = building;
    }

    /**
     * Creates an building requirement from an attributes string array.
     * See getAttributes for the format.
     * @param attributes        An attributes array describing the research requirement.
     */
    public BuildingResearchRequirement(String[] attributes)
    {
        if(!attributes[0].equals(type) || attributes.length < 3)
        {
            Log.getLogger().error("Error parsing received BuildingResearchRequirement.");
            building = "";
            buildingLevel = 0;
        }
        else
        {
             building = attributes[1];
             buildingLevel = Integer.parseInt(attributes[2]);
        }
    }

    /**
     * @return the building description
     */
    public String getBuilding()
    {
        return building;
    }

    /**
     * @return the building level
     */
    public int getBuildingLevel()
    {
        return buildingLevel;
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        int sum = 0;
        if(colony instanceof IColonyView)
        {
            for (final IBuildingView building : ((IColonyView) colony).getBuildings())
            {
                if (building.getSchematicName().equals(this.getBuilding()))
                {
                    sum += building.getBuildingLevel();

                    if(sum >= this.buildingLevel)
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
                if (building.getSchematicName().equalsIgnoreCase(this.building))
                {
                    sum += building.getBuildingLevel();

                    if (sum >= this.buildingLevel)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String getAttributes()
    {
        return type + ":" + building + ":" + buildingLevel;
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        return new TranslationTextComponent("com.minecolonies.coremod.research.requirement.building.level", new TranslationTextComponent("block.minecolonies.blockhut" + this.building), this.buildingLevel);
    }
}
