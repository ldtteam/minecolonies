package com.minecolonies.core.client.gui.questlog;

import java.time.Duration;

/**
 * Constants for the quest log.
 */
public class Constants
{
    /**
     * Highlight manager key for the quest log tracking.
     */
    public static final String HIGHLIGHT_QUEST_LOG_TRACKER_KEY = "questLogTracking";

    /**
     * Highlight manager key for the quest log tracking.
     */
    public static final Duration HIGHLIGHT_QUEST_LOG_TRACKER_DURATION = Duration.ofSeconds(30);

    /**
     * ID of the quests list inside the GUI.
     */
    public static final String LIST_QUESTS = "quests";

    /**
     * ID of the label for the quest name.
     */
    public static final String LABEL_QUEST_NAME = "questName";

    /**
     * ID of the label for the quest giver.
     */
    public static final String LABEL_QUEST_GIVER = "questGiver";

    /**
     * ID of the label for the quest objective.
     */
    public static final String LABEL_QUEST_OBJECTIVE = "questObjective";

    /**
     * ID of the label for the quest completed count.
     */
    public static final String LABEL_COMPLETED_COUNT = "questCompletedCount";

    /**
     * ID of the button for the quest locator.
     */
    public static final String BUTTON_QUEST_LOCATOR = "questLocator";

    private Constants()
    {
    }
}
