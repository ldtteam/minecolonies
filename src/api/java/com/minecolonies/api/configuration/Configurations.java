package com.minecolonies.api.configuration;

import net.minecraftforge.common.config.Config;

import static com.minecolonies.api.util.constant.Constants.*;

@Config(modid = MOD_ID)
public class Configurations
{
    @Config.Comment("All configuration related to gameplay")
    public static Gameplay gameplay = new Gameplay();

    @Config.Comment("All configurations related to pathfinding")
    public static Pathfinding pathfinding = new Pathfinding();

    @Config.Comment("All configurations related to citizen names")
    public static Names names = new Names();

    @Config.Comment("All configurations related to the request system")
    public static RequestSystem requestSystem = new RequestSystem();

    public static class Gameplay
    {
        @Config.Comment("Should builder place construction tape?")
        public boolean builderPlaceConstructionTape = true;

        @Config.Comment("Colony size (radius)")
        public int workingRangeTownHall = 100;

        @Config.Comment("Padding between colonies")
        public int townHallPadding = 20;

        @Config.Comment("Should supply chests be craftable on this server?")
        public boolean supplyChests = true;

        @Config.Comment("Should players be able to place an infinite amount of supplychests?")
        public boolean allowInfiniteSupplyChests = false;

        @Config.RangeInt(min = (CITIZEN_RESPAWN_INTERVAL_MIN), max = CITIZEN_RESPAWN_INTERVAL_MAX)
        @Config.Comment("Average citizen respawn interval (in seconds)")
        public int citizenRespawnInterval = 60;

        @Config.Comment("Should builder and miner build without resources? (this also turns off what they produce)")
        public boolean builderInfiniteResources = false;

        @Config.Comment("Should there be at max 1 warehouse per colony?")
        public boolean limitToOneWareHousePerColony = true;

        @Config.Comment("Delay after each block placement (Increasing it, increases the delay)")
        public int builderBuildBlockDelay = 15;

        @Config.Comment("Delay modifier to mine a block (Decreasing it, decreases the delay)")
        public int blockMiningDelayModifier = 500;

        @Config.Comment("Should worker work during the rain?")
        public boolean workersAlwaysWorkInRain = false;

        @Config.Comment("Should the colony protection be enabled?")
        public boolean enableColonyProtection = true;

        @Config.Comment("Independend from the colony protection, should explosions be turned off?")
        public boolean turnOffExplosionsInColonies = true;

        @Config.Comment("Whether or not to spawn barbarians")
        public boolean doBarbariansSpawn = true;

        @Config.RangeInt(min = (MIN_BARBARIAN_DIFFICULTY), max = MAX_BARBARIAN_DIFFICULTY)
        @Config.Comment("The difficulty setting for barbarians")
        public int barbarianHordeDifficulty = 5;

        @Config.RangeInt(min = (MIN_BARBARIAN_HORDE_SIZE), max = MAX_BARBARIAN_HORDE_SIZE)
        @Config.Comment("The max size of a barbarian horde")
        public int maxBarbarianHordeSize = 40;

        @Config.Comment("The average amount of nights between raids")
        public int averageNumberOfNightsBetweenRaids = 3;

        @Config.Comment("Should players be allowed to build their colonies over existing villages?")
        public boolean protectVillages = false;
        /* schematics usage */
        @Config.Comment("Should the default schematics be ignored (from the jar)?")
        public boolean ignoreSchematicsFromJar = false;

        @Config.Comment("Should player made schematics be allowed")
        public boolean allowPlayerSchematics = false;

        @Config.Comment("Max amount of schematics to be cached on the server")
        public int maxCachedSchematics = 100;

        @Config.Comment("Should players be allowed to change names? -1 for false, 0 for specific groups, 1 for true")
        public  int allowGlobalNameChanges = 1;

        @Config.Comment("Players who have special permission (Patreons for example)")
        public  String[] specialPermGroup = new String[]
                {
                        "_Raycoms_"
                };

    /* Command configs */

        @Config.Comment("Time until a next teleport can be executed (in seconds)")
        public int teleportBuffer = 120;

        @Config.Comment("Which level counts as op level on the server")
        public int opLevelForServer = 3;

