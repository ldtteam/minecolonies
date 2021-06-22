package com.minecolonies.api.util.constant;

/**
 * Some constants needed to store things to NBT.
 */
public final class NbtTagConstants
{
    public static final String TAG_ID                 = "id";
    public static final String TAG_NAME               = "name";
    public static final String TAG_SUFFIX             = "suffix";
    public static final String TAG_DIMENSION          = "dimension";
    public static final String TAG_CENTER             = "center";
    public static final String TAG_BUILDINGS          = "buildings";
    public static final String TAG_BUILDING           = "building";
    public static final String TAG_BUILDINGS_CLAIM    = "buildingsClaim";
    public static final String TAG_BUILDINGS_UNCLAIM  = "buildingUnclaim";
    public static final String TAG_CITIZENS           = "citizens";
    public static final String TAG_VISITORS           = "visitors";
    public static final String TAG_WORK               = "work";
    public static final String TAG_MANUAL_HIRING      = "manualHiring";
    public static final String TAG_MANUAL_HOUSING     = "manualHousing";
    public static final String TAG_MOVE_IN            = "moveIn";
    public static final String TAG_REQUESTMANAGER     = "requestManager";
    public static final String TAG_WAYPOINT           = "waypoints";
    public static final String TAG_FREE_BLOCKS        = "freeBlocks";
    public static final String TAG_FREE_POSITIONS     = "freePositions";
    public static final String TAG_GRAVE              = "graves";
    public static final String TAG_ABANDONED          = "abandoned";
    public static final String TAG_PRIO               = "prio";
    public static final String TAG_LAST_ONLINE        = "lastOnlineTime";

    /**
     * @deprecated Superseeded by request-based pickup system.
     */
    @Deprecated
    public static final String TAG_PRIO_STATE             = "prioState";
    public static final String TAG_COLONIES               = "colonies";
    public static final String TAG_COLONY_MANAGER         = "colonymanager";
    public static final String TAG_UUID                   = "uuid";
    public static final String TAG_NEW_FIELDS             = "newFields";
    public static final String TAG_COMPATABILITY_MANAGER  = "compatabilityManager";
    public static final String TAG_SAP_LEAF               = "tagSapLeaves";
    public static final String TAG_BLOCK                  = "block";
    public static final String TAG_POS                    = "pos";
    public static final String TAG_PLANTGROUND            = "plantGround";
    public static final String TAG_CURRENT_PHASE          = "currentPhase";
    public static final String TAG_SETTING                = "setting";
    public static final String TAG_BOOKCASES              = "bookcase";
    public static final String TAG_BUILDING_MANAGER       = "buildingManager";
    public static final String TAG_CITIZEN_MANAGER        = "citizenManager";
    public static final String TAG_EVENT_DESC_MANAGER     = "event_desc_manager";
    public static final String TAG_GRAVE_MANAGER          = "graveManager";
    public static final String TAG_COLONY_ID              = "colony";
    public static final String TAG_CITIZEN                = "citizen";
    public static final String TAG_HELD_ITEM_SLOT         = "HeldItemSlot";
    public static final String TAG_OFFHAND_HELD_ITEM_SLOT = "OffhandHeldItemSlot";
    public static final String TAG_STATUS                 = "status";
    public static final String TAG_DAY                    = "day";
    public static final String TAG_IS_BUILT               = "isBuilt";
    public static final String TAG_CUSTOM_NAME            = "customName";
    public static final String TAG_OTHER_LEVEL            = "otherLevel";
    public static final String TAG_PASTEABLE              = "isPasteable";
    public static final String TAG_NEED_TO_MOURN          = "needToMourn";
    public static final String TAG_MOURNING               = "mourning";
    public static final String TAG_DECEASED               = "deceased";
    public static final String TAG_PAUSED                 = "paused";
    public static final String TAG_CHILD                  = "child";
    public static final String TAG_CHILD_TIME             = "childTime";
    public static final String TAG_JUST_ATE               = "justAte";
    public static final String TAG_EXPLOSIONS             = "Explosions";
    public static final String TAG_FIREWORKS              = "Fireworks";
    public static final String TAG_COLORS                 = "Colors";
    public static final String TAG_FLICKER                = "Flicker";
    public static final String TAG_TRAIL                  = "Trail";
    public static final String TAG_TYPE                   = "Type";
    public static final String TAG_MERCENARY_TIME         = "mercenaryUseTime";
    public static final String TAG_IDLE                   = "idle";
    public static final String TAG_PURGED_MOBS            = "purgedMobs";
    public static final String TAG_MANUAL_JOB_SELECTION   = "manualMode";
    public static final String TAG_RESERVED               = "reserved";

