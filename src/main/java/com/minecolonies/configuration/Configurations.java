package com.minecolonies.configuration;

public class Configurations
{
    public static int       workingRangeTownhall        = 100;
    public static int       townhallPadding             = 20;
    public static boolean   allowInfiniteSupplyChests   = false;
    public static int       citizenRespawnInterval      = 30;

    public static final int CITIZEN_RESPAWN_INTERVAL_MIN = 10;
    public static final int CITIZEN_RESPAWN_INTERVAL_MAX = 600;

    public static boolean   builderInfiniteResources        = true;//TODO change to false when material handling is implemented
    public static boolean   deliverymanInfiniteResources    = true;//TODO change to false when material handling is implemented
    public static int       maxCitizens                     = 4;//TODO remove config value and set maxCitizens based on the colony buildings/levels
    public static boolean   alwaysRenderNameTag             = true;
    public static int       maxBlocksCheckedByBuilder       = 1000;//TODO change count to agreed upon value, possibly remove if we think this shouldn't be a problem

    public static boolean   enableInDevelopmentFeatures = false;

    public static String[] maleFirstNames = new String[] {
            "Jim", "John", "James", "Robert", "Thomas", "Michael", "William", "David", "Richard", "Charles", "Joseph", "Christopher", "Paul", "Mark", "George", "Steven", "Peter", "Henrik", "Rory" };

    public static String[] femaleFirstNames = new String[] {
            "Mary", "Patricia", "Linda", "Barbara", "Elizabeth", "Jennifer", "Maria", "Susan", "Margaret", "Dorothy", "Lisa", "Nancy", "Karen", "Betty", "Helen", "Natasha" };

    public static String[] lastNames = new String[] {
            "Smith", "Johnson", "Jones", "Williams", "Brown", "Miller", "Wilson", "Taylor", "Jackson", "White", "Harris", "Robinson", "Clark", "Wallgreen", "Allen", "Mardle" };
}
