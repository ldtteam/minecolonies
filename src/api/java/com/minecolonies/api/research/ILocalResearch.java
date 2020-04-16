package com.minecolonies.api.research;

import com.minecolonies.api.research.effects.IResearchEffectManager;
import com.minecolonies.api.research.util.ResearchState;

/**
 * Interface defining how a local research at a colony is.
 * This represents how a colony stores researches they finished or started.
 */
public interface ILocalResearch
{
    /**
     * Getter for the progress of the research.
     * @return the progress in ticks.
     */
    int getProgress();

    /**
     * Getter of the id of the research.
     * @return the String id.
     */
    String getId();

    /**
     * Get the ResearchState of the research.
     * @return the current state.
     */
    ResearchState getState();

    /**
     * Get the string name of the branch.
     * @return the branch name.
     */
    String getBranch();

    /**
     * Getter for the research depth.
     * @return the depth.
     */
    int getDepth();

    /**
     * Set the current research state.
     * @param value the state to set.
     */
    void setState(ResearchState value);

    /**
     * Set the research progress.
     * @param progress the progress to set.
     */
    void setProgress(int progress);

    /**
     * Tick the research to execute it.
     * @param effects the research effects class which holds the information of the colony.
     * @param tree the research tree which holds the currently explored tree.
     * @return true if effective.
     */
    boolean research(IResearchEffectManager effects, ILocalResearchTree tree);
}
