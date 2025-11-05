package com.minecolonies.core.research;

import com.google.gson.JsonObject;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingRegistry;
import com.minecolonies.api.research.IResearchEffect;
import com.minecolonies.api.research.ModResearchEffects;
import com.minecolonies.core.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * An instance of a Research Effect specific for unlocking a building.
 */
public class BuildingResearchEffect implements IResearchEffect
{
    /**
     * The json property marking the building in question.
     */
    public static final String BUILDING_PROP = "building";

    /**
     * The NBT tag for an individual effect's identifier, as a ResourceLocation.
     */
    private static final String TAG_ID = "id";

    /**
     * The NBT tag for an individual effect's description, as a human-readable string or TranslationText key.
     */
    private static final String TAG_DESC = "desc";

    /**
     * The NBT tag for an individual effect's strength, in magnitude.
     */
    private static final String TAG_BUILDING = "building";

    /**
     * The NBT tag for an individual effect's strength, in magnitude.
     */
    private static final String TAG_LEVEL = "level";

    /**
     * The unique effect Id.
     */
    private final ResourceLocation id;

    /**
     * The optional text description of the effect. If empty, a translation key will be derived from id.
     */
    private final Component name;

    /**
     * The ID of the building.
     */
    private final ResourceLocation buildingId;

    /**
     * The level of the building to unlock.
     */
    private final int level;

    /**
     * The constructor to build a new building research effect from an NBT.
     *
     * @param nbt the nbt containing the traits for the global research.
     */
    public BuildingResearchEffect(final CompoundTag nbt)
    {
        this.id = new ResourceLocation(nbt.getString(TAG_ID));
        this.name = Component.Serializer.fromJson(nbt.getString(TAG_DESC));
        this.buildingId = new ResourceLocation(nbt.getString(TAG_BUILDING));
        this.level = nbt.getInt(TAG_LEVEL);
    }

    /**
     * The constructor to build a new building research effect from json.
     *
     * @param id          the id to unlock.
     * @param effectLevel the level of the effect.
     * @param json        the json data.
     */
    public BuildingResearchEffect(final ResourceLocation id, final int effectLevel, final JsonObject json)
    {
        final ResourceLocation buildingId = GsonHelper.getAsResourceLocation(json, BUILDING_PROP, new ResourceLocation(""));
        final BuildingEntry buildingType = IBuildingRegistry.getInstance().getValue(buildingId);
        if (buildingType == null)
        {
            throw new IllegalArgumentException("Invalid building id " + buildingId);
        }

        this.id = id;
        this.name = Component.translatable("com.minecolonies.coremod.research.requirement.building.unlock", Component.translatable(buildingType.getTranslationKey()));
        this.buildingId = buildingId;
        this.level = effectLevel;
    }

    @Override
    public ModResearchEffects.ResearchEffectEntry getRegistryEntry()
    {
        return ModResearchEffects.buildingResearchEffect.get();
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    @NotNull
    public Component getName()
    {
        return name;
    }

    @Override
    @NotNull
    public Component getSubtitle()
    {
        return Component.empty();
    }

    @Override
    public double getEffect()
    {
        return this.level;
    }

    @Override
    public boolean overrides(@NotNull final IResearchEffect other)
    {
        return other instanceof BuildingResearchEffect buildingResearchEffect && Math.abs(level) > Math.abs(buildingResearchEffect.level);
    }

    @Override
    public CompoundTag writeToNBT()
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.putString(TAG_ID, id.toString());
        nbt.putString(TAG_DESC, Component.Serializer.toJson(name));
        nbt.putString(TAG_BUILDING, buildingId.toString());
        nbt.putInt(TAG_LEVEL, level);
        return nbt;
    }
}
