package com.minecolonies.api.research;

import com.minecolonies.api.MinecoloniesAPIProxy;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
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
     * Get an effect id for a particular research
     * @param id    the id of the research.
     * @return the effect id
     */
    List<IResearchEffect<?>> getEffectsForResearch(final @NotNull String id);

   /** Get a research's resource location.
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
     * Check if a research exists, by id.
     *
     * @param id     the id of the research.
     * @return true if the research exists, false if it does not.
     */
    boolean hasResearch(final String id);

    /**
     * Add a research to the tree.
     *
     * @param research the research to add.
     * @param branch   the branch of the research.
     * @param isDynamic  true if reloaded with world events (ie datapacks, onWorldLoad), false if assigned statically once.
     */
    void addResearch(final String branch, final IGlobalResearch research, final boolean isDynamic);

    /**
     * Get the list of all branches.
     *
     * @return the list of branches.
     */
    List<String> getBranches();

    /**
     * Resets all dynamically assigned research.
     */
    void reset();

    /**
     * Set name on an individual branch.
     * @param branchId      Machine identifier of the branch
     * @param branchName    Human-readable or translation key name for the branch.
     */
    void setBranchName(final String branchId, final String branchName);

    /**
     * Gets the name on an individual branch.
     * @param branchId      Machine identifier of the branch
     * @return    Human-readable or translation key name for the branch.
     */
    TranslationTextComponent getBranchName(final String branchId);

    /**
     * Set base time on an individual branch.
     * @param branchId      Machine identifier of the branch
     * @param baseTime      Base duration of the research.
     */
    void setBranchTime(final String branchId, final double baseTime);

    /**
     * Set base time on an individual branch.
     * @param branchId      Machine identifier of the branch
     * @return              Base duration of the research.
     */
    double getBranchTime(final String branchId);

    /**
     * Get the primary research of a certain branch.
     *
     * @param branch the branch it belongs to.
     * @return the list of research without parent.
     */
    List<String> getPrimaryResearch(final String branch);

    /**
     * Get the list of researches that are intended to start automatically
     * once their requirements are met.
     * @return the list of research.
     */
    HashSet<IGlobalResearch> getAutostartResearches();

    /**
     * Validates and gets the list of research reset costs, if any are set, from their configuration values.
     * @return the list of items in namespace:item:integercount format
     */
    List<String> getResearchResetCosts();

    /**
     * Checks if a specific research effect has been registered, whether or not it is unlocked.
     * @param id   the effect's identifier.
     * @return true if present, false if not registered.
     */
    boolean hasResearchEffect(final String id);

    /**
     * Checks if the research requirements are completed, for a given colony.
     * @param requirements   the research requirements.
     * @param colony         the colony to test against.
     * @return               true if complete.
     */
    boolean isResearchRequirementsFulfilled(final List<IResearchRequirement> requirements, final IColony colony);

    /**
     * Write the research tree to NBT.
     *
     * @param compound the compound.
     */
    void writeToNBT(final CompoundNBT compound);

    /**
     * Read the research tree from NBT.
     *
     * @param compound the compound to read it from.
     */
    void readFromNBT(final CompoundNBT compound);

    /**
     * Handle messages in the client from the server describing the Global Research Tree.
     * Only used for remote clients.
     * @param buf       the buffer of received network data.
     */
    IMessage handleGlobalResearchTreeMessage(final PacketBuffer buf);

    /**
     * Sends messages to the client from the server describing the Global Research Tree.
     * Only used for dedicated servers.
     * @param player        the player to send the message
     *                      all players should be updated on a datapack reload.
     */
    void sendGlobalResearchTreePackets(final ServerPlayerEntity player);
}
