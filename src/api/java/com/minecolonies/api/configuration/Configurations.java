package com.minecolonies.api.configuration;

import net.minecraftforge.common.config.Config;

import static com.minecolonies.api.util.constant.Constants.*;

@Config(modid = MOD_ID)
public class Configurations
{
    @Config.Comment("All configuration related to gameplay")
    public static Gameplay gameplay = new Gameplay();

    @Config.Comment("All configuration related to mod compatibility")
    public static Compatibility compatibility = new Compatibility();

    @Config.Comment("All configurations related to pathfinding")
    public static Pathfinding pathfinding = new Pathfinding();

    @Config.Comment("All configurations related to the request system")
    public static RequestSystem requestSystem = new RequestSystem();

    public static class Gameplay
    {
        @Config.Comment("Should builder place construction tape? [Default: true]")
        public boolean builderPlaceConstructionTape = true;

        @Config.Comment("Max distance a colony can claim a chunk from the center, 0 if disable maximum.  [Default: 0]")
        public int workingRangeTownHall = 0;

        @Config.Comment("Colony size (radius in chunks around central colony chunk). Only for the static mode. [Default: 8]")
        public int workingRangeTownHallChunks = 8;

        @Config.Comment("The minimum distances between town halls for dynamic colony sizes (used as default initial claim too). [Default: 3]")
        public int minTownHallPadding = 3;

        @Config.Comment("Padding between colonies  - deprecated, don't use.  [Default: 20]")
        public int townHallPadding = 20;

        @Config.Comment("Padding between colonies in chunks. [Default: 1]")
        public int townHallPaddingChunk = 1;

        @Config.Comment("Should player get one guidebook on first join to a new world? [Default: true]")
        public boolean playerGetsGuidebookOnFirstJoin = true;

        @Config.Comment("Should supply chests be craftable on this server? [Default: true]")
        public boolean supplyChests = true;

        @Config.Comment("Should players be able to place an infinite amount of supplychests? [Default: false]")
        public boolean allowInfiniteSupplyChests = false;

        @Config.Comment("Should players be allowed to abandon their colony to create a new one easily? [Default: false]")
        public boolean allowInfiniteColonies = false;

        @Config.RangeInt(min = (CITIZEN_RESPAWN_INTERVAL_MIN), max = CITIZEN_RESPAWN_INTERVAL_MAX)
        @Config.Comment("Average citizen respawn interval (in seconds). [Default: 60]")
        public int citizenRespawnInterval = 60;

        @Config.Comment("Max citizens in one colony. [Default: 50]")
        public int maxCitizenPerColony = 50;

        @Config.Comment("Should builder and miner build without resources? (this also turns off what they produce). [Default: false]")
        public boolean builderInfiniteResources = false;

        @Config.Comment("Should there be at max 1 warehouse per colony?. [Default: true]")
        public boolean limitToOneWareHousePerColony = true;

        @Config.Comment("Delay after each block placement (Increasing it, increases the delay) [Default: 15]")
        @Config.RangeInt(min = 1, max = 500)
        public int builderBuildBlockDelay = 15;

        @Config.Comment("Delay modifier to mine a block (Decreasing it, decreases the delay) [Default: 500]")
        public int blockMiningDelayModifier = 500;

        @Config.Comment("Ores for the miner to mine that aren't autodetected")
        public String[] extraOres = new String[]
                                      {
                                        "minestuck:ore_cruxite",
                                        "minestuck:ore_uranium",
                                      };

        @Config.Comment("Should workers work during the rain? [Default: false]")
        public boolean workersAlwaysWorkInRain = false;

        @Config.Comment("Should the colony protection be enabled? [Default: true]")
        public boolean enableColonyProtection = true;

        @Config.Comment("Should Players be sent entering/leaving colony notifications? [Default: true]")
        public boolean sendEnteringLeavingMessages = true;

        @Config.Comment("Independent from the colony protection, should explosions be turned off? [Default: true]")
        public boolean turnOffExplosionsInColonies = true;

        @Config.Comment("Whether or not to spawn barbarians. [Default: true]")
        public boolean doBarbariansSpawn = true;