    public static final String TAG_PARENT_A = "parentA";
    public static final String TAG_PARENT_B = "parentB";
    public static final String TAG_SIBLINGS = "siblings";
    public static final String TAG_CHILDREN = "children";
    public static final String TAG_PARTNER  = "partner";

    /**
     * Event tags
     */
    public static final String TAG_EVENT_ID       = "mc_event_id";
    public static final String TAG_EVENT_STATUS   = "eventStatus";
    public static final String TAG_SPAWN_POS      = "spawnPos";
    public static final String TAG_CAMPFIRE_LIST  = "campfirelist";
    public static final String TAG_EVENT_POS      = "eventPos";
    public static final String TAG_CITIZEN_NAME   = "citizenName";
    public static final String TAG_DEATH_CAUSE    = "deathCause";
    public static final String TAG_BUILDING_NAME  = "buildingName";
    public static final String TAG_BUILDING_LEVEL = "buildingLevel";

    /**
     * Tag used to store the containers to NBT.
     */
    public static final String TAG_CONTAINERS = "Containers";

    /**
     * The tag to store the building type.
     */
    public static final String TAG_BUILDING_TYPE = "type";

    /**
     * The tag to store the building location. Location is unique (within a Colony) and so can double as the Id.
     */
    public static final String TAG_LOCATION = "location";

    /**
     * The tag to store the level of the building.
     */
    public static final String TAG_SCHEMATIC_LEVEL = "level";

    /**
     * The tag to store the rotation of the building.
     */
    public static final String TAG_ROTATION = "rotation";

    /**
     * The tag to store the md5 hash of the schematic.
     */
    public static final String TAG_SCHEMATIC_MD5 = "schematicMD5";

    /**
     * The tag to store the mirror of the building.
     */
    public static final String TAG_MIRROR = "mirror";

    /**
     * The tag to store the style of the building.
     */
    public static final String TAG_STYLE = "style";

    /**
     * Tag to store if raidable to a colony.
     */
    public static final String TAG_RAIDABLE = "raidable";

    /**
     * Tag to store nights since last raid
     */
    public static final String TAG_NIGHTS_SINCE_LAST_RAID = "nightsRaid";

    /**
     * Tag on the ancient tome used to indicate if a raid will happen.
     */
    public static final String TAG_RAID_WILL_HAPPEN = "raidWillHappen";

    /**
     * Tag to store if auto deletable to a colony.
     */
    public static final String TAG_AUTO_DELETE = "autoDelete";

    /**
     * The tag to store the requester Id of the Building.
     */
    public static final String TAG_REQUESTOR_ID = "Requestor";

    public static final String TAG_RS_BUILDING_DATASTORE = "DataStoreToken";

    public static final String TAG_RS_DMANJOB_DATASTORE = "DataStoreToken";

    public static final String TAG_TOKEN = "Token";

    /**
     * Tags used to store the building corners to nbt and retrieve them.
     */
    public static final String TAG_CORNER1 = "corner1";
    public static final String TAG_CORNER2 = "corner2";
    public static final String TAG_CORNER3 = "corner3";
    public static final String TAG_HEIGHT  = "height";

    /**
     * Tag to store if the field has been taken.
     */
    public static final String TAG_TAKEN = "taken";

