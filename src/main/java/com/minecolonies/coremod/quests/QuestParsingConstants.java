package com.minecolonies.coremod.quests;

/**
 * Constants used for json parsing
 */
public class QuestParsingConstants
{
    public static final String QUEST_EFFECTS         = "side-effects";
    public static final String QUEST_REWARDS         = "rewards";
    public static final String QUEST_TRIGGERS        = "triggers";
    public static final String QUEST_OBJECTIVES      = "objectives";
    public static final String TRIGGER_ORDER         = "triggerOrder";
    public static final String MAX_OCC               = "max-occurrences";
    public static final String TYPE                  = "type";
    public static final String TIMEOUT               = "timeout";
    public static final String NAME                  = "name";

    public static final String ID                    = "id";
    public static final String QUEST_PARENTS         = "parents";

    /**
     * Logical symbols for conditions
     */
    public final static String OR          = "||";
    public final static String AND         = "&&";
    public final static String NOT         = "!";
    public final static String BRACE_OPEN  = "(";
    public final static String BRACE_CLOSE = ")";
}
