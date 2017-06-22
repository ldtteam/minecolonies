package com.minecolonies.api.configuration;

import com.minecolonies.api.util.constant.Constants;
import net.minecraftforge.common.config.Config;

@Config(modid = Constants.MOD_ID)
public class Configurations
{
    public static final int     CITIZEN_RESPAWN_INTERVAL_MIN = 10;
    public static final int     CITIZEN_RESPAWN_INTERVAL_MAX = 600;

    @Config.Comment("Should builder place construction tape?")
    public static       boolean builderPlaceConstructionTape = true;

    @Config.Comment("Colony size (radius)")
    public static       int     workingRangeTownHall         = 100;

    @Config.Comment("Padding between colonies")
    public static       int     townHallPadding              = 20;

    @Config.Comment("Should supply chests be craftable on this server?")
    public static       boolean supplyChests                 = true;

    @Config.Comment("Should players be able to place an infinite amount of supplychests?")
    public static       boolean allowInfiniteSupplyChests    = false;

    @Config.Comment("Average citizen respawn interval (in ticks)")
    public static       int     citizenRespawnInterval       = 240;

    @Config.Comment("Should builder and miner build without resources? (this also turns off what they produce)")
    public static       boolean builderInfiniteResources     = false;

    @Config.Comment("Should there be at max 1 warehouse per colony?")
    public static       boolean limitToOneWareHousePerColony = true;

    @Config.Comment("Delay after each block placement (Increasing it, increases the delay)")
    public static       int     builderBuildBlockDelay       = 15;

    @Config.Comment("Delay modifier to mine a block (Decreasing it, decreases the delay)")
    public static       int     blockMiningDelayModifier     = 500;

    @Config.Comment("Should worker work during the rain?")
    public static       boolean workersAlwaysWorkInRain      = false;

    @Config.Comment("Should the colony protection be enabled?")
    public static boolean enableColonyProtection      = true;

    @Config.Comment("Independend from the colony protection, should explosions be turned off?")
    public static boolean turnOffExplosionsInColonies = true;

    /* schematics usage */
    @Config.Comment("Should the default schematics be ignored (from the jar)?")
    public static boolean ignoreSchematicsFromJar = false;

    @Config.Comment("Should player made schematics be allowed")
    public static boolean allowPlayerSchematics   = false;

    @Config.Comment("Max amount of schematics to be cached on the server")
    public static int     maxCachedSchematics     = 100;

    /* Command configs */

    @Config.Comment("Time until a next teleport can be executed (in seconds)")
    public static int     teleportBuffer                    = 120;

    @Config.Comment("Which level counts as op level on the server")
    public static int     opLevelForServer                  = 3;

    @Config.Comment("Should the player be allowed to use the '/mc rtp' command?")
    public static boolean canPlayerUseRTPCommand            = true;

    @Config.Comment("Should the player be allowed to use the '/mc colony teleport' command?")
    public static boolean canPlayerUseColonyTPCommand       = false;

    @Config.Comment("Should the player be allowed to use the '/mc home' command?")
    public static boolean canPlayerUseHomeTPCommand         = true;

    @Config.Comment("Should the player be allowed to use the '/mc citizens info' command?")
    public static boolean canPlayerUseCitizenInfoCommand    = true;

    @Config.Comment("Should the player be allowed to use the '/mc citizens list' command?")
    public static boolean canPlayerUseListCitizensCommand   = true;

    @Config.Comment("Should the player be allowed to use the '/mc citizens respawn' command?")
    public static boolean canPlayerRespawnCitizensCommand   = true;

    @Config.Comment("Should the player be allowed to use the '/mc colony info' command?")
    public static boolean canPlayerUseShowColonyInfoCommand = true;

    @Config.Comment("Should the player be allowed to use the '/mc citizens kill' command?")
    public static boolean canPlayerUseKillCitizensCommand   = true;

    @Config.Comment("Should the player be allowed to use the '/mc colony addOfficer' command?")
    public static boolean canPlayerUseAddOfficerCommand     = true;

    @Config.Comment("Should the player be allowed to use the '/mc colony delete' command?")
    public static boolean canPlayerUseDeleteColonyCommand   = true;

    @Config.Comment("Should the player be allowed to use the '/mc colony refresh' command?")
    public static boolean canPlayerUseRefreshColonyCommand  = false;