    /**
     * Tag to store the fields positive length.
     */
    public static final String TAG_FIELD_EAST = "plot_east";

    /**
     * Tag to store the fields positive width.
     */
    public static final String TAG_FIELD_SOUTH = "plot_south";

    /**
     * Tag to store the fields negative length.
     */
    public static final String TAG_FIELD_WEST = "plot_west";

    /**
     * Tag to store the fields negative width.
     */
    public static final String TAG_FIELD_NORTH = "plot_north";

    /**
     * Tag to store the fields stage.
     */
    public static final String TAG_STAGE = "stage";

    /**
     * Tag to store the owner.
     */
    public static final String TAG_OWNER = "owner";

    /**
     * Tag to store the inventory to nbt.
     */
    public static final String TAG_INVENTORY = "inventory";

    /**
     * Tag to store the content to nbt.
     */
    public static final String TAG_CONTENT = "content";

    /**
     * Tag to store an empty stack to nbt.
     */
    public static final String TAG_EMPTY = "empty";

    /**
     * Tag to store the inventory to nbt.
     */
    public static final String TAG_LEVEL_MAP = "levelMap";

    /**
     * Tag used when a Compound wraps a list.
     */
    public static final String TAG_LIST = "List";

    /**
     * Tag for the ongoing list of deliveries.
     */
    public static final String TAG_ONGOING_LIST = "OngoingList";

    public static final String TAG_ASSIGNED_LIST = "AssignedList";

    /**
     * Tag used when we store a Request.
     */
    public static final String TAG_REQUEST = "Request";

    public static final String TAG_RESOLVER = "Resolver";

    public static final String TAG_VALUE = "Value";

    public static final String TAG_OPEN_REQUESTS_BY_TYPE = "OpenRequestByRequestableType";

    public static final String TAG_OPEN_REQUESTS_BY_CITIZEN = "OpenRequestByCitizen";

    public static final String TAG_COMPLETED_REQUESTS_BY_CITIZEN = "CompletedRequestByCitizen";

    public static final String TAG_CITIZEN_BY_OPEN_REQUEST = "CitizenByOpenRequest";

    /**
     * Tag used to store the worker to nbt.
     */
    public static final String TAG_WORKER = "worker";

    /**
     * Tag to store the buildings hiring mode.
     */
    public static final String TAG_HIRING_MODE = "buildingHiringMode";

    /**
     * NBTTag to store the recipes list.
     */
    public static final String TAG_RECIPES = "recipes";

    /**
     * Tag to store the id to NBT.
     */
    public static final String TAG_WORKER_ID = "workerId";

    /**
     * The tag used to store the residents.
     */
    public static final String TAG_RESIDENTS = "residents";

    /**
     * List storing all beds which have been registered to the building.
     */
    public static final String TAG_BEDS = "beds";

    /**
     * Patients tags.
     */
    public static final String TAG_PATIENTS = "patients";

    /**
     * Tag for all chunk storages..
     */
    public static final String TAG_CHUNK_STORAGE = "chunk";

    /**
     * Var for first pos string.
     */
    public static final String TAG_X = "xPos";

    /**
     * Var for second pos string.
     */
    public static final String TAG_Z = "zPos";

    /**
     * Tag for all chunk storages..
     */
    public static final String TAG_ALL_CHUNK_STORAGES = "allchunk";

    /**
     * Tag used to store the relative neighbor pos to NBT.
     */
    public static final String TAG_RELATIVE_NEIGHBOR = "relNeighbor";

    /**
     * Tag used to store the size.
     */
    public static final String TAG_SIZE = "tagSIze";

    /**
     * Tag used to store if the entity is the main.
     */
    public static final String TAG_MAIN = "main";

    /**
     * Tag used to store if the entity is in a Warehouse.
     */
    public static final String TAG_IN_WAREHOUSE = "inWarehouse";

    /**
     * Tag used to store the team color of the colony.
     */
    public static final String TAG_TEAM_COLOR = "teamcolor";

