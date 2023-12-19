package com.minecolonies.api.research;

import com.google.gson.JsonObject;
import com.minecolonies.api.research.costs.IResearchCost;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

/**
 * Registry entries for item cost types.
 */
public class ModResearchCostTypes
{
    public static final ResourceLocation SIMPLE_ITEM_COST_ID = new ResourceLocation(Constants.MOD_ID, "item_simple");
    public static final ResourceLocation LIST_ITEM_COST_ID   = new ResourceLocation(Constants.MOD_ID, "item_list");
    public static final ResourceLocation TAG_ITEM_COST_ID    = new ResourceLocation(Constants.MOD_ID, "item_tag");

    public static RegistryObject<ResearchCostType> simpleItemCost;

    public static RegistryObject<ResearchCostType> listItemCost;

    public static RegistryObject<ResearchCostType> tagItemCost;

    /**
     * Quest reward entry type.
     */
    public static class ResearchCostType
    {
        /**
         * The ID of the cost type.
         */
        private final ResourceLocation id;

        /**
         * The producer for the cost instance.
         */
        private final Function<ResearchCostType, IResearchCost> producer;

        /**
         * Default constructor.
         */
        public ResearchCostType(final ResourceLocation id, final Function<ResearchCostType, IResearchCost> productionFunction)
        {
            this.id = id;
            this.producer = productionFunction;
        }

        /**
         * The ID of this cost type.
         *
         * @return the id.
         */
        public ResourceLocation getId()
        {
            return id;
        }

        /**
         * Create an empty cost instance.
         *
         * @return the cost instance.
         */
        public IResearchCost createInstance()
        {
            return producer.apply(this);
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
