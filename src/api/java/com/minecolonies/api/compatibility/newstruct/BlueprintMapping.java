package com.minecolonies.api.compatibility.newstruct;

import java.util.*;

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
        styleMapping.put("asian", "East Asian");
        styleMapping.put("wooden", "Default");

        //todo remaining styles

        final Map<String, List<String>> foldersToHuts = new HashMap<>();
        foldersToHuts.put("agriculture/horticulture", Arrays.asList("composter", "farmer", "florist", "plantation"));
        foldersToHuts.put("agriculture/husbandry", Arrays.asList("beekeeper", "chickenherder", "cowboy", "fisherman", "rabbithutch", "shepherd", "swineherder"));

        foldersToHuts.put("craftsmanship/carpentry", Arrays.asList("fletcher", "sawmill"));
        foldersToHuts.put("craftsmanship/luxury", Arrays.asList("alchemist", "baker", "concretemixer", "dyer", "glassblower"));
        foldersToHuts.put("craftsmanship/masonry", Arrays.asList("crusher", "sifter", "stonemason", "stonesmeltery"));
        foldersToHuts.put("craftsmanship/metallurgy", Arrays.asList("blacksmith", "mechanic", "smeltery"));
        foldersToHuts.put("craftsmanship/storage", Arrays.asList("deliveryman", "warehouse"));

        foldersToHuts.put("education", Arrays.asList("library", "school", "university"));
        foldersToHuts.put("fundamentals", Arrays.asList("builder", "citizen", "cook", "hospital", "lumberjack", "miner", "tavern", "townhall"));
        foldersToHuts.put("military", Arrays.asList("archery", "barracks", "barrackstower", "combatacademy", "guardtower"));
        foldersToHuts.put("mystic", Arrays.asList("enchanter", "netherworker"));

        runForStyle(foldersToHuts, "asian");
        runForStyle(foldersToHuts, "wooden");
        runForStyle(foldersToHuts, "stone");
        runForStyle(foldersToHuts, "spacewars");
        runForStyle(foldersToHuts, "sandstone");
        runForStyle(foldersToHuts, "nordic");
        runForStyle(foldersToHuts, "medievalspruce");
        runForStyle(foldersToHuts, "medievaloak");
        runForStyle(foldersToHuts, "medievaldarkoeak");
        runForStyle(foldersToHuts, "medievalbirch");
        runForStyle(foldersToHuts, "lostcity");
        runForStyle(foldersToHuts, "jungle");
        runForStyle(foldersToHuts, "incan");
        runForStyle(foldersToHuts, "fortress");
        runForStyle(foldersToHuts, "darkoak");
        runForStyle(foldersToHuts, "caledonia");
        runForStyle(foldersToHuts, "birch");
        runForStyle(foldersToHuts, "asian");
        runForStyle(foldersToHuts, "acacia");

        // This is for the default mapping of things.
        runForStyle(foldersToHuts, "");

        pathMapping.put("asianalternative:citizen", "fundamentals/citizenalt");
        pathMapping.put("asianalternative:guardtower", "military/guardtoweralt");

        pathMapping.put("woodalternative:citizen", "fundamentals/citizenalt");
        pathMapping.put("woodalternative:shepherd", "agriculture/husbandry/shepherdalt");
        pathMapping.put("woodalternative:deliveryman", "craftsmanship/storage/deliverymanalt");
        pathMapping.put("woodalternative:warehouse", "craftsmanship/storage/warehousealt");

        //todo remaining alt huts
    }

    private static void runForStyle(final Map<String, List<String>> foldersToHuts, final String style)
    {
        for (final Map.Entry<String, List<String>> entry : foldersToHuts.entrySet())
        {
            for (final String hut : entry.getValue())
            {
                pathMapping.put(style + ":" + hut, entry.getKey() + "/" +  hut);
            }
        }
    }
}