    @Config.Comment("Should the player be allowed to use the '/mc backup' command?")
    public static boolean canPlayerUseBackupCommand         = false;

    /* Colony TP configs */
    @Config.Comment("Amount of attemps to find a save rtp")
    public static int numberOfAttemptsForSafeTP = 4;

    @Config.Comment("Max distance from world spawn")
    public static int maxDistanceFromWorldSpawn = 8000;

    @Config.Comment("Min distance from world spawn")
    public static int minDistanceFromWorldSpawn = 512;

    @Config.Comment("Should the dman create resources out of hot air (Not implemented)")
    public static boolean deliverymanInfiniteResources = false;

    @Config.Comment("Amount of initial citizens")
    public static int     maxCitizens         = 4;

    @Config.Comment("Should citizen name tags be rendered?")
    public static boolean alwaysRenderNameTag = true;

    @Config.Comment("Amount of blocks the builder checks (to decrease lag by builder)")
    public static int maxBlocksCheckedByBuilder = 1000;

    @Config.Comment("Chat frequency of worker requests")
    public static int chatFrequency             = 30;

    @Config.Comment("Should in development features be enabled (might be buggy)")
    public static boolean enableInDevelopmentFeatures = false;

    @Config.Comment("Draw pathfinding paths (might be laggy)")
    public static boolean pathfindingDebugDraw      = false;

    @Config.Comment("Verbosity of pathfinding")
    public static int     pathfindingDebugVerbosity = 0;

    @Config.Comment("Amount of additional threads to be used for pathfinding")
    public static int     pathfindingMaxThreadCount = 2;

    @Config.Comment("Blocks players should be able to interact with in any colony (Ex vending machines)")
    public static String[] freeToInteractBlocks = new String[]
                                                    {
                                                      "block:dirt",
                                                      "0 0 0"
                                                    };

    @Config.Comment("Male first names to be used for colonists")
    public static String[] maleFirstNames = new String[]
                                              {
                                                "Aaron",
                                                "Adam",
                                                "Adrian",
                                                "Aidan",
                                                "Aiden",
                                                "Alain",
                                                "Alex",
                                                "Alexander",
                                                "Andrew",
                                                "Anthony",
                                                "Asher",
                                                "Austin",
                                                "Benjamin",
                                                "Brayden",
                                                "Bryson",
                                                "Caden",
                                                "Caleb",
                                                "Callum",
                                                "Camden",
                                                "Cameron",
                                                "Carson",
                                                "Carter",
                                                "Charles",
                                                "Charlie",
                                                "Chase",
                                                "Christian",
                                                "Christopher",
                                                "Cole",
                                                "Colton",
                                                "Connor",
                                                "Cooper",
                                                "Curtis",
                                                "Cyrille",
                                                "Damian",
                                                "Daniel",
                                                "David",
                                                "Declan",
                                                "Diego",
                                                "Diogo",
                                                "Dominic",
                                                "Duarte",
                                                "Dylan",
                                                "Easton",
                                                "Eli",
                                                "Elias",
                                                "Elijah",
                                                "Elliot",
                                                "Ethan",
                                                "Evan",
                                                "Ezra",
                                                "Félix",
                                                "Gabriel",
                                                "Gavin",
                                                "George",
                                                "Grayson",
                                                "Guewen",
                                                "Harrison",
                                                "Henrik",
                                                "Henry",
                                                "Houston",
                                                "Hudson",
                                                "Hugo",
                                                "Hunter",
                                                "Ian",
                                                "Isaac",
                                                "Isaiah",
                                                "Jack",
                                                "Jackson",
                                                "Jacob",
                                                "James",
                                                "Jason",
                                                "Jayce",
                                                "Jayden",
                                                "Jeremiah",
                                                "Jim",
                                                "Joel",
                                                "John",
                                                "Jonathan",
                                                "Jordan",
                                                "Joseph",
                                                "Joshua",
                                                "Josiah",
                                                "Julian",
                                                "Kai",
                                                "Karsen",
                                                "Kevin",
                                                "Kian",
                                                "Landon",
                                                "Leo",
                                                "Levi",
                                                "Liam",
                                                "Lincoln",
                                                "Logan",
                                                "Luís",
                                                "Lucas",
                                                "Luke",
                                                "Mark",
                                                "Mason",
                                                "Mateo",
                                                "Matthew",
                                                "Max",
                                                "Michael",
                                                "Miles",
                                                "Muhammad",
                                                "Nathan",
                                                "Nathanael",
                                                "Nicholas",
                                                "Noah",
                                                "Nolan",
                                                "Oliver",
                                                "Oscar",
                                                "Owen",
                                                "Parker",
                                                "Paul",
                                                "Peter",
                                                "Philibert",
                                                "Rénald",
                                                "Ray",
                                                "Richard",
                                                "Robert",
                                                "Rory",
                                                "Roxan",
                                                "Ryan",
                                                "Samuel",
                                                "Sebastian",
                                                "Steven",
                                                "Thaddee",
                                                "Thomas",
                                                "Tiago",
                                                "Tristan",
                                                "Tyler",
                                                "William",
                                                "Wyatt",
                                                "Xavier",
                                                "Zachary",
                                                "Zane",
                                                "Abraham",
                                                "Allen",
                                                "Ambrose",
                                                "Arthur",
                                                "Avery",
                                                "Barnaby",
                                                "Bartholomew",
                                                "Benedict",
                                                "Bernard",
                                                "Cuthbert",
                                                "Edmund",
                                                "Edward",
                                                "Francis",
                                                "Fulke",
                                                "Geoffrey",
                                                "Gerard",
                                                "Gilbert",
                                                "Giles",
                                                "Gregory",
                                                "Hugh",
                                                "Humphrey",
                                                "Jerome",
                                                "Lancelot",
                                                "Lawrence",
                                                "Leonard",
                                                "Martin",
                                                "Mathias",
                                                "Nathaniel",
                                                "Oswyn",
                                                "Philip",
                                                "Piers",
                                                "Ralph",
                                                "Reynold",
                                                "Roger",
                                                "Rowland",
                                                "Simon",
                                                "Solomon",
                                                "Stephen",
                                                "Tobias",
                                                "Walter",
                                                "William"
                                              };

