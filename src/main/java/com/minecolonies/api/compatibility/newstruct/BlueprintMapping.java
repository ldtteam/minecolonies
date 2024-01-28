package com.minecolonies.api.compatibility.newstruct;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class that is responsible for the mapping of the old structurename/style to new style/structurename.
 */
public class BlueprintMapping
{
    /**
     * Maps an old style name to a new style name.
     */
    private static Map<String, String> styleMapping = new HashMap<>();

    /**
     * Maps the old blueprint path to the new path. Query it with style:blueprintName and receive the pack specific path.
     */
    private static Map<String, String> pathMapping = new HashMap<>();

    static
    {
        styleMapping.put("medievaloak", "Medieval Oak");
        styleMapping.put("medievalspruce", "Medieval Spruce");
        styleMapping.put("medievaldarkoak", "Medieval Dark Oak");
        styleMapping.put("medievalbirch", "Medieval Birch");
        styleMapping.put("caledonia", "Caledonia");
        styleMapping.put("darkoak", "Dark Oak Treehouse");
        styleMapping.put("fortress", "Fortress");
        styleMapping.put("birch", "Urban Birch");
        styleMapping.put("jungle", "Jungle Treehouse");
        styleMapping.put("lostcity", "Lost Mesa City");
        styleMapping.put("incan", "Incan");
        styleMapping.put("nordic", "Nordic Spruce");
        styleMapping.put("sandstone", "Desert Oasis");
        styleMapping.put("spacewars", "Spacewars");
        styleMapping.put("warped", "Warped Netherlands");
        styleMapping.put("truedwarven", "Stalactite Caves");
        styleMapping.put("stone", "Minecolonies Original");
        styleMapping.put("acacia", "Urban Savanna");
        styleMapping.put("wooden", "Minecolonies Original");
        styleMapping.put("asian", "Pagoda");

        styleMapping.put("asianalternative", "Pagoda");
        styleMapping.put("fortressalternative", "Fortress");
        styleMapping.put("junglealternative", "Jungle Treehouse");
        styleMapping.put("woodalternative", "Minecolonies Original");
        styleMapping.put("stonealternative", "Minecolonies Original");
        styleMapping.put("medievaloakalternative", "Medieval Oak");
        styleMapping.put("medievalsprucealternative", "Medieval Spruce");
        styleMapping.put("medievaldarkoakalternative", "Medieval Dark Oak");
        styleMapping.put("medievalbirchalternative", "Medieval Birch");
        styleMapping.put("caledoniaalternative", "Caledonia");

        final Map<String, List<String>> foldersToHuts = new HashMap<>();
        foldersToHuts.put("agriculture/horticulture", Arrays.asList("composter", "farmer", "florist", "plantation"));
        foldersToHuts.put("agriculture/husbandry", Arrays.asList("beekeeper", "chickenherder", "cowboy", "fisherman", "rabbithutch", "shepherd", "swineherder"));

        foldersToHuts.put("craftsmanship/carpentry", Arrays.asList("fletcher", "sawmill"));
        foldersToHuts.put("craftsmanship/luxury", Arrays.asList("alchemist", "baker", "concretemixer", "dyer", "glassblower"));
        foldersToHuts.put("craftsmanship/masonry", Arrays.asList("crusher", "sifter", "stonemason", "stonesmeltery"));
        foldersToHuts.put("craftsmanship/metallurgy", Arrays.asList("blacksmith", "mechanic", "smeltery"));
        foldersToHuts.put("craftsmanship/storage", Arrays.asList("deliveryman", "warehouse"));

        foldersToHuts.put("education", Arrays.asList("library", "school", "university"));
        foldersToHuts.put("fundamentals", Arrays.asList("builder", "residence", "cook", "hospital", "lumberjack", "miner", "tavern", "townhall"));
        foldersToHuts.put("military", Arrays.asList("archery", "barracks", "barrackstower", "combatacademy", "guardtower"));
        foldersToHuts.put("mystic", Arrays.asList("enchanter", "netherworker", "mysticalsite", "graveyard"));

        foldersToHuts.put("infrastructure/mineshafts", Arrays.asList("simplequarry", "mediumquarry"));

        runForStyle(foldersToHuts, "medievaloak");
        runForStyle(foldersToHuts, "medievalspruce");
        runForStyle(foldersToHuts, "medievaldarkoak");
        runForStyle(foldersToHuts, "medievalbirch");

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
        runForStyle(foldersToHuts, "fortress");

        // This is for the default mapping of things.
        runForStyle(foldersToHuts, "");

        runForAlt(foldersToHuts, "medievalbirchalternative");
        runForAlt(foldersToHuts, "medievaloakalternative");
        runForAlt(foldersToHuts, "medievaldarkoakalternative");
        runForAlt(foldersToHuts, "medievalsprucealternative");
        runForAlt(foldersToHuts, "fortressalternative");
        runForAlt(foldersToHuts, "asianalternative");

        pathMapping.put(":home", "fundamentals/residence");
        pathMapping.put(":citizen", "fundamentals/residence");

        pathMapping.put("woodalternative:citizen", "fundamentals/altresidence");
        pathMapping.put("woodalternative:shepherd", "agriculture/husbandry/shepherdalt");
        pathMapping.put("woodalternative:deliveryman", "craftsmanship/storage/deliverymanalt");
        pathMapping.put("woodalternative:warehouse", "craftsmanship/storage/warehousealt");

        pathMapping.put("junglealternative:farmer", "agriculture/horticulture/farmeralt");

        pathMapping.put("caledoniaalternative:farmer", "agriculture/horticulture/farmeralt");
        pathMapping.put("caledoniaalternative:guardtower", "military/guardtoweralt");
    }

    /**
     * Get the style mapping for a style.
     * @param key the old style name.
     * @return the new stylename if mapping exists, else the same key again.
     */
    public static String getStyleMapping(final String key)
    {
        return styleMapping.getOrDefault(key, key);
    }

    /**
     * Get the path mapping for a hut for a given style. Or the default mapping if no special handling is setup.
     * @param style the style to query it for.
     * @param hut the hut to query it for.
     * @return the mapping.
     */
    public static String getPathMapping(final String style, final String hut)
    {
        return pathMapping.getOrDefault(style + ":" + hut, pathMapping.get(":" + hut));
    }

    /**
     * Generate backwards compat for alternative styles with many buildings.
     * @param foldersToHuts the folder mapping.
     * @param style the pack name.
     */
    private static void runForAlt(final Map<String, List<String>> foldersToHuts, final String style)
    {
        for (final Map.Entry<String, List<String>> entry : foldersToHuts.entrySet())
        {
            for (final String hut : entry.getValue())
            {
                pathMapping.put(style + ":" + hut, entry.getKey() + "/" +  "alt" + hut);
            }
        }
    }

    /**
     * Generate backwards compat for all the default huts.
     * @param foldersToHuts the folder mapping.
     * @param style the pack name.
     */
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
