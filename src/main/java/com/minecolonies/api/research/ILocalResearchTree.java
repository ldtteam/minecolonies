package com.minecolonies.api.research;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.research.effects.IResearchEffectManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The class which contains all research.
 */
public interface ILocalResearchTree
{
    /**
     * Get a research by id.
     *
     * @param id     the id of the research.
     * @param branch the branch of the research.
     * @return the IResearch object.
     */
    ILocalResearch getResearch(final ResourceLocation branch, final ResourceLocation id);

    /**
     * Add a research to the tree.
     *
     * @param research the research to add.
     * @param branch   the branch of the research.
     */
    void addResearch(final ResourceLocation branch, final ILocalResearch research);

    /**
     * Check if a branch already researched a level 6 research. This is important since only 1 of these can be researched for each branch.
     *
     * @param branch the branch to check.
     * @return true if so.
     */
    boolean branchFinishedHighestLevel(final ResourceLocation branch);

    /**
     * Get a list of all research in progress.
     *
     * @return the list.
     */
    List<ILocalResearch> getResearchInProgress();

    /**
     * Checks if a given research is complete.
     *
     * @return true if complete or if no such research is loaded, false if not completed.
     */
     boolean hasCompletedResearch(final ResourceLocation researchId);

    /**
     * Finish a research and remove it from the inProgress list.
     *
     * @param id the id of the research to finish.
     */
    void finishResearch(final ResourceLocation id);

    /**
     * Attempt to begin a research.
     * @param player     the player(s) making the request (and to apply costs toward)
     * @param colony     the colony doing the research
     * @param research   the research.
     */
    void attemptBeginResearch(final Player player, final IColony colony, final IGlobalResearch research);

    /**
     * Reset a research, and optionally undo its effects.  If the research is begun but incomplete, cancel it.
     *
     * @param player     the player to notify of research cancellation results.
     * @param colony     the colony to remove effects from, or null if no effect reset is desired.
     * @param research   the local research descriptor.
     */
    void attemptResetResearch(Player player, @Nullable final IColony colony, ILocalResearch research);

    /**
     * Write the research tree to NBT.
     *
     * @param compound the compound.
     */
    void writeToNBT(final CompoundTag compound);

    /**
     * Read the research tree from NBT.
     *
     * @param compound the compound to read it from.
     * @param effects  the effects.
     */
    void readFromNBT(final CompoundTag compound, final IResearchEffectManager effects);

    /**
     * Check if a given research is complete.
     * @param location the unique id.
     * @return true if so.
     */
    boolean isComplete(ResourceLocation location);
}
