package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.research.IResearchRequirement;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Certain building research requirements.
 */
public class BuildingResearchRequirement implements IResearchRequirement
{
    /**
     * The building level.
     */
    private final int buildingLevel;

    /**
     * The building desc.
     */
    private final String building;

    /**
     * Create a building based research requirement.
     * @param buildingLevel the required building level.
     * @param building the required building class.
     */
    public BuildingResearchRequirement(final int buildingLevel, final String building)
    {
        this.buildingLevel = buildingLevel;
        this.building = building;
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            int sum = 0;
            if (building.getSchematicName().equalsIgnoreCase(this.building))
            {
                sum += building.getBuildingLevel();

                if (sum >= this.buildingLevel)
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        return new TranslationTextComponent("com.minecolonies.coremod.research.requirement.building.level", this.building, this.buildingLevel);
    }
}
