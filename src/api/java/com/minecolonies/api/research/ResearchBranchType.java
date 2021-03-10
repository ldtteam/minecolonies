package com.minecolonies.api.research;

/**
 * Different Research Branch types, along with descriptions of their behaviors.  Primarily related to display rendering internal to Minecolonies,
 * or to implementations for AbstractResearchProvider.
 */
public enum ResearchBranchType
{
    /**
     * Default branch using Tree-based view.  Enforces University Level, limits to one level-six research, shows research level headers, etc.
     * All branches without an explicit type, or with an invalid explicit type, will act as DEFAULT.
     */
    DEFAULT("default"),
    /**
     * Achievement-style branch using Tree-based view.  Does not enforce university level, allows multiple researches above the university's max level,
     * does not show research level requirement in headers, does not show research level time headers if branch-time is under 0.01.
     * This is not a replacement for vanilla advancements: use only where University behaviors are important (eg: time, item cost, parent requirement enforcement),
     * where requirement complexity is too broad to fit the Advancement system, where exclusive branches or colony-wide announcements are required,
     * or where requirement or reward is tied to a colony-wide behavior instead of a player-specific one.
     */
    ACHIEVEMENT("achievement");

    /**
     * The string tag representation of the Branch-Type.  Used both in logic, and in datapack JSON.
     */
    public final String tag;

    private ResearchBranchType(String tag) {
        this.tag = tag;
    }

    public static ResearchBranchType valueOfTag(String tag)
    {
        for(ResearchBranchType type : values())
        {
            if(type.tag.equals(tag))
            {
                return type;
            }
        }
        return DEFAULT;
    }
}
