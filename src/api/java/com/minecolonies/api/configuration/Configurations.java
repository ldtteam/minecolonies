package com.minecolonies.api.configuration;

@SuppressWarnings({"squid:S1444", "squid:ClassVariableVisibilityCheck", "squid:S2386"})
public class Configurations
{

    private Configurations()
    {
        // Private to hide implicit
    }

    public static class Gameplay
    {

        /// --- General --- \\\

        public static int     averageNumberOfNightsBetweenRaids = 3;
        public static int     barbarianHordeDifficulty          = 5;
        public static int     builderBuildBlockDelay            = 15;
        public static int     townHallPadding                   = 20;
        public static int     maxBarbarianHordeSize             = 40;
        public static int     citizenRespawnInterval            = 60;
        public static int     workingRangeTownHall              = 100;
        public static int     blockMiningDelayModifier          = 500;
        public static boolean builderPlaceConstructionTape      = true;
        public static boolean supplyChests                      = true;
        public static boolean limitToOneWareHousePerColony      = true;
        public static boolean enableColonyProtection            = true;
        public static boolean turnOffExplosionsInColonies       = true;
        public static boolean doBarbariansSpawn                 = true;
        public static boolean allowInfiniteSupplyChests         = false;
        public static boolean builderInfiniteResources          = false;
        public static boolean protectVillages                   = false;
        public static boolean workersAlwaysWorkInRain           = false;

        /// --- Schematics --- \\\

        public static int      allowGlobalNameChanges  = 1;
        public static int      maxCachedSchematics     = 100;
        public static boolean  ignoreSchematicsFromJar = false;
        public static boolean  allowPlayerSchematics   = false;
        public static String[] specialPermGroup        = new String[]
                                                           {
                                                             "_Raycoms_",
                                                             "Asherslab"
                                                           };

        /// --- Command Configs --- \\\

        public static int     autoDeleteColoniesInHours         = 0;
        public static int     opLevelForServer                  = 3;
        public static int     teleportBuffer                    = 120;
        public static boolean autoDestroyColonyBlocks           = true;
        public static boolean canPlayerUseRTPCommand            = true;
        public static boolean canPlayerUseHomeTPCommand         = true;
        public static boolean canPlayerUseCitizenInfoCommand    = true;
        public static boolean canPlayerUseListCitizensCommand   = true;
        public static boolean canPlayerRespawnCitizensCommand   = true;
        public static boolean canPlayerUseShowColonyInfoCommand = true;
        public static boolean canPlayerUseKillCitizensCommand   = true;
        public static boolean canPlayerUseAddOfficerCommand     = true;
        public static boolean canPlayerUseDeleteColonyCommand   = true;
        public static boolean canPlayerUseColonyTPCommand       = false;
        public static boolean canPlayerUseRefreshColonyCommand  = false;
        public static boolean canPlayerUseBackupCommand         = false;

        /// --- Colony TP Settings --- \\\

        public static int      numberOfAttemptsForSafeTP    = 4;
        public static int      maxCitizens                  = 4;
        public static int      chatFrequency                = 30;
        public static int      minDistanceFromWorldSpawn    = 512;
        public static int      maxBlocksCheckedByBuilder    = 1000;
        public static int      maxDistanceFromWorldSpawn    = 8000;
        public static boolean  alwaysRenderNameTag          = true;
        public static boolean  deliverymanInfiniteResources = false;
        public static boolean  enableInDevelopmentFeatures  = false;
        public static String[] freeToInteractBlocks         = new String[]
                                                                {
                                                                  "block:dirt",
                                                                  "0 0 0"
                                                                };

        private Gameplay()
        {
            // Private to hide implicit
        }

    }

    public static class Pathfinding
    {
        public static int     pathfindingDebugVerbosity = 0;
        public static int     pathfindingMaxThreadCount = 2;
        public static boolean pathfindingDebugDraw      = false;

        private Pathfinding()
        {
            // Private to hide implicit
        }

    }

    public static class Names
    {

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
                                        "parry",
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

        private Names()
        {
            // Private to hide implicit
        }

    }

    public static class RequestSystem
    {

        public static int     maximalRetries           = 3;
        public static int     minimalBuildingsToGather = 3;
        public static int     maximalBuildingsToGather = 6;
        public static int     delayBetweenRetries      = 1200;
        public static boolean enableDebugLogging       = false;
        public static boolean creativeResolve          = false;

        private RequestSystem()
        {
            // Private to hide implicit
        }

    }
}
