package com.minecolonies.api.research.util;

/**
 * Class for research constants.
 */
public final class ResearchConstants
{
    /**
     * Tag for the unlock research effect.
     */
    public static final String TAG_UNLOCK = "unlock";

    /**
     * Tag for the modifier research effect.
     */
    public static final String TAG_MODIFIER = "modifier";

    /**
     * The research tree tag.
     */
    public static final String TAG_RESEARCH_TREE = "researchTree";

    /**
     * The research tree tag.
     */
    public static final String TAG_RESEARCH_EFFECTS = "researchEffects";

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
    public static final String TAG_COST       = "cost";
    public static final String TAG_CHILDS     = "hasResearchedChild";
    public static final String TAG_ONLY_CHILD = "onlyChild";
    public static final String TAG_CHILD     = "child";

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
