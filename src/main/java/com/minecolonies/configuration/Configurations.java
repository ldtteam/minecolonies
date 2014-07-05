package com.minecolonies.configuration;

public class Configurations
{
    /*
    Defaults
     */
    public static final int     DEFAULT_WORKINGRANGETOWNHALL     = 100;
    public static final int     DEFAULT_TOWNHALLPADDING          = 20;
    public static final boolean DEFAULT_ALLOWINFINTESUPPLYCHESTS = false;
    public static final int     DEFAULT_CITIZENRESPAWNINTERVAL   = 30;
    public static final boolean DEFAULT_BUILDERINFINITERESOURCES = true;//TODO change to false when material handling is implemented

    public static final String[] DEFAULT_MALE_FIRST_NAMES = new String[]{
            "Jim", "John", "James", "Robert", "Thomas", "Michael", "William", "David", "Richard", "Charles", "Joseph", "Christopher", "Paul", "Mark", "George", "Steven", "Peter", "Henrik", "Rory"};

    public static final String[] DEFAULT_FEMALE_FIRST_NAMES = new String[]{
            "Mary", "Patricia", "Linda", "Barbara", "Elizabeth", "Jennifer", "Maria", "Susan", "Margaret", "Dorothy", "Lisa", "Nancy", "Karen", "Betty", "Helen", "Natasha"};

    public static final String[] DEFAULT_LAST_NAMES = new String[]{
            "Smith", "Johnson", "Jones", "Williams", "Brown", "Miller", "Wilson", "Taylor", "Jackson", "White", "Harris", "Robinson", "Clark", "Wallgreen", "Allen", "Mardle"};

    /*
    Holders
     */
    public static int     workingRangeTownhall;
    public static int     townhallPadding;
    public static boolean allowInfiniteSupplyChests;
    /**
     * The citizen respawn interval in seconds
     */
    public static int     citizenRespawnInterval;
    public static boolean builderInfiniteResources;

    public static String[] maleFirstNames;
    public static String[] femaleFirstNames;
    public static String[] lastNames;
}
