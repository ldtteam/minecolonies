package com.minecolonies.api.util.constant;

/**
 * All colony manager related constants.
 */
public final class ColonyManagerConstants
{
    /**
     * Distance NBT tag.
     */
    public static final String TAG_DISTANCE = "dist";

    /**
     * The file name of the minecolonies path.
     */
    public static final String FILENAME_MINECOLONIES_PATH = "minecolonies";

    /**
     * The file name of the minecolonies.
     */
    public static final String FILENAME_MINECOLONIES = "colonies.dat";

    /**
     * The file name pattern of the minecolonies backup.
     */
    public static final String FILENAME_MINECOLONIES_BACKUP = "colonies-%s.zip";

    /**
     * Printed text if world capability couldn't be found.
     */
    public static final String UNABLE_TO_FIND_WORLD_CAP_TEXT = "Unable to find Chunk manager in world capability, please report this to the mod author!";

    /**
     * Compound tag key for the recipe manager.
     */
    public static final String RECIPE_MANAGER_TAG = "recipeManager";

    /**
     * Colony filename.
     */
    public static final String FILENAME_COLONY = "colony%d.dat";

    /**
     * Colony filename deleted.
     */
    public static final String FILENAME_COLONY_DELETED = "colony%d.dat.deleted";

    /**
     * Log message for missing world cap.
     */
    public static final String MISSING_WORLD_CAP_MESSAGE = "Missing world capability with colony manager!";

    /**
     * ID for when no colony exists
     */
    public static final int NO_COLONY_ID = 0;

    /**
     * Private constructor to hide implicit one.
     */
    private ColonyManagerConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
