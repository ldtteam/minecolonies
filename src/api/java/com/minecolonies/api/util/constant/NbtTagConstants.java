package com.minecolonies.api.util.constant;

/**
 * Some constants needed to store things to NBT.
 */
public final class NbtTagConstants
{
    public static final String TAG_ID                     = "id";
    public static final String TAG_NAME                   = "name";
    public static final String TAG_DIMENSION              = "dimension";
    public static final String TAG_CENTER                 = "center";
    public static final String TAG_MAX_CITIZENS           = "maxCitizens";
    public static final String TAG_POTENTIAL_MAX_CITIZENS = "potentialMaxCitizens";
    public static final String TAG_BUILDINGS              = "buildings";
    public static final String TAG_BUILDING               = "building";
    public static final String TAG_BUILDINGS_CLAIM        = "buildingsClaim";
    public static final String TAG_BUILDINGS_UNCLAIM      = "buildingUnclaim";
    public static final String TAG_CITIZENS               = "citizens";
    public static final String TAG_ACHIEVEMENT            = "achievement";
    public static final String TAG_ACHIEVEMENT_LIST       = "achievementlist";
    public static final String TAG_WORK                   = "work";
    public static final String TAG_MANUAL_HIRING          = "manualHiring";
    public static final String TAG_MANUAL_HOUSING         = "manualHousing";
    public static final String TAG_MOVE_IN                = "moveIn";
    public static final String TAG_REQUESTMANAGER         = "requestManager";
    public static final String TAG_WAYPOINT               = "waypoints";
    public static final String TAG_FREE_BLOCKS            = "freeBlocks";
    public static final String TAG_FREE_POSITIONS         = "freePositions";
    public static final String TAG_HAPPINESS_MODIFIER     = "happinessModifier";
    public static final String TAG_ABANDONED              = "abandoned";
    public static final String TAG_BUILDING_PRIO          = "buildingPrio";
    public static final String TAG_PRIO                   = "prio";
    public static final String TAG_PRIO_MODE              = "prioMode";
    public static final String TAG_PRIO_ID                = "prioId";
    public static final String TAG_COLONIES               = "colonies";
    public static final String TAG_UUID                   = "uuid";
    public static final String TAG_STATISTICS             = "statistics";
    public static final String TAG_MINER_STATISTICS       = "minerStatistics";
    public static final String TAG_MINER_ORES             = "ores";
    public static final String TAG_MINER_DIAMONDS         = "diamonds";
    public static final String TAG_FARMER_STATISTICS      = "farmerStatistics";
    public static final String TAG_FARMER_WHEAT           = "wheat";
    public static final String TAG_FARMER_POTATOES        = "potatoes";
    public static final String TAG_FARMER_CARROTS         = "carrots";
    public static final String TAG_GUARD_STATISTICS       = "guardStatistics";
    public static final String TAG_GUARD_MOBS             = "mobs";
    public static final String TAG_BUILDER_STATISTICS     = "builderStatistics";
    public static final String TAG_BUILDER_HUTS           = "huts";
    public static final String TAG_FISHERMAN_STATISTICS   = "fishermanStatistics";
    public static final String TAG_FISHERMAN_FISH         = "fish";
    public static final String TAG_FIELDS                 = "fields";
    public static final String TAG_NEW_FIELDS             = "newFields";
    public static final String TAG_COMPATABILITY_MANAGER  = "compatabilityManager";
    public static final String TAG_SAPLINGS               = "tagSaplings";
    public static final String TAG_SAP_LEAF               = "tagSapLeaves";
    public static final String TAG_ORES                   = "tagOres";
    public static final String TAG_BLOCK                  = "block";
    public static final String TAG_POS                    = "pos";
    public static final String TAG_BOOKCASES              = "bookcase";
    public static final String TAG_BUILDING_MANAGER       = "buildingManager";
    public static final String TAG_CITIZEN_MANAGER        = "citizenManager";
    public static final String TAG_STATS_MANAGER          = "statsManager";
    public static final String TAG_COLONY_ID              = "colony";
    public static final String TAG_CITIZEN                = "citizen";
    public static final String TAG_HELD_ITEM_SLOT         = "HeldItemSlot";
    public static final String TAG_OFFHAND_HELD_ITEM_SLOT = "OffhandHeldItemSlot";
    public static final String TAG_STATUS                 = "status";
    public static final String TAG_LAST_JOB               = "lastJob";
    public static final String TAG_DAY                    = "day";
    public static final String OWNED_CHUNKS_TO_LOAD_TAG   = "ownedChunks";
    public static final String CLOSE_CHUNKS_TO_LOAD_TAG   = "closeChunks";
    public static final String TAG_HAPPINESS_NAME         = "happiness";
    public static final String TAG_BASE                   = "base";
    public static final String TAG_FOOD                   = "foodModifier";
    public static final String TAG_DAMAGE                 = "damageModifier";
    public static final String TAG_HOUSE                = "houseModifier";
    public static final String TAG_NUMBER_OF_DAYS_HOUSE = "numberOfDaysWithoutHouse";
    public static final String TAG_JOB                  = "jobModifier";
    public static final String TAG_NUMBER_OF_DAYS_JOB   = "numberOfDaysWithoutJob";
    public static final String TAG_HAS_NO_FIELDS        = "hasNoFields";
    public static final String TAG_FIELD_DAYS_INACTIVE  = "daysinactive";
    public static final String TAG_FIELD_CAN_FARM       = "canfarm";
    public static final String TAG_NO_TOOLS             = "noTools";
    public static final String TAG_NO_TOOLS_NUMBER_DAYS = "numberOfDaysNoTools";
    public static final String TAG_NO_TOOLS_TOOL_TYPE   = "toolType";
    public static final String TAG_IS_BUILT             = "isBuilt";
    public static final String TAG_CUSTOM_NAME          = "customName";
    public static final String TAG_OTHER_LEVEL          = "otherLevel";
    public static final String TAG_PASTEABLE            = "isPasteable";
    public static final String TAG_STRING_NAME          = "Name";
    public static final String TAG_DISPLAY              = "display";
    public static final String TAG_NEED_TO_MOURN        = "needToMourn";
    public static final String TAG_MOURNING             = "mourning";
    public static final String TAG_PAUSED               = "paused";
    public static final String TAG_CHILD                = "child";
    public static final String TAG_CHILD_TIME                = "childTime";
    public static final String TAG_BOUGHT_CITIZENS      = "bought_citizen";
    public static final String TAG_JUST_ATE             = "justAte";
    public static final String TAG_EXPLOSIONS           = "Explosions";
    public static final String TAG_FIREWORKS            = "Fireworks";
    public static final String TAG_COLORS               = "Colors";
    public static final String TAG_FLICKER              = "Flicker";
    public static final String TAG_TRAIL                = "Trail";
    public static final String TAG_TYPE                 = "Type";
    public static final String TAG_MERCENARY_TIME       = "mercenaryUseTime";

