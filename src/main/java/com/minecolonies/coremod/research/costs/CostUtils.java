package com.minecolonies.coremod.research.costs;

import com.google.gson.JsonObject;

import static com.minecolonies.coremod.research.GlobalResearch.RESEARCH_QUANTITY_PROP;

/**
 * Util methods for cost entries.
 */
public class CostUtils
{
    private CostUtils()
    {
    }

    public static int getCount(final JsonObject jsonObject)
    {
        int count = 1;
        if (jsonObject.has(RESEARCH_QUANTITY_PROP) &&
              jsonObject.get(RESEARCH_QUANTITY_PROP).isJsonPrimitive() &&
              jsonObject.get(RESEARCH_QUANTITY_PROP).getAsJsonPrimitive().isNumber())
        {
            count = Math.max(jsonObject.get(RESEARCH_QUANTITY_PROP).getAsInt(), 1);
        }
        return count;
    }
}
