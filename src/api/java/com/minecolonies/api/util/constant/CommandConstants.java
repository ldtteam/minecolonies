package com.minecolonies.api.util.constant;

import org.jetbrains.annotations.NonNls;

/**
 * Command wide constants.
 */
public final class CommandConstants
{
    /**
     * No Permission messages:
     */
    @NonNls
    public static final String NO_PERMISSION_TO_CLAIM_MESSAGE = "You do not have permission to claim land like that!";
    @NonNls
    public static final String NO_PERMISSION_TO_SCAN_MESSAGE  = "You do not have permission to scan structures!";

    /**
     * Failure messages:
     */
    @NonNls
    public static final String SCAN_FAILURE_MESSAGE          = "Failed to scan structure!";
    @NonNls
    public static final String MISSING_PLAYER_STRUCTURE_SCAN = "Failed to scan structure, missing player to store the file!";

    /**
     * Invalid argument messages:
     */
    @NonNls
    public static final String COLONY_X_NULL             = "Couldn't find colony %d.";
    @NonNls
    public static final String  NO_COLONY_OR_PLAYER       = "Please define a colony or player";
    @NonNls
    public static final String  NO_PLAYER                 = "Can't find player to add";
    @NonNls
    public static final String  HAS_A_COLONY              = "Player %s has a colony already.";
    @NonNls
    public static final String  NO_COLONY_MESSAGE         = "Invalid colony, aborting!";
    @NonNls
    public static final String  SPECIAL_CHARACTERS_ADVICE = "Please stick to the default characters A-Z, a-z and, 0-9";

    /**
     * Success messages:
     */
    @NonNls
    public static final String SUCCESS_MESSAGE_ADD_OFFICER = "Successfully added Player %s to colony %d";
    @NonNls
    public static final String SCAN_SUCCESS_MESSAGE        = "Successfully scan structure!";
    @NonNls
    public static final String SUCCESS_MESSAGE_OWNERCHANGE = "Successfully switched Owner %s to colony %d";
    @NonNls
    public static final String DELETE_COLONY_TASK_SCHEDULED = "Delete colony task scheduled.";
    @NonNls
    public static final String SUCCESFULLY_CLAIMED_CHUNKS = "Succesfully claimed chunks in every direction";
    @NonNls
    public static final String TOO_MANY_CHUNKS = "You are trying to claim an area bigger than twice the colony size! Aborting to avoid lags and crashes.";
    @NonNls
    public static final int CHUNKS_TO_CLAM_THRESHOLD = 5000;
    @NonNls
    public static final String TOO_MANY_CHUNKS_CLAIMED = "You've reached the maximal claimable chunk count, aborting.";

    /**
     * Private constructor to hide the implicit one.
     */
    private CommandConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
