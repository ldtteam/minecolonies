package com.minecolonies.api.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.research.interfaces.IResearchRequirement;
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
        return colony.getBuildingManager().getBuildings().values().stream().anyMatch(b -> b.getBuildingLevel() >= this.buildingLevel && b.getSchematicName().equalsIgnoreCase(this.building));
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        return new TranslationTextComponent("com.minecolonies.coremod.research.requirement.building.level", this.building, this.buildingLevel);
    }
}
