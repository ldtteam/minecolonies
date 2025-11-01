package com.minecolonies.api.research;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry entries for item cost types.
 */
public class ModResearchCosts
{
    public static final ResourceLocation SIMPLE_ITEM_COST_ID = new ResourceLocation(Constants.MOD_ID, "item_simple");
    public static final ResourceLocation LIST_ITEM_COST_ID   = new ResourceLocation(Constants.MOD_ID, "item_list");
    public static final ResourceLocation TAG_ITEM_COST_ID    = new ResourceLocation(Constants.MOD_ID, "item_tag");

    public static RegistryObject<ResearchCostEntry> simpleItemCost;
    public static RegistryObject<ResearchCostEntry> listItemCost;
    public static RegistryObject<ResearchCostEntry> tagItemCost;

    private ModResearchCosts()
    {
        throw new IllegalStateException("Tried to initialize: ModResearchCosts but this is a Utility class.");
    }

    /**
     * Functional interface used in reading the costs from nbt.
     */
    @FunctionalInterface
    public interface ReadFromNBTFunction
    {
        IResearchCost read(final CompoundTag compound);
    }

    /**
     * Functional interface used in reading the costs from json.
     */
    @FunctionalInterface
    public interface ReadFromJsonFunction
    {
        IResearchCost read(final JsonObject json);
    }

    /**
     * Quest reward entry type.
     */
    public static class ResearchCostEntry
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
        public ResearchCostEntry(final ResourceLocation registryName, final ReadFromNBTFunction readFromNBT, final ReadFromJsonFunction readFromJson)
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
         * Read a research cost instance from NBT.
         */
        public IResearchCost readFromNBT(final CompoundTag nbt)
        {
            return readFromNBT.read(nbt);
        }

        /**
         * Read a research cost instance from json.
         */
        public IResearchCost readFromJson(final JsonObject json) throws JsonParseException
        {
            return readFromJson.read(json);
        }
    }
}
