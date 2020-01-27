package com.minecolonies.api.research;

import static com.minecolonies.api.research.ResearchConstants.BASE_RESEARCH_TIME;

/**
 * The implementation of the ILocalResearch interface which represents one type of research, stored in each colony.
 */
public class LocalResearch implements ILocalResearch
{
    /**
     * Depth of research.
     */
    private final int depth;

    /**
     * The current research state.
     */
    private ResearchState state;

    /**
     * The string id of the research.
     */
    private final String id;

    /**
     * The research branch.
     */
    private final String branch;

    /**
     * The progress of the research.
     */
    private int progress;

    /**
     * Create the new research.
     * @param id it's id.
     * @param depth the depth in the tree.
     * @param branch the branch it is on.
     */
    public LocalResearch(final String id, final String branch, final int depth)
    {
        this.id = id;
        this.depth = depth;
        this.branch = branch;
    }

    @Override
    public void research(final ResearchEffects effects, final ResearchTree tree)
    {
        if (state == ResearchState.IN_PROGRESS)
        {
            progress++;
            if (progress >= BASE_RESEARCH_TIME * depth)
            {
                state = ResearchState.FINISHED;
                effects.applyEffect(GlobalResearchTree.researchTree.getResearch(this.branch, this.getId()).getEffect());
            }
        }
    }

    @Override
    public int getProgress()
    {
        return (BASE_RESEARCH_TIME * depth)/progress;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public ResearchState getState()
    {
        return this.state;
    }

    @Override
    public String getBranch()
    {
        return this.branch;
    }

    @Override
    public void setState(final ResearchState value)
    {
        this.state = value;
    }

    @Override
    public void setProgress(final int progress)
    {
        this.progress = progress;
    }
}
