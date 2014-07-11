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
    private static final String CATEGORY_GAMEPLAY = "Game Play";
    private static final String CATEGORY_NAMES    = "Names";

    public static void init(File file)
    {
        Configuration config = new Configuration(file);

        try
        {
            config.load();
            Configurations.workingRangeTownhall = config.get(CATEGORY_GAMEPLAY, "Working Range Townhall: ", DEFAULT_WORKINGRANGETOWNHALL).getInt(DEFAULT_WORKINGRANGETOWNHALL);
            Configurations.townhallPadding = config.get(CATEGORY_GAMEPLAY, "Empty space between townhall boundaries: ", DEFAULT_TOWNHALLPADDING).getInt(DEFAULT_TOWNHALLPADDING);
            Configurations.allowInfiniteSupplyChests = config.get(CATEGORY_GAMEPLAY, "Allow infinite placing of Supply Chests: ", DEFAULT_ALLOWINFINTESUPPLYCHESTS).getBoolean(DEFAULT_ALLOWINFINTESUPPLYCHESTS);
            Configurations.citizenRespawnInterval = getClampedInt(config, CATEGORY_GAMEPLAY, "Citizen Respawn Interval: ", DEFAULT_CITIZENRESPAWNINTERVAL, 10, 600, "Value in seconds");
            Configurations.builderInfiniteResources = config.get(CATEGORY_GAMEPLAY, "Does Builder have infinite resources: ", DEFAULT_BUILDERINFINITERESOURCES).getBoolean(DEFAULT_BUILDERINFINITERESOURCES);

            Configurations.maleFirstNames = config.get(CATEGORY_NAMES, "Male first names: ", DEFAULT_MALE_FIRST_NAMES).getStringList();
            Configurations.femaleFirstNames = config.get(CATEGORY_NAMES, "Female first names: ", DEFAULT_FEMALE_FIRST_NAMES).getStringList();
            Configurations.lastNames = config.get(CATEGORY_NAMES, "Last names: ", DEFAULT_LAST_NAMES).getStringList();
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
