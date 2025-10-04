package com.minecolonies.api.research;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry entries for research requirement types.
 */
public class ModResearchRequirements
{
    public static final ResourceLocation BUILDING_RESEARCH_REQ_ID = new ResourceLocation(Constants.MOD_ID, "building");
    public static final ResourceLocation BUILDING_ALTERNATES_RESEARCH_REQ_ID = new ResourceLocation(Constants.MOD_ID, "alternate-building");
    public static final ResourceLocation BUILDING_SINGLE_RESEARCH_REQ_ID     = new ResourceLocation(Constants.MOD_ID, "single-building");
    public static final ResourceLocation RESEARCH_RESEARCH_REQ_ID            = new ResourceLocation(Constants.MOD_ID, "research");

    public static RegistryObject<ResearchRequirementEntry> buildingResearchRequirement;
    public static RegistryObject<ResearchRequirementEntry> buildingAlternatesResearchRequirement;
    public static RegistryObject<ResearchRequirementEntry> buildingSingleResearchRequirement;
    public static RegistryObject<ResearchRequirementEntry> researchResearchRequirement;

    private ModResearchRequirements()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchRequirements but this is a Utility class.");
    }

    /**
     * Functional interface used in reading the requirements from nbt.
     */
    @FunctionalInterface
    public interface ReadFromNBTFunction
    {
        IResearchRequirement read(final CompoundTag compound);
    }

    /**
     * Functional interface used in reading the requirements from json.
     */
    @FunctionalInterface
    public interface ReadFromJsonFunction
    {
        IResearchRequirement read(final JsonObject json);
    }

    /**
     * Entry for the {@link IResearchRequirement} registry. Makes it possible to create a single registry for a {@link IResearchRequirement}.
     */
    public static class ResearchRequirementEntry
    {
        /**
         * The registry name for this entry.
         */
        private final ResourceLocation registryName;

        /**
         * Function to read this item from NBT.
         */
        private final ReadFromNBTFunction readFromNBT;

        /**
         * Function to read this item from json.
         */
        private final ReadFromJsonFunction readFromJson;

        /**
         * Default constructor.
         *
         * @param registryName the registry name for this entry.
         * @param readFromJson function to read this item from NBT.
         * @param readFromNBT  function to read this item from json.
         */
        public ResearchRequirementEntry(final ResourceLocation registryName, final ReadFromNBTFunction readFromNBT, final ReadFromJsonFunction readFromJson)
        {
            this.registryName = registryName;
            this.readFromNBT = readFromNBT;
            this.readFromJson = readFromJson;
        }

        /**
         * Get the registry name for this entry.
         */
        public ResourceLocation getRegistryName()
        {
            return registryName;
        }

        /**
         * Read a research requirement instance from NBT.
         */
        public IResearchRequirement readFromNBT(final CompoundTag nbt)
        {
            return readFromNBT.read(nbt);
        }

        /**
         * Read a research requirement instance from json.
         */
        public IResearchRequirement readFromJson(final JsonObject json) throws JsonParseException
        {
            return readFromJson.read(json);
        }
    }
}
