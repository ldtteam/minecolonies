package com.minecolonies.api.research.requirements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingRegistry;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.minecolonies.api.research.requirements.BuildingResearchRequirement.parseFallbackBuildingKey;

/**
 * Requires one out of a list of buildings to be present.
 */
public class BuildingAlternatesResearchRequirement implements IResearchRequirement
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
     * The property name for the alternate building.
     */
    private static final String RESEARCH_REQUIREMENT_ALTERNATE_BUILDINGS_PROP = "alternate-buildings";

    /**
     * The property name for a numeric level.
     */
    private static final String RESEARCH_REQUIREMENT_BUILDING_LEVEL_PROP = "level";

    /**
     * The list of buildings, by level.
     */
    private final Map<ResourceLocation, Integer> buildings;

    /**
     * Create an alternate building research requirement.
     *
     * @param nbt the nbt containing the relevant data.
     */
    public BuildingAlternatesResearchRequirement(final CompoundTag nbt)
    {
        buildings = new HashMap<>();
        final ListTag buildingsNBT = nbt.getList(TAG_BUILDINGS_LIST, Constants.TAG_COMPOUND);
        for (int i = 0; i < buildingsNBT.size(); i++)
        {
            CompoundTag indNBT = buildingsNBT.getCompound(i);
            buildings.put(parseFallbackBuildingKey(indNBT.getString(TAG_BUILDING_NAME)), indNBT.getInt(TAG_BUILDING_LVL));
        }
    }

    /**
     * Create an alternate building research requirement.
     *
     * @param json the json containing the relevant data.
     */
    public BuildingAlternatesResearchRequirement(final JsonObject json)
    {
        buildings = new HashMap<>();
        for (final JsonElement element : GsonHelper.getAsJsonArray(json, RESEARCH_REQUIREMENT_ALTERNATE_BUILDINGS_PROP))
        {
            final String arrBuilding = element.getAsString();
            final int arrLevel = GsonHelper.getAsInt(json, RESEARCH_REQUIREMENT_BUILDING_LEVEL_PROP);
            buildings.merge(parseFallbackBuildingKey(arrBuilding), arrLevel, Integer::sum);
        }
    }

    /**
     * Get the Map of required building types and their levels.  Only one must be met to unlock the research.
     *
     * @return the building description
     */
    public Map<ResourceLocation, Integer> getBuildings()
    {
        return buildings;
    }

    @Override
    public ModResearchRequirements.ResearchRequirementEntry getRegistryEntry()
    {
        return ModResearchRequirements.buildingAlternatesResearchRequirement.get();
    }

    @Override
    public MutableComponent getDesc()
    {
        final MutableComponent requirementList = Component.literal("");
        final Iterator<Map.Entry<ResourceLocation, Integer>> iterator = buildings.entrySet().iterator();
        while (iterator.hasNext())
        {
            final Map.Entry<ResourceLocation, Integer> kvp = iterator.next();
            final ResourceLocation building = kvp.getKey();
            final int buildingLevel = kvp.getValue();

            final BuildingEntry buildingEntry = IBuildingRegistry.getInstance().getValue(building);
            final MutableComponent buildingName = buildingEntry != null ? Component.translatable(buildingEntry.getTranslationKey()) : Component.empty();

            requirementList.append(Component.translatable("com.minecolonies.coremod.research.requirement.building.mandatory.level", buildingName, buildingLevel));

            if (iterator.hasNext())
            {
                requirementList.append(Component.translatable("com.minecolonies.coremod.research.requirement.building.or"));
            }
        }
        return requirementList;
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        for (final Map.Entry<ResourceLocation, Integer> requirement : buildings.entrySet())
        {
            if (colony.hasBuilding(requirement.getKey(), requirement.getValue(), false))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public CompoundTag writeToNBT()
    {
        final CompoundTag nbt = new CompoundTag();
        final ListTag buildingsNBT = new ListTag();
        for (Map.Entry<ResourceLocation, Integer> build : buildings.entrySet())
        {
            CompoundTag indNBT = new CompoundTag();
            indNBT.putString(TAG_BUILDING_NAME, build.getKey().toString());
            indNBT.putInt(TAG_BUILDING_LVL, build.getValue());
            buildingsNBT.add(indNBT);
        }
        nbt.put(TAG_BUILDINGS_LIST, buildingsNBT);
        return nbt;
    }
}