    @Config.Comment("Female first names to be used for colonists")
    public static String[] femaleFirstNames = new String[]
                                                {
                                                  "Aaliyah",
                                                  "Abigail",
                                                  "Adalyn",
                                                  "Addison",
                                                  "Adeline",
                                                  "Alaina",
                                                  "Alexandra",
                                                  "Alice",
                                                  "Allison",
                                                  "Alyssa",
                                                  "Amelia",
                                                  "Anastasia",
                                                  "Anna",
                                                  "Annabelle",
                                                  "Aria",
                                                  "Arianna",
                                                  "Aubrey",
                                                  "Audrey",
                                                  "Aurora",
                                                  "Ava",
                                                  "Bailey",
                                                  "Barbara",
                                                  "Bella",
                                                  "Betty",
                                                  "Brooklyn",
                                                  "Callie",
                                                  "Camilla",
                                                  "Caroline",
                                                  "Charlotte",
                                                  "Chloe",
                                                  "Claire",
                                                  "Cora",
                                                  "Daniela",
                                                  "Diana",
                                                  "Dorothy",
                                                  "Eleanor",
                                                  "Elena",
                                                  "Eliana",
                                                  "Elizabeth",
                                                  "Ella",
                                                  "Ellie",
                                                  "Emilia",
                                                  "Emilienne",
                                                  "Emily",
                                                  "Emma",
                                                  "Eva",
                                                  "Evelyn",
                                                  "Everly",
                                                  "Filipa",
                                                  "Frédérique",
                                                  "Gabriella",
                                                  "Gianna",
                                                  "Grace",
                                                  "Hailey",
                                                  "Hannah",
                                                  "Harper",
                                                  "Haylie",
                                                  "Hazel",
                                                  "Helen",
                                                  "Isabella",
                                                  "Isabelle",
                                                  "Jade",
                                                  "Jasmine",
                                                  "Jennifer",
                                                  "Jocelyn",
                                                  "Jordyn",
                                                  "Julia",
                                                  "Juliana",
                                                  "Julienne",
                                                  "Karen",
                                                  "Katia",
                                                  "Kaylee",
                                                  "Keira",
                                                  "Kennedy",
                                                  "Kinsley",
                                                  "Kylie",
                                                  "Layla",
                                                  "Leah",
                                                  "Lena",
                                                  "Lila",
                                                  "Liliana",
                                                  "Lillian",
                                                  "Lily",
                                                  "Linda",
                                                  "Lisa",
                                                  "London",
                                                  "Lorena",
                                                  "Luana",
                                                  "Lucy",
                                                  "Luna",
                                                  "Mélanie",
                                                  "Mackenzie",
                                                  "Madelyn",
                                                  "Madison",
                                                  "Maisy",
                                                  "Makayla",
                                                  "Margaret",
                                                  "Maria",
                                                  "Marine",
                                                  "Mary",
                                                  "Maya",
                                                  "Melanie",
                                                  "Mia",
                                                  "Mila",
                                                  "Nancy",
                                                  "Natalie",
                                                  "Natasha",
                                                  "Niamh",
                                                  "Nora",
                                                  "Odile",
                                                  "Olivia",
                                                  "Paisley",
                                                  "Paloma",
                                                  "Paola",
                                                  "Patricia",
                                                  "Penelope",
                                                  "Peyton",
                                                  "Prudence",
                                                  "Reagan",
                                                  "Riley",
                                                  "Sadie",
                                                  "Samantha",
                                                  "Sarah",
                                                  "Savannah",
                                                  "Scarlett",
                                                  "Skyler",
                                                  "Sophia",
                                                  "Sophie",
                                                  "Stella",
                                                  "Susan",
                                                  "Vérane",
                                                  "Vera",
                                                  "Victoria",
                                                  "Violet",
                                                  "Vivian",
                                                  "Zoe",
                                                  "Agnes",
                                                  "Amy",
                                                  "Anne",
                                                  "Avis",
                                                  "Beatrice",
                                                  "Blanche",
                                                  "Bridget",
                                                  "Catherine",
                                                  "Cecily",
                                                  "Charity",
                                                  "Christina",
                                                  "Clemence",
                                                  "Constance",
                                                  "Denise",
                                                  "Edith",
                                                  "Elinor",
                                                  "Ellen",
                                                  "Florence",
                                                  "Fortune",
                                                  "Frances",
                                                  "Frideswide",
                                                  "Gillian",
                                                  "Isabel",
                                                  "Jane",
                                                  "Janet",
                                                  "Joan",
                                                  "Josian",
                                                  "Joyce",
                                                  "Judith",
                                                  "Katherine",
                                                  "Lettice",
                                                  "Mabel",
                                                  "Margery",
                                                  "Marion",
                                                  "Martha",
                                                  "Maud",
                                                  "Mildred",
                                                  "Millicent",
                                                  "Parnell",
                                                  "Philippa",
                                                  "Rachel",
                                                  "Rebecca",
                                                  "Rose",
                                                  "Ruth",
                                                  "Susanna",
                                                  "Sybil",
                                                  "Thomasin",
                                                  "Ursula",
                                                  "Wilmot",
                                                  "Winifred"
                                                };

