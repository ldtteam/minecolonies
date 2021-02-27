package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Certain building research requirements.
 */
public class BuildingResearchRequirement implements IResearchRequirement
{
    /**
     * The NBT tag for an individual building's name.
     */
    private static final String TAG_BUILDING_NAME = "building-name";

    /**
     * The NBT tag for an individual building's required level.
     */
    private static final String TAG_BUILDING_LVL = "building-lvl";

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
     * Create a building-based research requirement.
     *
     * @param nbt           the nbt containing the relevant tags.
     */
    public BuildingResearchRequirement(CompoundNBT nbt)
    {
        this.buildingLevel = nbt.getInt(TAG_BUILDING_LVL);
        this.building = nbt.getString(TAG_BUILDING_NAME);
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
    public TranslationTextComponent getDesc()
    {
        return new TranslationTextComponent("com.minecolonies.coremod.research.requirement.building.level", new TranslationTextComponent("block.minecolonies.blockhut" + this.building), this.buildingLevel);
    }

    @Override
    public ResearchRequirementEntry getRegistryEntry() {return ModResearchRequirements.buildingResearchRequirement;}

    @Override
    public CompoundNBT writeToNBT()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(TAG_BUILDING_NAME, building);
        nbt.putInt(TAG_BUILDING_LVL, buildingLevel);
        return nbt;
    }
}
