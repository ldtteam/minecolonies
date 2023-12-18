package com.minecolonies.api.research;

import com.google.gson.JsonObject;
import com.minecolonies.api.research.costs.IResearchCost;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

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

    /**
     * Quest reward entry type.
     */
    public static class ResearchCostEntry
    {
        /**
         * The producer for the cost instance.
         */
        private final Supplier<IResearchCost> producer;

        /**
         * Default constructor.
         */
        public ResearchCostEntry(final Supplier<IResearchCost> productionFunction)
        {
            this.producer = productionFunction;
        }

        /**
         * Create an empty cost instance.
         *
         * @return the cost instance.
         */
        public IResearchCost createInstance()
        {
            return producer.get();
        }

        /**
         * Checks if this json object has the correct fields for this cost instance.
         *
         * @param jsonObject the input json object.
         * @return true if the json object is in the right format.
         */
        public boolean hasCorrectJsonFields(final JsonObject jsonObject)
        {
            final IResearchCost instance = createInstance();
            return instance.hasCorrectJsonFields(jsonObject);
        }

        /**
         * Parses the json object on this cost instance.
         *
         * @param jsonObject the input json object.
         */
        public IResearchCost parseFromJson(final JsonObject jsonObject)
        {
            final IResearchCost instance = createInstance();
            instance.parseFromJson(jsonObject);
            return instance;
        }
    }
}