        @Config.RangeInt(min = (MIN_BARBARIAN_DIFFICULTY), max = MAX_BARBARIAN_DIFFICULTY)
        @Config.Comment("The difficulty setting for barbarians. [Default: 5]")
        public int barbarianHordeDifficulty = 5;

        @Config.RangeInt(min = (MIN_SPAWN_BARBARIAN_HORDE_SIZE), max = MAX_SPAWN_BARBARIAN_HORDE_SIZE)
        @Config.Comment("The spawn size of a barbarian horde. [Default: 5]")
        public int spawnBarbarianSize = 5;

        @Config.RangeInt(min = (MIN_BARBARIAN_HORDE_SIZE), max = MAX_BARBARIAN_HORDE_SIZE)
        @Config.Comment("The max size of a barbarian horde. [Default: 20]")
        public int maxBarbarianSize = 20;

        @Config.Comment("Whether or not to barbarians can break, scale, bridge obstacles. [Default: true]")
        public boolean doBarbariansBreakThroughWalls = true;

        @Config.Comment("The average amount of nights between raids. [Default: 3]")
        public int averageNumberOfNightsBetweenRaids = 3;

        @Config.Comment("The minimum number of nights between raids. [Default: 1]")
        public int minimumNumberOfNightsBetweenRaids = 1;

        // TODO: change to true over time
        @Config.Comment("Should Mobs attack citizens? [Default: false")
        public boolean mobAttackCitizens = false;

        @Config.Comment("Should Citizens call guards for help when attacked? default:true")
        public boolean citizenCallForHelp = true;

        @Config.Comment("Should players be allowed to build their colonies over existing villages? [Default: false]")
        public boolean protectVillages         = false;

        @Config.Comment("Should player made schematics be allowed [Default: false]")
        public boolean allowPlayerSchematics = false;

        @Config.Comment("Should players be allowed to change names? -1 for false, 0 for specific groups, 1 for true. [Default: 1]")
        public int allowGlobalNameChanges = 1;

        @Config.Comment("Players who have special permission (Patreons for example)")
        public String[] specialPermGroup = new String[]
                                             {
                                               "_Raycoms_"
                                             };

        /* Command configs */

        @Config.Comment("Time until a next teleport can be executed (in seconds). [Default: 120]")
        public int teleportBuffer = 120;

        @Config.Comment("Which level counts as op level on the server. [Default: 3]")
        public int opLevelForServer = 3;

        @Config.Comment("Sets the amount of hours until a colony will be deleted after not seeing it's mayor, set to zero to disable. [Default: 0]")
        public int     autoDeleteColoniesInHours = 0;

        @Config.Comment("Sets weither or not Colony structures are destroyed automatically. [Default: true]")
        public boolean autoDestroyColonyBlocks   = true;

        @Config.Comment("Should the player be allowed to use the '/mc rtp' command? [Default: true]")
        public boolean canPlayerUseRTPCommand    = true;

        @Config.Comment("Should the player be allowed to use the '/mc colony teleport' command? [Default: false]")
        public boolean canPlayerUseColonyTPCommand = false;

        @Config.Comment("Should the player be allowed to use the '/mc home' command? [Default: true]")
        public boolean canPlayerUseHomeTPCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc citizens info' command? [Default: true]")
        public boolean canPlayerUseCitizenInfoCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc citizens list' command? [Default: true]")
        public boolean canPlayerUseListCitizensCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc citizens respawn' command? [Default: true]")
        public boolean canPlayerRespawnCitizensCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc colony info' command? [Default: true]")
        public boolean canPlayerUseShowColonyInfoCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc citizens kill' command? [Default: true]")
        public boolean canPlayerUseKillCitizensCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc colony addOfficer' command? [Default: true]")
        public boolean canPlayerUseAddOfficerCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc colony delete' command? [Default: true]")
        public boolean canPlayerUseDeleteColonyCommand = true;

        @Config.Comment("Should the player be allowed to use the '/mc colony refresh' command? [Default: false]")
        public boolean canPlayerUseRefreshColonyCommand = false;

        @Config.Comment("Should the player be allowed to use the '/mc backup' command? [Default: false]")
        public boolean canPlayerUseBackupCommand = false;

        /* Colony TP configs */
        @Config.Comment("Amount of attempts to find a save rtp. [Default: 4]")
        public int numberOfAttemptsForSafeTP = 4;

