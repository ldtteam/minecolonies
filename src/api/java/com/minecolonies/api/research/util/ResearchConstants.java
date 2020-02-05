package com.minecolonies.api.research.util;

/**
 * Class for research constants.
 */
public final class ResearchConstants
{
    /**
     * The research tree tag.
     */
    public static final String TAG_RESEARCH_TREE = "researchTree";

    /**
     * Base research time, default to 3h playtime.
     */
    public static final int BASE_RESEARCH_TIME = 60 * 60 / 25;

    /**
     * Constants to write the research to NBT.
     */
    public static final String TAG_PARENT     = "parent";
    public static final String TAG_STATE      = "state";
    public static final String TAG_ID         = "id";
    public static final String TAG_BRANCH     = "branch";
    public static final String TAG_DESC       = "desc";
    public static final String TAG_EFFECT     = "effect";
    public static final String TAG_DEPTH      = "depth";
    public static final String TAG_PROGRESS   = "progress";
    public static final String TAG_CHILDS     = "hasResearchedChild";
    public static final String TAG_ONLY_CHILD = "onlyChild";
    public static final String TAG_CHILD     = "child";

    /**
     * Research constants for window.
     */
    public static final String DRAG_VIEW_ID = "dragView";
    public static final int GRADIENT_WIDTH  = 175;
    public static final int X_SPACING       = 40;
    public static final int GRADIENT_HEIGHT = 50;
    public static final int Y_SPACING   = 20;
    public static final int COST_OFFSET          = 20;
    public static final int TIMELABEL_Y_POSITION = 10;
    public static final int MAX_DEPTH        = 6;
    public static final int INITIAL_X_OFFSET = 10;
    public static final int NAME_OFFSET      = 50;
    public static final int INITIAL_Y_OFFSET = 10;
    public static final int TEXT_X_OFFSET  = 5;
    public static final int XPBAR_Y_OFFSET = 30;
    public static final int XPBAR_LENGTH  = 90;
    public static final int TEXT_Y_OFFSET     = 10;
    public static final int DEFAULT_COST_SIZE = 16;
    public static final int LOCK_WIDTH        = 15;
    public static final int LOCK_HEIGHT = 17;
    public static final int OR_X_OFFSET = 14;
    public static final int OR_Y_OFFSET = 10;

    /**
     * Private constructor to hide implicit public one.
     */
    private ResearchConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
