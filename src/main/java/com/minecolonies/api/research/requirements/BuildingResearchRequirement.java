package com.minecolonies.api.research.requirements;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingRegistry;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.research.ModResearchRequirements;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.Objects;

import static com.minecolonies.api.research.ModResearchRequirements.BUILDING_SINGLE_RESEARCH_REQ_ID;
import static com.minecolonies.api.research.util.ResearchConstants.TAG_REQ_TYPE;
import static com.minecolonies.core.datalistener.ResearchListener.RESEARCH_REQUIREMENT_TYPE_PROP;

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
     * The building desc.
     */
    private final Identifier building;

    /**
     * The building level.
     */
    private final int buildingLevel;

    /**
     * Whether to look between several or a single building.
     */
    private final boolean singleBuilding;

    /**
     * Create a building research requirement.
     *
     * @param nbt the nbt containing the relevant data.
     */
    public BuildingResearchRequirement(final CompoundTag nbt)
    {
        building = parseFallbackBuildingKey(nbt.getStringOr(TAG_BUILDING_NAME, ""));
        buildingLevel = nbt.getIntOr(TAG_BUILDING_LVL, 0);
        singleBuilding = nbt.getStringOr(TAG_REQ_TYPE, "").equals(BUILDING_SINGLE_RESEARCH_REQ_ID.toString());
    }

    /**
     * Attempts to parse a resource location from a given key. If the key is not already a valid resource location,
     * it is assumed to be a namespaced key within the MineColonies mod, and is converted to a valid resource location accordingly.
     *
     * @param key the key to parse.
     * @return the parsed resource location.
     */
    // TODO: 1.22: Remove
    public static Identifier parseFallbackBuildingKey(final String key)
    {
        final Identifier buildingIdentifier = Identifier.tryParse(key);
        return Objects.requireNonNullElseGet(buildingIdentifier, () -> Identifier.fromNamespaceAndPath(Constants.MOD_ID, key));
    }

    /**
     * Create a building research requirement.
     *
     * @param json the json containing the relevant data.
     */
    public BuildingResearchRequirement(final JsonObject json)
    {
        // TODO: 1.22: Change to GsonHelper.getAsIdentifier(json, RESEARCH_REQUIREMENT_BUILDING_PROP);
        building = parseFallbackBuildingKey(GsonHelper.getAsString(json, RESEARCH_REQUIREMENT_BUILDING_PROP));
        buildingLevel = GsonHelper.getAsInt(json, RESEARCH_REQUIREMENT_BUILDING_LEVEL_PROP);
        singleBuilding = GsonHelper.getAsIdentifier(json, RESEARCH_REQUIREMENT_TYPE_PROP).equals(BUILDING_SINGLE_RESEARCH_REQ_ID);
    }

    /**
     * @return the building registry resource location
     */
    public Identifier getBuilding()
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
        final BuildingEntry buildingEntry = IBuildingRegistry.getInstance().getValue(building);
        final MutableComponent buildingName = buildingEntry != null ? Component.translatable(buildingEntry.getTranslationKey()) : Component.empty();

        if (singleBuilding)
        {
            return Component.translatable("com.minecolonies.coremod.research.requirement.building.single.level", buildingName, buildingLevel);
        }
        else
        {
            return Component.translatable("com.minecolonies.coremod.research.requirement.building.level", buildingName, buildingLevel);
        }
    }

    @Override
    public boolean isFulfilled(final IColony colony)
    {
        return colony.getCommonBuildingManager().hasBuilding(building, buildingLevel, singleBuilding);
    }

    @Override
    public CompoundTag writeToNBT()
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putString(TAG_BUILDING_NAME, building.toString());
        nbt.putInt(TAG_BUILDING_LVL, buildingLevel);
        return nbt;
    }
}