        @Config.Comment("Should the min/max distance from spawn also affect colony placement? [Default: false]")
        public boolean restrictColonyPlacement = false;

        @Config.Comment("Should the colony have a fixed radius or should it be dynamic. [Default: false]")
        public boolean enableDynamicColonySizes = false;

        @Config.Comment("Max distance from world spawn. [Default: 8000]")
        public int maxDistanceFromWorldSpawn = 8000;

        @Config.Comment("Min distance from world spawn. [Default: 512]")
        public int minDistanceFromWorldSpawn = 512;

        @Config.Comment("Amount of initial citizens. [Default: 4]")
        public int initialCitizenAmount = 4;

        @Config.Comment("Should citizen name tags be rendered? [Default: true]")
        public boolean alwaysRenderNameTag = true;

        @Config.Comment("Child growth modifier, default on avg they take about 60min to grow (at 1.0x modifier). Setting to 5 = 5x as fast. [Default: 1]")
        public double growthModifier = 1.0;

        @Config.Comment("Should Guard Rangers benefit from Power/Smite/Bane of Arthropods enchants? [Default: true]")
        public boolean rangerEnchants = true;

        @Config.Comment("Damage multiplier for Ranger Guards. [Default: 1.0]")
        public double rangerDamageMult = 1.0;

        @Config.Comment("Damage multiplier for Knight Guards. [Default: 1.0]")
        public double knightDamageMult = 1.0;

        @Config.Comment("Health multiplier for all Guards. [Default: 1.0]")
        public double guardHealthMult = 1.0;

        @Config.Comment("Amount of blocks the builder checks (to decrease lag by builder). [Default: 1000]")
        public int maxBlocksCheckedByBuilder = 1000;

        @Config.Comment("Chat frequency of worker requests. [Default: 30]")
        public int chatFrequency = 30;

        @Config.Comment("Should in development features be enabled (might be buggy). [Default: false]")
        public boolean enableInDevelopmentFeatures = false;

        @Config.Comment("Blocks players should be able to interact with in any colony (Ex vending machines)")
        public String[] freeToInteractBlocks  = new String[]
                                                  {
                                                    "block:dirt",
                                                    "0 0 0"
                                                  };
        @Config.Comment("Should colonies in other dimensions be allowed? [Default: false]")
        public boolean  allowOtherDimColonies = false;

        @Config.Comment("ResourceLocations for extra entities for the GuardHut's list. \n"
                          + "once done you'll need to recalculate the list."
                          + "EntityMob's already calculated in list.")
        public String[] guardResourceLocations = new String[]
                                                   {
                                                     "minecraft:slime",
                                                     "tconstruct:blueslime"
                                                   };

        @Config.Comment("List of items the Students in the library can use. \n"
                          + "Format: itemname;SkillIncreasePCT[100-1000];BreakPCT[0-100] \n"
                          + "Example: minecraft:paper;400;100 \n"
                          + "Which adds minecraft Paper with a 400%(4x) increased chance to skillup and a 100% chance to be used up during the try to skillup")
        public final String[] configListStudyItems = new String[]
                                                       {
                                                         "minecraft:paper;400;100"
                                                       };

        @Config.Comment("The items and item-tags that the composter can use to produce compost.")
        public String[] listOfCompostableItems = new String[]
                                                   {
                                                     "minecraft:rotten_flesh",
                                                     "minecraft:tallgrass",
                                                     "minecraft:yellow_flower",
                                                     "minecraft:red_flower",
                                                     "minecraft:brown_mushroom",
                                                     "minecraft:red_mushroom",
                                                     "minecraft:double_plant",
                                                     "minecraft:feather",
                                                     "food",
                                                     "seed",
                                                     "treeSapling"
                                                   };

        @Config.Comment("The items and item-tags that the florist can plant.")
        public String[] listOfPlantables = new String[]
                                                   {
                                                     "minecraft:tallgrass",
                                                     "minecraft:yellow_flower",
                                                     "minecraft:red_flower",
                                                     "minecraft:double_plant",
                                                     "minecraft:cactus",
                                                     "minecraft:reeds",
                                                   };

        @Config.Comment("Turn on Minecolonies pvp mode, attention (colonies can be destroyed and can be griefed under certain conditions). [Default: false]")
        public boolean pvp_mode = false;

