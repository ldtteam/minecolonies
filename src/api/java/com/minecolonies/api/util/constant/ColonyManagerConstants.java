package com.minecolonies.api.util.constant;

import net.minecraft.util.DamageSource;

/**
 * All colony manager related constants.
 */
public class ColonyManagerConstants
{
    /**
     * Distance NBT tag.
     */
    public static final String TAG_DISTANCE = "dist";

    /**
     * Tag storing the amount of colonies to NBT.
     */
    public static final String TAG_NEW_COLONIES = "amountOfColonies";

    /**
     * The file name of the minecolonies path.
     */
    public static final String FILENAME_MINECOLONIES_PATH = "minecolonies";

    /**
     * The file name of the minecolonies path.
     */
    public static final String CHUNK_INFO_PATH = FILENAME_MINECOLONIES_PATH + "/chunkInfo";

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
     * The tag of the colonies.
     */
    public static final String TAG_COLONIES = "colonies";

    /**
     * Compound tag key for the recipe manager.
     */
    public static final String RECIPE_MANAGER_TAG = "recipeManager";

    /**
     * The tag of the pseudo unique identifier
     */
    public static final String TAG_UUID     = "uuid";

    /**
     * Colony filename.
     */
    public static final String FILENAME_COLONY = "colony%d.dat";

    /**
     * Distance in chunks to load immediately after creating the colony.
     */
    public static final int DISTANCE_TO_LOAD_IMMEDIATELY = 5;

    /**
     * The damage source used to kill citizens.
     */
    public static final DamageSource CONSOLE_DAMAGE_SOURCE = new DamageSource("Console");

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
