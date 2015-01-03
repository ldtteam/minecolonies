package com.minecolonies.configuration;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

import static com.minecolonies.configuration.Configurations.*;

/**
 * Configuration Handler.
 * Reads the config file, and stores them in Configurations.java
 * The file is FMLPreInitializationEvent.getSuggestedConfigurationFile
 */
public class ConfigurationHandler
{
    private static final String CATEGORY_GAMEPLAY = "Gameplay";
    private static final String CATEGORY_NAMES    = "Names";

    public static void init(File file)
    {
        Configuration config = new Configuration(file);

        try
        {
            config.load();
            workingRangeTownhall = config.get(CATEGORY_GAMEPLAY, "workingRangeTownhall", workingRangeTownhall, "Townhall Working Range").getInt();
            townhallPadding = config.get(CATEGORY_GAMEPLAY, "townhallPadding", townhallPadding, "Empty space between townhall boundaries").getInt();
            allowInfiniteSupplyChests = config.get(CATEGORY_GAMEPLAY, "allowInfiniteSupplyChests", allowInfiniteSupplyChests, "Allow infinite placing of Supply Chests?").getBoolean();
            citizenRespawnInterval = getClampedInt(config, CATEGORY_GAMEPLAY, "citizenRespawnInterval", citizenRespawnInterval, 10, 600, "Citizen respawn interval in seconds");
            builderInfiniteResources = config.get(CATEGORY_GAMEPLAY, "builderInfiniteResources", builderInfiniteResources, "Does Builder have infinite resources?").getBoolean();
            deliverymanInfiniteResources = config.get(CATEGORY_GAMEPLAY, "deliverymanInfiniteResources", deliverymanInfiniteResources, "Does Deliveryman have infinite resources?").getBoolean();
            maxBuildingLevel = config.get(CATEGORY_GAMEPLAY, "maxBuildingLevel", maxBuildingLevel, "Maximum Building Level").getInt();
            maxCitizens = config.get(CATEGORY_GAMEPLAY, "maxCitizens", maxCitizens, "Maximum number of citizens").getInt();
            alwaysRenderNameTag = config.get(CATEGORY_GAMEPLAY, "alwaysRenderNameTag", alwaysRenderNameTag, "Always render Citizen's name tag?").getBoolean();
            maxBlocksCheckedByBuilder = config.get(CATEGORY_GAMEPLAY, "maxBlocksCheckedByBuilder", maxBlocksCheckedByBuilder, "Limits the number of checked blocks per builder update").getInt();

            maleFirstNames = config.get(CATEGORY_NAMES, "maleFirstNames", maleFirstNames).getStringList();
            femaleFirstNames = config.get(CATEGORY_NAMES, "femaleFirstNames", femaleFirstNames).getStringList();
            lastNames = config.get(CATEGORY_NAMES, "lastNames", lastNames).getStringList();
        }
        finally
        {
            config.save();
        }
    }

    private static final String FORMAT_RANGE = "%s (range: %s ~ %s, default: %s)";

    private static int getClampedInt(Configuration config, String category, String key, int defaultValue, int min, int max, String comment)
    {
        Property property = config.get(category, key, defaultValue);
        property.comment = String.format(FORMAT_RANGE, comment, min, max, defaultValue);
        int value = property.getInt(defaultValue);
        property.set(Math.max(min, Math.min(max, value)));
        return value;
    }
}