        @Config.Comment("Days until the pirate ships despawn again. [Default: 3]")
        public int daysUntilPirateshipsDespawn = 3;

        @Config.Comment("Should special holiday content be displayed? [Default: true]")
        public boolean holidayFeatures = true;

        @Config.Comment("AI Update rate, increase to improve performance. [Default: 1]")
        @Config.RangeInt(min = 1,max = 10000)
        public int updateRate = 1;

        @Config.Comment("Quantity of dirt per Compost filling. [Default: 1]")
        public int dirtFromCompost = 1;

        @Config.Comment("Chance to get a lucky block in percent. [Default: 1]")
        public int luckyBlockChance = 1;

        @Config.Comment("The blocks where the miner has a chance to get a random ore.")
        public String[] luckyBlocks = new String[]
                                                {
                                                  "minecraft:stone",
                                                  "minecraft:cobblestone",
                                                };

        @Config.Comment("The random ores the miner can get separated by ! for rarity")
        public String[] luckyOres = new String[]
                                              {
                                                "minecraft:coal_ore!64",
                                                "minecraft:iron_ore!32",
                                                "minecraft:gold_ore!16",
                                                "minecraft:redstone_ore!8",
                                                "minecraft:lapis_ore!4",
                                                "minecraft:diamond_ore!2",
                                                "minecraft:emerald_ore!1"
                                              };

        @Config.Comment("What the crusher can produce at the cost of 2:1")
        public String[] crusherProduction = new String[]
                                              {
                                                "minecraft:cobblestone!minecraft:gravel",
                                                "minecraft:gravel!minecraft:sand",
                                                "minecraft:sand!minecraft:clay"
                                              };

        @Config.Comment("Automatically fix orphaned chunks which were caused by chunk loading and saving issues. [Default: false]")
        public boolean fixOrphanedChunks = false;

        @Config.Comment("Max Y level for Barbarians to spawn. [Default: 200]")
        public int maxYForBarbarians = 200;

        @Config.Comment("If the builder should be slower underground or as fast as anywhere else. [Default: true]")
        public boolean restrictBuilderUnderground = true;

        @Config.Comment("The different meshes which can be bought in the building with durability")
        public String[] sifterMeshes = new String[]
                                   {
                                     "minecraft:string,0",
                                     "minecraft:flint,0.1",
                                     "minecraft:iron_ingot,0.1",
                                     "minecraft:diamond,0.1"
                                   };

        @Config.Comment("The blocks which can be sifted for items")
        public String[] siftableBlocks = new String[]
                                   {
                                     "minecraft:dirt",
                                     "minecraft:sand",
                                     "minecraft:gravel",
                                     "minecraft:soul_sand",
                                   };

