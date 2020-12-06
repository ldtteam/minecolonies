package com.minecolonies.api.research;

import com.minecolonies.api.MinecoloniesAPIProxy;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * The interface for the object that holds all research globally.
 */
public interface IGlobalResearchTree
{
    /**
     * Get an instance of this Tree.
     *
     * @return the instance.
     */
    static IGlobalResearchTree getInstance()
    {
        return MinecoloniesAPIProxy.getInstance().getGlobalResearchTree();
    }

    /**
     * Get a research by id.
     *
     * @param id     the id of the research.
     * @param branch the branch of the research.
     * @return the IResearch object.
     */
    IGlobalResearch getResearch(final String branch, final String id);

    /**
     * Get a research's resource location.
     *
     * @param id     the id of the research.
     * @return the IResearch object.
     */
    ResourceLocation getResearchResourceLocation(final String branch, final String id);

    /**
     * Check if a research exists, by id.
     *
     * @param id     the id of the research.
     * @param branch the branch of the research.
     * @return true if the research exists, false if it does not.
     */
    boolean hasResearch(final String branch, final String id);

    /**
     * Add a research to the tree.
     *
     * @param research the research to add.
     * @param branch   the branch of the research.
     * @param isDynamic  true if reloaded with world events (ie datapacks, onWorldLoad), false if assigned statically once.
     */
    void addResearch(final String branch, final IGlobalResearch research, Boolean isDynamic);

    /**
     * Get the list of all branches.
     *
     * @return the list of branches.
     */
    List<String> getBranches();

    /**
     * Resets all dynamically assigned research.  Run on world unload.
     */
    void reset();

    /**
     * Get the primary research of a certain branch.
     *
     * @param branch the branch it belongs to.
     * @return the list of research without parent.
     */
    List<String> getPrimaryResearch(final String branch);

    /**
     * Checks if a specific unlockBuildingEffect has been registered, whether or not it is unlocked.
     * @param id   the effect's identifier.
     * @return true if present
     */
    boolean hasUnlockBuildingEffect(String id);

    /**
     * Checks if a specific unlockAbilityEffect has been registered, whether or not it is unlocked.
     * @param id   the effect's identifier.
     * @return true if present
     */
    boolean hasUnlockAbilityEffect(String id);

    /**
     * Write the research tree to NBT.
     *
     * @param compound the compound.
     */
    void writeToNBT(final CompoundNBT compound);

    /**
     * Read the research tree from NBT.
     *
     * @param compound the compound to read it from. +
     */
    void readFromNBT(final CompoundNBT compound);
}
