package com.minecolonies.api.research;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface defining how a research is.
 */
public interface IGlobalResearch
{
    /**
     * Check if this research can be executed at this moment.
     * @param uni_level the level of the university.
     * @return true if so.
     */
    boolean canResearch(int uni_level, @NotNull final ILocalResearchTree localTree);

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
     * Get the cost list from the research.
     * @return the list.
     */
    List<ItemStorage> getCostList();

    /**
     * Start the research.
     * @param player the player starting it.
     * @param localResearchTree  the local research tree to store in the colony.
     */
    void startResearch(@NotNull final PlayerEntity player, @NotNull final ILocalResearchTree localResearchTree);

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
     * Check if this research is an only child research.
     * This means, after researching one child no other childs can be researched.
     * @return true if so.
     */
    boolean hasOnlyChild();

    /**
     * Set if a research should only allow one child.
     * @param onlyChild the param to set.
     */
    void setOnlyChild(boolean onlyChild);

    /**
     * Check if this research has other children and if one of these children has been research already.
     * @param localTree the local tree of the colony.
     * @return true if so.
     */
    boolean hasResearchedChild(@NotNull final ILocalResearchTree localTree);

    /**
     * Add a child to a research.
     * @param child the child to add.
     */
    void addChild(IGlobalResearch child);

    /**
     * Get the list of children of the research.
     */
    ImmutableList<String> getChilds();

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
