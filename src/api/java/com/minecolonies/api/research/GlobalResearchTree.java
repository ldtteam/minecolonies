package com.minecolonies.api.research;

/**
 * The class which loads the global research tree.
 */
public class GlobalResearchTree
{
    /**
     * The map containing all researches by ID.
     */
    public static final ResearchTree researchTree = new ResearchTree();

    static
    {
        fillResearchTree();
    }

    /**
     * Method to fill the research tree with the elements.
     */
    private static void fillResearchTree()
    {
        Research research1 = new Research("testResearch1", "", "combat", "it happens", 0, new ModifierResearchEffect("moreHealth", 1.0));
        Research research2 = new Research("testResearch2", research1.getId(), "combat", "it happens", 1, new ModifierResearchEffect("moreHealth", 2.0));
        Research research3 = new Research("testResearch3", research2.getId(), "combat", "it happens", 2, new ModifierResearchEffect("moreHealth", 3.0));
        Research research4 = new Research("testResearch4", research3.getId(), "combat", "it happens", 3, new ModifierResearchEffect("moreHealth", 4.0));

        Research research22 = new Research("testResearch22", research1.getId(), "combat", "wow", 1, new ModifierResearchEffect("moreHealth", 2.2));

        Research research11 = new Research("testResearchX", "", "combat", "diff", 0, new ModifierResearchEffect("ohhh", 1.0));
        Research research12 = new Research("testResearchY", research11.getId(), "combat", "diff2", 0, new ModifierResearchEffect("ohhhh", 2.0));

        research1.addChild(research2.getId());
        research2.addChild(research3.getId());
        research3.addChild(research4.getId());

        research1.addChild(research22.getId());

        research11.addChild(research12.getId());

        researchTree.addResearch(research1.getBranch(), research1);
        researchTree.addResearch(research2.getBranch(), research2);
        researchTree.addResearch(research3.getBranch(), research3);
        researchTree.addResearch(research4.getBranch(), research4);
        researchTree.addResearch(research22.getBranch(), research22);

        researchTree.addResearch(research11.getBranch(), research11);
        researchTree.addResearch(research12.getBranch(), research12);
    }
}
