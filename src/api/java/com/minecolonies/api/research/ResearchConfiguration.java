package com.minecolonies.api.research;

import net.minecraftforge.common.config.Config;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

@Config(modid = MOD_ID, name = "minecolonies_research")
public class ResearchConfiguration
{
    @Config.Comment("All configurations related to combat research")
    public static Combat combat = new Combat();

    @Config.Comment("All configurations related to civilian research")
    public static Civilian civilian = new Civilian();

    @Config.Comment("All configurations related to technology research")
    public static Technology technology = new Technology();

    /**
     * Class which contains all the Combat related research options.
     */
    public static class Combat
    {
        @Config.Comment("Test Research 1")
        public String[] testResearch1 = new String[]
                                      {
                                        "item:coal*64",
                                      };

        @Config.Comment("Test Research 1")
        public String[] testResearch2 = new String[]
                                          {
                                            "item:book*64",
                                          };

        @Config.Comment("Test Research 1")
        public String[] testResearch3 = new String[]
                                          {
                                            "item:diamond*64",
                                          };

        @Config.Comment("Test Research 1")
        public String[] testResearch4 = new String[]
                                          {
                                            "item:iron_bars*64",
                                          };
    }

    /**
     * Class which contains all the Civilian related research options.
     */
    public static class Civilian
    {

    }

    /**
     * Class which contains all the Technology related research options.
     */
    public static class Technology
    {

    }
}
