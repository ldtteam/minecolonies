package com.minecolonies.core.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
    public BuildingResearchRequirement(CompoundTag nbt)
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
        return colony.hasBuilding(this.building, this.buildingLevel, this.singleBuilding);
    }

    @Override
    public MutableComponent getDesc()
    {
        if(singleBuilding)
        {
            return Component.translatableEscape("com.minecolonies.coremod.research.requirement.building.mandatory.level",
              Component.translatableEscape("com.minecolonies.building." + building),
              this.buildingLevel);
        }
        else
        {
            return Component.translatableEscape("com.minecolonies.coremod.research.requirement.building.level",
              Component.translatableEscape("com.minecolonies.building." + building),
              this.buildingLevel);
        }
    }

    @Override
    public ResearchRequirementEntry getRegistryEntry() {return ModResearchRequirements.buildingResearchRequirement.get();}

    @Override
    public CompoundTag writeToNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(TAG_BUILDING_NAME, building);
        nbt.putInt(TAG_BUILDING_LVL, buildingLevel);
        nbt.putBoolean(TAG_BUILDING_SINGLE, singleBuilding);
        return nbt;
    }
}