        @Config.Comment("Sets the amount of hours until a colony will be deleted after not seeing it's mayor, set to zero to disable")
        public int     autoDeleteColoniesInHours = 0;
        @Config.Comment("Sets weither or not Colony structures are destroyed automatically.")
        public boolean autoDestroyColonyBlocks   = true;
        @Config.Comment("Should the player be allowed to use the '/mc rtp' command?")
        public boolean canPlayerUseRTPCommand    = true;

        @Config.Comment("Should the player be allowed to use the '/mc colony teleport' command?")
        public boolean canPlayerUseColonyTPCommand = false;

        @Config.Comment("Should the player be allowed to use the '/mc home' command?")
        public boolean canPlayerUseHomeTPCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc citizens info' command?")
        public boolean canPlayerUseCitizenInfoCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc citizens list' command?")
        public boolean canPlayerUseListCitizensCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc citizens respawn' command?")
        public boolean canPlayerRespawnCitizensCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc colony info' command?")
        public boolean canPlayerUseShowColonyInfoCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc citizens kill' command?")
        public boolean canPlayerUseKillCitizensCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc colony addOfficer' command?")
        public boolean canPlayerUseAddOfficerCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc colony delete' command?")
        public boolean canPlayerUseDeleteColonyCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc colony refresh' command?")
        public boolean canPlayerUseRefreshColonyCommand = false;

        @Config.Comment("Should the player be allowed to use the '/mc backup' command?")
        public boolean canPlayerUseBackupCommand = false;

        /* Colony TP configs */
        @Config.Comment("Amount of attemps to find a save rtp")
        public int numberOfAttemptsForSafeTP = 4;

        @Config.Comment("Max distance from world spawn")
        public int maxDistanceFromWorldSpawn = 8000;

        @Config.Comment("Min distance from world spawn")
        public int minDistanceFromWorldSpawn = 512;

        @Config.Comment("Should the dman create resources out of hot air (Not implemented)")
        public boolean deliverymanInfiniteResources = false;

        @Config.Comment("Amount of initial citizens")
        public int maxCitizens = 4;

        @Config.Comment("Should citizen name tags be rendered?")
        public boolean alwaysRenderNameTag = true;

        @Config.Comment("Amount of blocks the builder checks (to decrease lag by builder)")
        public int maxBlocksCheckedByBuilder = 1000;

        @Config.Comment("Chat frequency of worker requests")
        public int chatFrequency = 30;

        @Config.Comment("Should in development features be enabled (might be buggy)")
        public boolean enableInDevelopmentFeatures = false;

        @Config.Comment("Blocks players should be able to interact with in any colony (Ex vending machines)")
        public String[] freeToInteractBlocks = new String[]
                                                 {
                                                   "block:dirt",
                                                   "0 0 0"
                                                 };
    }

    public static class Pathfinding
    {
        @Config.Comment("Draw pathfinding paths (might be laggy)")
        public boolean pathfindingDebugDraw = false;

        @Config.Comment("Verbosity of pathfinding")
        public int pathfindingDebugVerbosity = 0;

        @Config.Comment("Amount of additional threads to be used for pathfinding")
        public int pathfindingMaxThreadCount = 2;
    }

    public static class Names
    {
        @Config.Comment("Male first names to be used for colonists")
        public String[] maleFirstNames = new String[]
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
        public String[] femaleFirstNames = new String[]
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
        public String[] lastNames = new String[]
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

    public static class RequestSystem
    {
        @Config.Comment("Should the request system print out debug information? Useful in case of malfunctioning of set system.")
        public boolean enableDebugLogging = false;

        @Config.Comment("The maximal amount of tries that the request system will perform for retryable requests. Higher increases server load.")
        public int maximalRetries = 3;

        @Config.Comment("The amount of ticks between retries of the request system for retryable requests. Lower increases server load.")
        public int delayBetweenRetries = 1200;

        @Config.Comment("The maximal amount of buildings the Delivery Man should try to gather before attempting a drop off at the warehouse.")
        public int maximalBuildingsToGather = 6;

        @Config.Comment("The minimal amount of buildings the Delivery Man should try to gather before attempting a drop off at the warehouse.")
        public int minimalBuildingsToGather = 3;

        @Config.Comment("Should the request system creatively resolve (if possible) when the player is required to resolve a request.")
        public boolean creativeResolve = false;

        @Config.Comment("Should the player be allowed to use the '/mc colony rs reset' command?")
        public boolean canPlayerUseResetCommand = false;
    }
}
