package com.minecolonies.coremod.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.research.registry.ResearchRequirementEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
     * The NBT tag for the list of alternate buildings.
     */
    private static final String TAG_BUILDINGS_LIST = "building-list";

    /**
     * The NBT tag for an individual building's name.
     */
    private static final String TAG_BUILDING_NAME = "building-name";

    /**
     * The NBT tag for an individual building's required level.
     */
    private static final String TAG_BUILDING_LVL = "building-lvl";

    /**
     * The list of buildings, by level.
     */
    final private Map<String, Integer> buildings = new HashMap<>();

    /**
     * Create a building-based research requirement, that requires one of multiple buildings be constructed.
     *
     * @param building the name of the building
     * @param level    the level requirement of the building
     */
    public AlternateBuildingResearchRequirement add(String building, int level)
    {
        if (buildings.containsKey(building))
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
     * Creates and returns an Alternate Building Requirement, reassembled from a compoundNBT
     * @param nbt the NBT containing the Building Names and Levels data
     */
    public AlternateBuildingResearchRequirement(CompoundNBT nbt)
    {
        ListNBT buildingsNBT = nbt.getList(TAG_BUILDINGS_LIST, Constants.TAG_COMPOUND);
        for(int i = 0; i < buildingsNBT.size(); i++)
        {
            CompoundNBT indNBT = buildingsNBT.getCompound(i);
            buildings.put(indNBT.getString(TAG_BUILDING_NAME), indNBT.getInt(TAG_BUILDING_LVL));
        }
    }

    /**
     * Get the Map of required building types and their levels.  Only one must be met to unlock the research.
     * @return the building description
     */
    public Map<String, Integer> getBuildings()
    {
        return buildings;
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        for (Map.Entry<String, Integer> requirement : buildings.entrySet())
        {
            if(colony.hasBuilding(requirement.getKey(), requirement.getValue(), false))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public TranslationTextComponent getDesc()
    {
        final TranslationTextComponent requirementList = new TranslationTextComponent("");
        final Iterator<Map.Entry<String, Integer>> iterator = buildings.entrySet().iterator();
        while (iterator.hasNext())
        {
            final Map.Entry<String, Integer> kvp = iterator.next();
            requirementList.append(new TranslationTextComponent("com.minecolonies.coremod.research.requirement.building.level",
              new TranslationTextComponent("block.minecolonies.blockhut" + kvp.getKey()),
              kvp.getValue()));
            if (iterator.hasNext())
            {
                requirementList.append(new TranslationTextComponent("com.minecolonies.coremod.research.requirement.building.or"));
            }
        }
        return requirementList;
    }

    @Override
    public ResearchRequirementEntry getRegistryEntry() { return ModResearchRequirements.alternateBuildingResearchRequirement;}

    @Override
    public CompoundNBT writeToNBT()
    {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT buildingsNBT = new ListNBT();
        for(Map.Entry<String, Integer> build : buildings.entrySet())
        {
            CompoundNBT indNBT = new CompoundNBT();
            indNBT.putString(TAG_BUILDING_NAME, build.getKey());
            indNBT.putInt(TAG_BUILDING_LVL, build.getValue());
            buildingsNBT.add(indNBT);
        }
        nbt.put(TAG_BUILDINGS_LIST, buildingsNBT);
        return nbt;
    }
}