    /**
     * Tag used to store the colony flag
     */
    public static final String TAG_FLAG_PATTERNS = "colonyflag";

    /**
     * Tag used by vanilla to store banner patterns
     */
    public static final String TAG_BANNER_PATTERNS = "Patterns";

    /**
     * Tag used by vanilla to store a single pattern in banner pattern-color pairs
     */
    public static final String TAG_SINGLE_PATTERN = "Pattern";

    /**
     * Tag used by vanilla to store single color in banner pattern-color pairs
     */
    public static final String TAG_PATTERN_COLOR = "Color";

    /**
     * Citizen data Tags.
     */
    public static final String TAG_FEMALE             = "female";
    public static final String TAG_TEXTURE            = "texture";
    public static final String TAG_SKILL              = "skill";
    public static final String TAG_LEVEL              = "level";
    public static final String TAG_EXPERIENCE         = "experience";
    public static final String TAG_NEW_SKILLS         = "newSkills";
    public static final String TAG_SATURATION         = "saturation";
    public static final String TAG_ASLEEP             = "asleep";
    public static final String TAG_CHAT_OPTION        = "chatoption";
    public static final String TAG_CHAT_OPTIONS       = "chatoptions";

    /**
     * Tag of the colony.
     */
    public static final String TAG_BUILDER = "builder";

    /**
     * Progress manager tags.
     */
    public static final String TAG_PROGRESS_MANAGER = "progressManager";
    public static final String TAG_PROGRESS_TYPE    = "progressType";
    public static final String TAG_PROGRESS_LIST    = "progressList";
    public static final String TAG_PRINT_PROGRESS   = "printProgrss";

    /**
     * Raid manager tag
     */
    public static final String TAG_SCHEMATIC_LIST = "ships";

    /**
     * String to store the existing time to NBT.
     */
    public static final String TAG_TIME = "time";

    /**
     * String to store the stuck counter to NBT.
     */
    public static final String TAG_STUCK_COUNTER = "stuck";

    /**
     * String to store the ladder counter to NBT.
     */
    public static final String TAG_LADDER_COUNTER = "ladder";

    /**
     * Research manager compound TAG.
     */
    public static final String TAG_RESEARCH = "research";

    /**
     * Lumberjack/Tree Tags for NBT
     */
    public static final String TAG_IS_SLIME_TREE         = "slimeTree";
    public static final String TAG_DYNAMIC_TREE          = "dynamicTree";
    public static final String TAG_SAPLING               = "treesapling";
    public static final String TAG_LOGS                  = "Logs";
    public static final String TAG_STUMPS                = "Stumps";
    public static final String TAG_TOP_LOG               = "topLog";
    public static final String TAG_NETHER_TREE           = "netherTree";
    public static final String TAG_LEAVES                = "leaves";
    public static final String TAG_NETHER_TREE_LIST      = "netherTrees";

    /**
     * Archery building constants.
     */
    public static final String TAG_ARCHERY_TARGETS = "archeryTargets";
    public static final String TAG_STAND           = "stand";
    public static final String TAG_TARGET          = "target";
    public static final String TAG_ARCHERY_STANDS  = "archeryStands";

    /**
     * Archery building constants.
     */
    public static final String TAG_COMBAT_TARGET  = "combatTarget";
    public static final String TAG_COMBAT_PARTNER = "combatPartner";
    public static final String TAG_PARTNER1       = "combatPartner1";
    public static final String TAG_PARTNER2       = "combatPartner2";

    /**
     * Crusher building constants.
     */
    public static final String TAG_DAILY         = "daily";
    public static final String TAG_CURRENT_DAILY = "currentDaily";
    public static final String TAG_CRUSHER_MODE  = "Crushermode";
    public static final String TAG_CRUSHER_RATIO = "Crusherratio";
    public static final String TAG_JOB_TYPE      = "type";