    @Config.Comment("Last names to be used for colonists")
    public static String[] lastNames = new String[]
                                         {
                                           "Brown",
                                           "Clark",
                                           "Fletcher",
                                           "Harris",
                                           "Johnson",
                                           "Jones",
                                           "Mardle",
                                           "Miller",
                                           "Robinson",
                                           "Smith",
                                           "Taylor",
                                           "Wallgreen",
                                           "White",
                                           "Williams",
                                           "Wilson",
                                           "Abell",
                                           "Ackworth",
                                           "Adams",
                                           "Addicock",
                                           "Alban",
                                           "Aldebourne",
                                           "Alfray",
                                           "Alicock",
                                           "Allard",
                                           "Allington",
                                           "Amberden",
                                           "Amcotts",
                                           "Amondsham",
                                           "Andrews",
                                           "Annesley",
                                           "Ansty",
                                           "Archer",
                                           "Ardall",
                                           "Ardern",
                                           "Argentein",
                                           "Arnold",
                                           "Asger",
                                           "Ashby",
                                           "Ashcombe",
                                           "Ashenhurst",
                                           "Ashton",
                                           "Askew",
                                           "Asplin",
                                           "Astley",
                                           "Atherton",
                                           "Atkinson",
                                           "Atlee",
                                           "Attilburgh",
                                           "Audeley",
                                           "Audlington",
                                           "Ayde",
                                           "Ayleward",
                                           "Aylmer",
                                           "Aynesworth",
                                           "Babham",
                                           "Babington",
                                           "Badby",
                                           "Baker",
                                           "Balam",
                                           "Baldwin",
                                           "Ballard",
                                           "Ballett",
                                           "Bammard",
                                           "Barber",
                                           "Bardolf",
                                           "Barefoot",
                                           "Barker",
                                           "Barnes",
                                           "Barre",
                                           "Barrentine",
                                           "Barrett",
                                           "Barstaple",
                                           "Bartelot",
                                           "Barton",
                                           "Basset",
                                           "Bathurst",
                                           "Battersby",
                                           "Battle",
                                           "Baynton",
                                           "Beauchamp",
                                           "Cheddar",
                                           "Chelsey",
                                           "Chernock",
                                           "Chester",
                                           "Chetwood",
                                           "Cheverell",
                                           "Cheyne",
                                           "Chichester",
                                           "Child",
                                           "Chilton",
                                           "Chowne",
                                           "Chudderley",
                                           "Church",
                                           "Churmond",
                                           "Clavell",
                                           "Claybrook",
                                           "Clement",
                                           "Clerk",
                                           "Clifford",
                                           "Clifton",
                                           "Clitherow",
                                           "Clopton",
                                           "Cobb",
                                           "Cobham",
                                           "Cobley",
                                           "Cockayne",
                                           "Cod",
                                           "Coddington",
                                           "Coffin",
                                           "Coggshall",
                                           "Colby",
                                           "Colkins",
                                           "Collard",
                                           "Colmer",
                                           "Colt",
                                           "Colthurst",
                                           "Complin",
                                           "Compton",
                                           "Conquest",
                                           "Cooke",
                                           "Coorthopp",
                                           "Coppinger",
                                           "Corbett",
                                           "Corby",
                                           "Cossington",
                                           "Cosworth",
                                           "Cotton",
                                           "Courtenay",
                                           "Covert",
                                           "Cowill",
                                           "Cox",
                                           "Crane",
                                           "Cranford",
                                           "Crawley",
                                           "Cressy",
                                           "Crickett",
                                           "Cripps",
                                           "Crisp",
                                           "Cristemas",
                                           "Crocker",
                                           "Crugg",
                                           "Cuddon",
                                           "Culpepper",
                                           "Cunningham",
                                           "Curzon",
                                           "Dagworth",
                                           "Gardiner",
                                           "Gare",
                                           "Garnis",
                                           "Garrard",
                                           "Garret",
                                           "Gascoigne",
                                           "Gasper",
                                           "Gavell",
                                           "Gedding",
                                           "Gerville",
                                           "Geste",
                                           "Gibbs",
                                           "Gifford",
                                           "Gill",
                                           "Ginter",
                                           "Gisborne",
                                           "Gittens",
                                           "Glennon",
                                           "Glover",
                                           "Gobberd",
                                           "Goddam",
                                           "Godfrey",
                                           "Gold",
                                           "Golding",
                                           "Goldwell",
                                           "Gomershall",
                                           "Gomfrey",
                                           "Gonson",
                                           "Good",
                                           "Goodenouth",
                                           "Gooder",
                                           "Goodluck",
                                           "Goodnestone",
                                           "Goodrick",
                                           "Goodrington",
                                           "Goodwin",
                                           "Goring",
                                           "Gorney",
                                           "Gorst",
                                           "Gosebourne",
                                           "Grafton",
                                           "Gray",
                                           "Greene",
                                           "Greenway",
                                           "Grenefeld",
                                           "Greville",
                                           "Grey",
                                           "Grimbald",
                                           "Grobbam",
                                           "Grofhurst",
                                           "Groston",
                                           "Grove",
                                           "Guildford",
                                           "Hackman",
                                           "Haddock",
                                           "Haddon",
                                           "Hadresham",
                                           "Hakebourne",
                                           "Hale",
                                           "Hall",
                                           "Halley",
                                           "Hambard",
                                           "Hammer",
                                           "Hammond",
                                           "Hampden"
                                         };
}
