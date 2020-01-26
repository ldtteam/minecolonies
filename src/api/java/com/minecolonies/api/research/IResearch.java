package com.minecolonies.api.research;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface defining how a research is.
 */
public interface IResearch
{
    /**
     * Check if this research can be executed at this moment.
     * @param uni_level the level of the university.
     * @return true if so.
     */
    boolean canResearch(int uni_level);

    /**
     * Check if this research can be displayed in the GUI.
     * @param uni_level the level of the university.
     * @return true if so.
     */
    boolean canDisplay(int uni_level);

    /**
     * Load the cost for the research from the configuration file.
     */
    void loadCostFromConfig();

    /**
     * Check whether all resources are available to execute the research.
     * @param inventory the inventory to check in.
     * @return true if so
     */
    boolean hasEnoughResources(final IItemHandler inventory);

    /**
     * Start the research.
     * @param player the player starting it.
     */
    void startResearch(@NotNull PlayerEntity player);

    /**
     * Tick the research to execute it.
     * @param effects the research effects class which holds the information of the colony.
     * @param tree the research tree which holds the currently explored tree.
     */
    void research(ResearchEffects effects, ResearchTree tree);

    /**
     * Getter for the progress of the research.
     * @return the progress an int between 0-100.
     */
    int getProgress();

    /**
     * Human readable description of research.
     * @return the description.
     */
    String getDesc();

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
     * Get the id of the parent IResearch.
     * @return the string id.
     */
    String getParent();

    /**
     * Get the string name of the branch.
     * @return the branch name.
     */
    String getBranch();

    /**
     * Get the depth in the research tree.
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
     * Obtain a copy of the research.
     * @return the copy of it.
     */
    IResearch copy();

    /**
     * Check if this research is an only child research.
     * This means, after being researched no other childs can e researched.
     * @return true if so.
     */
    boolean isOnlyChild();

    /**
     * Set if a research should only allow one child.
     * @param onlyChild the param to set.
     */
    void setOnlyChild(boolean onlyChild);

    /**
     * Check if this research has other childs.
     * @return true if so.
     */
    boolean hasResearchedChild();

    /**
     * Add a child to a research.
     * @param child the child to add.
     */
    void addChild(IResearch child);

    /**
     * Get the list of childs of the research.
     */
    List<String> getChilds();

    /**
     * Set the parent of a research.
     * @param id the id of the parent.
     */
    void setParent(String id);


    /**
     * Set the research requirement.
     * @param requirement the requirement.
     */
    void setRequirement(final IResearchRequirement requirement);
    /**
     * Getter for the research requirement.
     * @return the requirement.
     */
    IResearchRequirement getResearchRequirement();

    /**
     * Get the effect of the research.
     * @return the effect.
     */
    IResearchEffect getEffect();
}