    /**
     * Crafter job tags.
     */
    public static final String TAG_PROGRESS      = "progress";
    public static final String TAG_MAX_COUNTER   = "maxCounter";
    public static final String TAG_CRAFT_COUNTER = "craftCounter";

    /**
     * Enchanter tags
     */
    public static final String TAG_GATHER_LIST       = "buildingstogather";
    public static final String TAG_QUANTITY          = "quantity";
    public static final String TAG_GATHERED_ALREADY  = "gatheredalready";
    public static final String TAG_BUILDING_TO_DRAIN = "buildingtodrain";
    public static final String TAG_WAITING_TICKS     = "waitingticks";

    /**
     * Researcher tags.
     */
    public static final String TAG_CURR_MANA  = "currentMana";

    /**
     * Beekeeper tags
     */
    public static final String TAG_HIVES              = "hives";
    public static final String TAG_HARVEST_HONEYCOMBS = "harvest_honeycombs";

    /**
     * BannerRallyGuards tags
     */
    public static final String TAG_RALLIED_GUARDTOWERS = "guardtowerpositions";

    // --------------------- Chat handling tags --------------------- //

    /**
     * Inquiry tag to store to NBT.
     */
    public static final String TAG_INQUIRY = "inquiry";

    /**
     * Response tag to store each response option.
     */
    public static final String TAG_RESPONSE = "response";

    /**
     * Responses tag to store the entire list.
     */
    public static final String TAG_RESPONSES = "responses";

    /**
     * The tag for the next inquiry to a response.
     */
    public static final String TAG_NEXT_INQUIRY = "nextinquiry";

    /**
     * The tag to store if this interaction is a primary interaction.
     */
    public static final String TAG_PRIMARY = "primary";

    /**
     * The tag to store the priority of this interaction..
     */
    public static final String TAG_PRIORITY = "priority";

    /**
     * Handler type to identify when loading from nbt.
     */
    public static final String TAG_HANDLER_TYPE = "handlertype";

    /**
     * Tags to store the needed resourced to nbt.
     */
    public static final String TAG_PROGRESS_POS = "newProgressPos";

    /**
     * Tags to store the needed resourced to nbt.
     */
    public static final String TAG_PROGRESS_STAGE = "newProgressStage";

    /**
     * Tags to store the needed resources to nbt.
     */
    public static final String TAG_FLUIDS_REMOVE = "fluidsToRemove";

    /**
     * Tags to store the stages
     */
    public static final String TAG_TOTAL_STAGES = "totalStages";
    public static final String TAG_CURR_STAGE = "currStage";

    /**
     * Tags to store the needed resources to nbt.
     */
    public static final String TAG_FLUIDS_REMOVE_POSITIONS = "positions";

    /**
     * Tags to store the needed resources to nbt.
     */
    public static final String TAG_FLUIDS_REMOVE_Y = "yLevel";

    /**
     * Some job constants.
     */
    public static final String TAG_TREE = "Tree";

    /**
     * Final strings to save and retrieve the current water location and pond list.
     */
    public static final String TAG_WATER         = "Pond";
    public static final String TAG_PONDS         = "newPonds";
    public static final String TAG_WATER_POND    = "waterpond";
    public static final String TAG_PARENT_POND   = "parentpond";
    public static final String TAG_DECONSTRUCTED = "deconstructed";
    public static final String TAG_GUARD_NEARBY  = "guardnearby";
    public static final String TAG_RESPAWN_POS   = "nextrespawnpos";

    /**
     * Tags to store grave information
     */
    public static final String TAG_DECAY_TIMER = "decaytimer";
    public static final String TAG_DECAYED = "decayed";

    /**
     * Tags/JSON names for storing item informations
     */
    public static final String COUNT_PROP = "count";
    public static final String ITEM_PROP = "item";
    public static final String MATCHTYPE_PROP = "matchType";
    public static final String MATCH_NBTIGNORE = "ignore";

    /**
     * Private constructor to hide the implicit one.
     */
    private NbtTagConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