        @Config.Comment("The possible drops from sifting - keyBlock, keyMesh, item, probability")
        public String[] sifterDrops = new String[]
                                        {
                                          //Dirt with String mesh
                                          "0,0,minecraft:wheat_seeds,25",
                                          "0,0,minecraft:sapling:0,1",
                                          "0,0,minecraft:sapling:1,1",
                                          "0,0,minecraft:sapling:2,1",
                                          "0,0,minecraft:sapling:3,1",

                                          //Dirt with flint mesh
                                          "0,1,minecraft:wheat_seeds,50",
                                          "0,1,minecraft:sapling:0,5",
                                          "0,1,minecraft:sapling:1,5",
                                          "0,1,minecraft:sapling:2,5",
                                          "0,1,minecraft:sapling:3,5",
                                          "0,1,minecraft:carrot:0,1",
                                          "0,1,minecraft:potato:0,1",

                                          //Dirt with iron mesh
                                          "0,2,minecraft:wheat_seeds,50",
                                          "0,2,minecraft:sapling:0,10",
                                          "0,2,minecraft:sapling:1,10",
                                          "0,2,minecraft:sapling:2,10",
                                          "0,2,minecraft:sapling:3,10",
                                          "0,2,minecraft:pumpkin_seeds:0,1",
                                          "0,2,minecraft:melon_seeds:0,1",
                                          "0,2,minecraft:beetroot_seeds:0,1",
                                          "0,2,minecraft:carrot:0,1",
                                          "0,2,minecraft:potato:0,1",
                                          "0,2,minecraft:sapling:4,1",
                                          "0,2,minecraft:sapling:5,1",

                                          //Dirt with diamond mesh
                                          "0,3,minecraft:wheat_seeds,25",
                                          "0,3,minecraft:sapling:0,10",
                                          "0,3,minecraft:sapling:1,10",
                                          "0,3,minecraft:sapling:2,10",
                                          "0,3,minecraft:sapling:3,10",
                                          "0,3,minecraft:pumpkin_seeds:0,5",
                                          "0,3,minecraft:melon_seeds:0,5",
                                          "0,3,minecraft:beetroot_seeds:0,5",
                                          "0,3,minecraft:carrot:0,5",
                                          "0,3,minecraft:potato:0,5",
                                          "0,3,minecraft:sapling:4,5",
                                          "0,3,minecraft:sapling:5,5",

                                          //Sand with string mesh
                                          "1,0,minecraft:cactus,2.5",
                                          "1,0,minecraft:reeds,2.5",

                                          //Sand with flint mesh
                                          "1,1,minecraft:cactus,5",
                                          "1,1,minecraft:reeds,5",
                                          "1,1,minecraft:gold_nugget,5",

                                          //Sand with iron mesh
                                          "1,2,minecraft:cactus,10",
                                          "1,2,minecraft:reeds,10",
                                          "1,2,minecraft:dye:3,10",
                                          "1,2,minecraft:gold_nugget,10",

                                          //Sand with diamond mesh
                                          "1,3,minecraft:cactus,15",
                                          "1,3,minecraft:reeds,15",
                                          "1,3,minecraft:dye:3,15",
                                          "1,3,minecraft:gold_nugget,15",

                                          //Gravel with string mesh
                                          "2,0,minecraft:iron_nugget,5",
                                          "2,0,minecraft:flint,5",
                                          "2,0,minecraft:coal,5",

                                          //Gravel with flint mesh
                                          "2,1,minecraft:redstone,10",
                                          "2,1,minecraft:iron_nugget,10",
                                          "2,1,minecraft:flint,10",
                                          "2,1,minecraft:coal,10",

                                          //Gravel with iron mesh
                                          "2,2,minecraft:redstone,15",
                                          "2,2,minecraft:iron_nugget,15",
                                          "2,2,minecraft:coal,15",
                                          "2,2,minecraft:dye:4,5",
                                          "2,2,minecraft:iron_ingot,1",
                                          "2,2,minecraft:gold_ingot,1",
                                          "2,2,minecraft:emerald,1",
                                          "2,2,minecraft:diamond,1",

                                          //Gravel with diamond mesh
                                          "2,3,minecraft:redstone,20",
                                          "2,3,minecraft:coal,20",
                                          "2,3,minecraft:dye:4,10",
                                          "2,3,minecraft:iron_ingot,2.5",
                                          "2,3,minecraft:gold_ingot,2.5",
                                          "2,3,minecraft:emerald,2.5",
                                          "2,3,minecraft:diamond,2.5",

                                          //Soulsand with string mesh
                                          "3,0,minecraft:nether_wart,5",
                                          "3,0,minecraft:quartz,5",

                                          //Soulsand with flint mesh
                                          "3,1,minecraft:nether_wart,10",
                                          "3,1,minecraft:quartz,10",
                                          "3,1,minecraft:glowstone_dust,5",

                                          //Soulsand with iron mesh
                                          "3,2,minecraft:nether_wart,10",
                                          "3,2,minecraft:quartz,10",
                                          "3,2,minecraft:glowstone_dust,10",
                                          "3,2,minecraft:blaze_powder,1",
                                          "3,2,minecraft:magma_cream,1",

                                          //Soulsand with diamond mesh
                                          "3,3,minecraft:nether_wart,15",
                                          "3,3,minecraft:quartz,15",
                                          "3,3,minecraft:glowstone_dust,15",
                                          "3,3,minecraft:blaze_powder,5",
                                          "3,3,minecraft:magma_cream,5",
                                          "3,3,minecraft:skull:3,5",
                                        };

