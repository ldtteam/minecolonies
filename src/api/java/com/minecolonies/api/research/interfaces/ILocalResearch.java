package com.minecolonies.api.research.interfaces;

import com.minecolonies.api.research.LocalResearchTree;
import com.minecolonies.api.research.effects.ResearchEffects;
import com.minecolonies.api.research.util.ResearchState;

/**
 * Interface defining how a local research at a colony is.
 */
public interface ILocalResearch
{
    /**
     * Getter for the progress of the research.
     * @return the progress an int between 0-100.
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
     */
    void research(ResearchEffects effects, LocalResearchTree tree);
}
