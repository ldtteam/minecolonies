package com.minecolonies.api.research.requirements;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.core.util.GsonHelper;
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
     * The property name for the building.
     */
    private static final String RESEARCH_REQUIREMENT_BUILDING_PROP = "building";

    /**
     * The property name for a numeric level.
     */
    private static final String RESEARCH_REQUIREMENT_BUILDING_LEVEL_PROP = "level";

    /**
     * The building level.
     */
    private final int buildingLevel;

    /**
     * The building desc.
     */
    private final String building;

    /**
     * Create a building research requirement.
     *
     * @param nbt the nbt containing the relevant data.
     */
    public BuildingResearchRequirement(final CompoundTag nbt)
    {
        this.building = nbt.getString(TAG_BUILDING_NAME);
        this.buildingLevel = nbt.getInt(TAG_BUILDING_LVL);
    }

    /**
     * Create a building research requirement.
     *
     * @param json the json containing the relevant data.
     */
    public BuildingResearchRequirement(final JsonObject json)
    {
        this.building = GsonHelper.getAsString(json, RESEARCH_REQUIREMENT_BUILDING_PROP);
        this.buildingLevel = GsonHelper.getAsInt(json, RESEARCH_REQUIREMENT_BUILDING_LEVEL_PROP);
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
    public ModResearchRequirements.ResearchRequirementEntry getRegistryEntry()
    {
        return ModResearchRequirements.buildingResearchRequirement.get();
    }

    @Override
    public MutableComponent getDesc()
    {
        return Component.translatable("com.minecolonies.coremod.research.requirement.building.level",
            Component.translatable("com.minecolonies.building." + building),
            this.buildingLevel);
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        return colony.hasBuilding(this.building, this.buildingLevel, false);
    }

    @Override
    public CompoundTag writeToNBT()
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putString(TAG_BUILDING_NAME, building);
        nbt.putInt(TAG_BUILDING_LVL, buildingLevel);
        return nbt;
    }
}