        @Config.Comment("The possible enchantments the enchanter worker can generate")
        public String[] enchantments = new String[]
                                           {
                                             "1,minecraft:aqua_affinity,1,50",
                                             "1,minecraft:bane_of_arthropods,1,50",
                                             "1,minecraft:blast_protection,1,50",
                                             "1,minecraft:depth_strider,1,50",
                                             "1,minecraft:feather_falling,1,50",
                                             "1,minecraft:fire_aspect,1,50",
                                             "1,minecraft:fire_protection,1,50",
                                             "1,minecraft:flame,1,50",
                                             "1,minecraft:frost_walker,1,50",
                                             "1,minecraft:knockback,1,50",
                                             "1,minecraft:looting,1,50",
                                             "1,minecraft:power,1,50",
                                             "1,minecraft:projectile_protection,1,50",
                                             "1,minecraft:protection,1,50",
                                             "1,minecraft:punch,1,50",
                                             "1,minecraft:respiration,1,50",
                                             "1,minecraft:sharpness,1,50",
                                             "1,minecraft:smite,1,50",
                                             "1,minecraft:sweeping_edge,1,50",
                                             "1,minecraft:unbreaking,1,50",

                                             "2,minecraft:aqua_affinity,2,25",
                                             "2,minecraft:bane_of_arthropods,2,25",
                                             "2,minecraft:blast_protection,2,25",
                                             "2,minecraft:depth_strider,2,25",
                                             "2,minecraft:feather_falling,2,25",
                                             "2,minecraft:fire_aspect,2,25",
                                             "2,minecraft:fire_protection,2,25",
                                             "2,minecraft:flame,2,25",
                                             "2,minecraft:frost_walker,2,25",
                                             "2,minecraft:knockback,2,25",
                                             "2,minecraft:looting,2,25",
                                             "2,minecraft:power,2,25",
                                             "2,minecraft:projectile_protection,2,25",
                                             "2,minecraft:protection,2,25",
                                             "2,minecraft:punch,2,25",
                                             "2,minecraft:respiration,2,25",
                                             "2,minecraft:sharpness,2,25",
                                             "2,minecraft:smite,2,25",
                                             "2,minecraft:sweeping_edge,2,25",
                                             "2,minecraft:unbreaking,2,25",

                                             "3,minecraft:aqua_affinity,3,15",
                                             "3,minecraft:bane_of_arthropods,3,15",
                                             "3,minecraft:blast_protection,3,15",
                                             "3,minecraft:depth_strider,3,15",
                                             "3,minecraft:feather_falling,3,15",
                                             "3,minecraft:fire_aspect,3,15",
                                             "3,minecraft:fire_protection,3,15",
                                             "3,minecraft:flame,3,15",
                                             "3,minecraft:frost_walker,3,15",
                                             "3,minecraft:knockback,3,15",
                                             "3,minecraft:looting,3,15",
                                             "3,minecraft:power,3,15",
                                             "3,minecraft:projectile_protection,3,15",
                                             "3,minecraft:protection,3,15",
                                             "3,minecraft:punch,3,15",
                                             "3,minecraft:respiration,3,15",
                                             "3,minecraft:sharpness,3,15",
                                             "3,minecraft:smite,3,15",
                                             "3,minecraft:sweeping_edge,3,15",
                                             "3,minecraft:unbreaking,3,15",

                                             "4,minecraft:aqua_affinity,4,5",
                                             "4,minecraft:bane_of_arthropods,4,5",
                                             "4,minecraft:blast_protection,4,5",
                                             "4,minecraft:depth_strider,4,5",
                                             "4,minecraft:feather_falling,4,5",
                                             "4,minecraft:fire_aspect,4,5",
                                             "4,minecraft:fire_protection,4,5",
                                             "4,minecraft:flame,4,5",
                                             "4,minecraft:frost_walker,4,5",
                                             "4,minecraft:infinity,1,5",
                                             "4,minecraft:knockback,4,5",
                                             "4,minecraft:looting,4,5",
                                             "4,minecraft:power,4,5",
                                             "4,minecraft:projectile_protection,4,5",
                                             "4,minecraft:protection,4,5",
                                             "4,minecraft:punch,4,5",
                                             "4,minecraft:respiration,4,5",
                                             "4,minecraft:sharpness,4,5",
                                             "4,minecraft:smite,4,5",
                                             "4,minecraft:sweeping_edge,4,5",
                                             "4,minecraft:unbreaking,4,5",

                                             "5,minecraft:aqua_affinity,5,1",
                                             "5,minecraft:bane_of_arthropods,5,1",
                                             "5,minecraft:blast_protection,5,1",
                                             "5,minecraft:depth_strider,5,1",
                                             "5,minecraft:feather_falling,5,1",
                                             "5,minecraft:fire_aspect,5,1",
                                             "5,minecraft:fire_protection,5,1",
                                             "5,minecraft:flame,5,1",
                                             "5,minecraft:frost_walker,5,1",
                                             "5,minecraft:infinity,1,1",
                                             "5,minecraft:knockback,5,1",
                                             "5,minecraft:looting,5,1",
                                             "5,minecraft:mending,1,1",
                                             "5,minecraft:power,5,1",
                                             "5,minecraft:projectile_protection,5,1",
                                             "5,minecraft:protection,5,1",
                                             "5,minecraft:punch,5,1",
                                             "5,minecraft:respiration,5,1",
                                             "5,minecraft:sharpness,5,1",
                                             "5,minecraft:smite,5,1",
                                             "5,minecraft:sweeping_edge,5,1",
                                             "5,minecraft:unbreaking,5,1"
                                           };

