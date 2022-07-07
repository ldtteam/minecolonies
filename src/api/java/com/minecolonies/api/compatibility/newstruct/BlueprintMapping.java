package com.minecolonies.api.compatibility.newstruct;

import java.util.HashMap;
import java.util.Map;

/**
 * The class that is responsible for the mapping of the old structurename/style to new style/structurename.
 */
public class BlueprintMapping
{
    /**
     * Maps an old style name to a new style name.
     */
    public static Map<String, String> styleMapping = new HashMap<>();

    /**
     * Maps the old blueprint path to the new path. Query it with style:blueprintName and receive the pack specific path.
     */
    public static Map<String, String> pathMapping = new HashMap<>();

    static
    {
        styleMapping.put("sandstone", "Moroccan");
        styleMapping.put("asian", "Asian");
        styleMapping.put("wooden", "Default");

        //todo add remaining
        pathMapping.put("sandstone:citizen1", "fundamentals/citizen1");
    }
}
