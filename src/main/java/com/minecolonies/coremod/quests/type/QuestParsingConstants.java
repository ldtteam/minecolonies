package com.minecolonies.coremod.quests.type;

/**
 * Constants used for json parsing
 */
public class QuestParsingConstants
{
    public static final String QUEST_CATEGORY        = "category";
    public static final String QUEST_EFFECTS         = "effects";
    public static final String QUEST_REWARDS         = "rewards";
    public static final String QUEST_TRIGGERS        = "triggers";
    public static final String ID                    = "id";
    public static final String QUEST_REPEATING       = "repeatingtimes";
    public static final String QUEST_PRE_QUESTS      = "pre-quests";
    public static final String QUEST_FOLLOWUP_QUESTS = "followup-quests";
    public static final String QUEST_LOCATION        = "location ";

    /**
     * Logical symbols for conditions
     */
    public final static String OR          = "||";
    public final static String AND         = "&&";
    public final static String NOT         = "!";
    public final static String BRACE_OPEN  = "(";
    public final static String BRACE_CLOSE = ")";
}