        @Config.Comment("Chance to get a sponge drop for the fisherman starting at level 4. [Default: 0.1]")
        public double fisherSpongeChance = 0.1;

        @Config.Comment("The minimum level a townhall has to have to allow teleportation to other colonies. [Default: 3]")
        public int minThLevelToTeleport = 3;

        @Config.Comment("Seconds between permission messages. [Default: 30]")
        public int secondsBetweenPermissionMessages = 30;

        @Config.Comment("Suggest build tool usage when trying to place a building without build tool. [Default: true]")
        public boolean suggestBuildToolPlacement = true;

        @Config.Comment("Food consumption modifier (Min: 1.0). [Default: 1.0]")
        public double   foodModifier = 1;

        @Config.Comment("Disable citizen voices. [Default: false]")
        public boolean disableCitizenVoices = false;

        @Config.Comment("Experience multiplier of the enchanter (how much more experience does he get from a citizen than he drains) [Default: 2.0]")
        public double enchanterExperienceMultiplier = 2;
    }

    public static class Compatibility
    {
        @Config.Comment("Harvest trunk-size for dynamic trees:1-8. [Default: 5]")
        public int dynamicTreeHarvestSize = 5;
    }

    public static class Pathfinding
    {
        @Config.Comment("Draw pathfinding paths (might be laggy). [Default: false]")
        public boolean pathfindingDebugDraw = false;

        @Config.Comment("Verbosity of pathfinding. [Default: 0]")
        public int pathfindingDebugVerbosity = 0;

        @Config.Comment("Amount of additional threads to be used for pathfinding. [Default: 2]")
        public int pathfindingMaxThreadCount = 2;

        @Config.Comment("Max amount of Nodes(positions) to map during pathfinding. Lowering increases performance, but might lead to pathing glitches. [Default: 5000]")
        public int pathfindingMaxNodes = 5000;
    }

    public static class RequestSystem
    {
        @Config.Comment("Should the request system print out debug information? Useful in case of malfunctioning of set system. [Default: false]")
        public boolean enableDebugLogging = false;

        @Config.Comment("The maximal amount of tries that the request system will perform for retryable requests. Higher increases server load. [Default: 3]")
        public int maximalRetries = 3;

        @Config.Comment("The amount of ticks between retries of the request system for retryable requests. Lower increases server load. [Default: 1200]")
        public int delayBetweenRetries = 1200;

        @Config.Comment("The maximal amount of buildings the Delivery Man should try to gather before attempting a drop off at the warehouse. [Default: 6]")
        public int maximalBuildingsToGather = 6;

        @Config.Comment("The minimal amount of buildings the Delivery Man should try to gather before attempting a drop off at the warehouse. [Default: 3]")
        public int minimalBuildingsToGather = 3;

        @Config.Comment("Should the request system creatively resolve (if possible) when the player is required to resolve a request. [Default: false]")
        public boolean creativeResolve = false;

        @Config.Comment("Should the player be allowed to use the '/mc colony rs reset' command? [Default: false]")
        public boolean canPlayerUseResetCommand = false;
    }
}
