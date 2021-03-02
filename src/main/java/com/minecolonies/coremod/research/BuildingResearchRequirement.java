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
     * The NBT tag for if a requirement must be filled by a single building.
     */
    private static final String TAG_BUILDING_SINGLE = "building-single";

    /**
     * The building level.
     */
    private final int    buildingLevel;
    /**
     * The building desc.
     */
    private final String building;
    /**
     * If true, requires that a single building meet the level requirements.
     */
    private final boolean singleBuilding;

    /**
     * Create a building based research requirement.
     *
     * @param buildingLevel  the required building level.
     * @param building       the required building class.
     * @param singleBuilding if true, must be fulfilled by a single building in the colony.
     *                       Otherwise, will be fulfilled if all buildings of the schematic combined meet the requirement.
     */
    public BuildingResearchRequirement(final int buildingLevel, final String building, final boolean singleBuilding)
    {
        this.buildingLevel = buildingLevel;
        this.building = building;
        this.singleBuilding = singleBuilding;
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
        this.singleBuilding = nbt.getBoolean(TAG_BUILDING_SINGLE);
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
                    if(singleBuilding)
                    {
                        if(building.getBuildingLevel() >= this.buildingLevel)
                        {
                            return true;
                        }
                    }
                    else
                    {
                        sum += building.getBuildingLevel();

                        if (sum >= this.buildingLevel)
                        {
                            return true;
                        }
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
                    if(singleBuilding)
                    {
                        if(building.getBuildingLevel() >= this.buildingLevel)
                        {
                            return true;
                        }
                    }
                    else
                    {
                        sum += building.getBuildingLevel();

                        if (sum >= this.buildingLevel)
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        if(singleBuilding)
        {
            return new TranslationTextComponent("com.minecolonies.coremod.research.requirement.building.mandatory.level",
              new TranslationTextComponent("block.minecolonies.blockhut" + this.building),
              this.buildingLevel);
        }
        else
        {
            return new TranslationTextComponent("com.minecolonies.coremod.research.requirement.building.level",
              new TranslationTextComponent("block.minecolonies.blockhut" + this.building),
              this.buildingLevel);
        }
    }

    @Override
    public ResearchRequirementEntry getRegistryEntry() {return ModResearchRequirements.buildingResearchRequirement;}

    @Override
    public CompoundNBT writeToNBT()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(TAG_BUILDING_NAME, building);
        nbt.putInt(TAG_BUILDING_LVL, buildingLevel);
        nbt.putBoolean(TAG_BUILDING_SINGLE, singleBuilding);
        return nbt;
    }
}
