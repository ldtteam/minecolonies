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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
    private final Set<ResourceLocation> buildings;

    /**
     * The building level.
     */
    private final int buildingLevel;

    /**
     * Create an alternate building research requirement.
     *
     * @param nbt the nbt containing the relevant data.
     */
    public BuildingAlternatesResearchRequirement(final CompoundTag nbt)
    {
        buildings = new HashSet<>();
        buildingLevel = nbt.getInt(TAG_BUILDING_LVL);
        final ListTag buildingsNBT = nbt.getList(TAG_BUILDINGS_LIST, Constants.TAG_COMPOUND);
        for (int i = 0; i < buildingsNBT.size(); i++)
        {
            final CompoundTag buildingNBT = buildingsNBT.getCompound(i);
            buildings.add(parseFallbackBuildingKey(buildingNBT.getString(TAG_BUILDING_NAME)));
        }
    }

    /**
     * Create an alternate building research requirement.
     *
     * @param json the json containing the relevant data.
     */
    public BuildingAlternatesResearchRequirement(final JsonObject json)
    {
        buildings = new HashSet<>();
        buildingLevel = GsonHelper.getAsInt(json, RESEARCH_REQUIREMENT_BUILDING_LEVEL_PROP);
        for (final JsonElement element : GsonHelper.getAsJsonArray(json, RESEARCH_REQUIREMENT_ALTERNATE_BUILDINGS_PROP))
        {
            final String arrBuilding = element.getAsString();
            buildings.add(parseFallbackBuildingKey(arrBuilding));
        }
    }

    /**
     * Get the set of required buildings. Only one must be met to unlock the research.
     *
     * @return the building description
     */
    public Set<ResourceLocation> getBuildings()
    {
        return buildings;
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
        return ModResearchRequirements.buildingAlternatesResearchRequirement.get();
    }

    @Override
    public MutableComponent getDesc()
    {
        final MutableComponent requirementList = Component.literal("");
        final Iterator<ResourceLocation> iterator = buildings.iterator();
        while (iterator.hasNext())
        {
            final ResourceLocation building = iterator.next();

            final BuildingEntry buildingEntry = IBuildingRegistry.getInstance().getValue(building);
            final MutableComponent buildingName = buildingEntry != null ? Component.translatable(buildingEntry.getTranslationKey()) : Component.empty();

            requirementList.append(Component.translatable("com.minecolonies.coremod.research.requirement.building.level", buildingName, buildingLevel));

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
        for (final ResourceLocation requirement : buildings)
        {
            if (colony.hasBuilding(requirement, buildingLevel, false))
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
        nbt.putInt(TAG_BUILDING_LVL, buildingLevel);
        final ListTag buildingsNBT = new ListTag();
        for (final ResourceLocation building : buildings)
        {
            CompoundTag indNBT = new CompoundTag();
            indNBT.putString(TAG_BUILDING_NAME, building.toString());
            buildingsNBT.add(indNBT);
        }
        nbt.put(TAG_BUILDINGS_LIST, buildingsNBT);
        return nbt;
    }
}