    /**
     * Tag used to store the containers to NBT.
     */
    public static final String TAG_CONTAINERS = "Containers";

    /**
     * The tag to store the building type.
     */
    public static final String TAG_BUILDING_TYPE = "type";

    /**
     * The tag to store the building location.
     * Location is unique (within a Colony) and so can double as the Id.
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

    public static final String TAG_ASSIGNMENTS = "Assignments";

    /**
     * Tags used to store the building corners to nbt and retrieve them.
     */
    public static final String TAG_CORNER1 = "corner1";
    public static final String TAG_CORNER2 = "corner2";
    public static final String TAG_CORNER3 = "corner3";
    public static final String TAG_CORNER4 = "corner4";
    public static final String TAG_HEIGHT  = "height";

    /**
     * Tag to store if the field has been taken.
     */
    public static final String TAG_TAKEN = "taken";

    /**
     * Tag to store the fields positive length.
     */
    public static final String TAG_LENGTH_PLUS = "length+";

    /**
     * Tag to store the fields positive width.
     */
    public static final String TAG_WIDTH_PLUS = "width+";

    /**
     * Tag to store the fields negative length.
     */
    public static final String TAG_LENGTH_MINUS = "length-";

    /**
     * Tag to store the fields negative width.
     */
    public static final String TAG_WIDTH_MINUS = "width-";

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
     * Tag to store the inventory to nbt.
     */
    public static final String TAG_LEVEL_MAP = "levelMap";

    /**
     * Tag to store the inventory to nbt.
     */
    public static final String TAG_EXPERIENCE_MAP = "experienceMap";

    /**
     * Tag used when a Compound wraps a list.
     */
    public static final String TAG_LIST = "List";

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
     * Missing chunks to be loaded.
     */
    public static final String TAG_MISSING_CHUNKS = "missingChunks";

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
     * Var for first pos string.
     */
    public static final String FIRST_POS_STRING = "pos1";

    /**
     * Var for second pos string.
     */
    public static final String SECOND_POS_STRING = "pos2";

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
     * Tag used to store the neighbor pos to NBT.
     */
    public static final String TAG_NEIGHBOR = "neighbor";

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
     * Citizen data Tags.
     */
    public static final String TAG_FEMALE             = "female";
    public static final String TAG_TEXTURE            = "texture";
    public static final String TAG_LEVEL              = "level";
    public static final String TAG_EXPERIENCE         = "experience";
    public static final String TAG_HEALTH             = "health";
    public static final String TAG_MAX_HEALTH         = "maxHealth";
    public static final String TAG_SKILLS             = "skills";
    public static final String TAG_SKILL_STRENGTH     = "strength";
    public static final String TAG_SKILL_STAMINA      = "endurance";
    public static final String TAG_SKILL_SPEED        = "charisma";
    public static final String TAG_SKILL_INTELLIGENCE = "intelligence";
    public static final String TAG_SKILL_DEXTERITY    = "dexterity";
    public static final String TAG_SATURATION         = "saturation";
    public static final String TAG_ASLEEP             = "asleep";

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
     * Raid manager tags
     */
    public static final String TAG_RAID_MANAGER   = "raidManager";
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
     * Lumberjack/Tree Tags for NBT
     */
    public static final String TAG_LUMBERJACK_STATISTICS = "lumberjackStatistics";
    public static final String TAG_LUMBERJACK_TREES      = "trees";
    public static final String TAG_LUMBERJACK_SAPLINGS   = "saplings";
    public static final String TAG_IS_SLIME_TREE         = "slimeTree";
    public static final String TAG_DYNAMIC_TREE          = "dynamicTree";
    public static final String TAG_LOGS                  = "Logs";
    public static final String TAG_STUMPS                = "Stumps";
    public static final String TAG_TOP_LOG               = "topLog";

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
    public static final String TAG_JOB_TYPE      = "type";
    public static final String NBT_SLOT          = "Slot";

    /**
     * Crafter job tags.
     */
    public static final String TAG_PROGRESS    = "progress";
    public static final String TAG_MAX_COUNTER = "maxCounter";
    public static final String TAG_CRAFT_COUNTER  = "craftCounter";

    /**
     * Enchanter tags
     */
    public static final String TAG_GATHER_LIST = "buildingstogather";
    public static final String TAG_QUANTITY      = "quantity";
    public static final String TAG_GATHERED_ALREADY = "gatheredalready";
    public static final String TAG_BUILDING_TO_DRAIN = "buildingtodrain";
    public static final String TAG_WAITING_TICKS = "waitingticks";

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
