package com.minecolonies.api.research;

import com.minecolonies.api.MinecoloniesAPIProxy;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.research.effects.IResearchEffect;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundNBT;

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
    IGlobalResearch getResearch(final ResourceLocation branch, final ResourceLocation id);

    /**
     * Get an effect id for a particular research
     * @param id    the id of the research.
     * @return the effect id
     */
    List<IResearchEffect<?>> getEffectsForResearch(final @NotNull ResourceLocation id);

    /**
     * Check if a research exists, by id.
     *
     * @param id     the id of the research.
     * @param branch the branch of the research.
     * @return true if the research exists, false if it does not.
     */
    boolean hasResearch(final ResourceLocation branch, final ResourceLocation id);

    /**
     * Check if a research exists, by id.
     *
     * @param id     the id of the research.
     * @return true if the research exists, false if it does not.
     */
    boolean hasResearch(final ResourceLocation id);

    /**
     * Add a research to the tree.
     *
     * @param research the research to add.
     * @param branch   the branch of the research.
     * @param isDynamic  true if reloaded with world events (ie data packs, onWorldLoad), false if assigned statically once.
     */
    void addResearch(final ResourceLocation branch, final IGlobalResearch research, final boolean isDynamic);

    /**
     * Get the list of all branches.
     *
     * @return the list of branches.
     */
    List<ResourceLocation> getBranches();

    /**
     * Resets all dynamically assigned research.
     */
    void reset();

    /**
     * Set name on an individual branch.
     * @param branchId      Machine identifier of the branch
     * @param branchName    Human-readable or translation key name for the branch.
     */
    void setBranchName(final ResourceLocation branchId, final TranslationTextComponent branchName);

    /**
     * Gets the name on an individual branch.
     * @param branchId      Machine identifier of the branch
     * @return    Human-readable or translation key name for the branch.
     */
    TranslationTextComponent getBranchName(final ResourceLocation branchId);

    /**
     * Set base time on an individual branch.
     * @param branchId      Machine identifier of the branch
     * @param baseTime      Base duration of the research.
     */
    void setBranchTime(final ResourceLocation branchId, final double baseTime);

    /**
     * Set base time on an individual branch.
     * @param branchId      Machine identifier of the branch
     * @return              Base duration of the research.
     */
    double getBranchTime(final ResourceLocation branchId);

    /**
     * Get the primary research of a certain branch.
     *
     * @param branch the branch it belongs to.
     * @return the list of research without parent.
     */
    List<ResourceLocation> getPrimaryResearch(final ResourceLocation branch);

    /**
     * Get the list of researches that are intended to start automatically
     * once their requirements are met.
     * @return the list of research.
     */
    HashSet<IGlobalResearch> getAutostartResearches();

    /**
     * Validates and gets the list of research reset costs, if any are set, from their configuration values.
     * @return the list of items in ItemStorage format.
     */
    List<ItemStorage> getResearchResetCosts();

    /**
     * Checks if a specific research effect has been registered, whether or not it is unlocked.
     * @param id   the effect's identifier.
     * @return true if present, false if not registered.
     */
    boolean hasResearchEffect(final ResourceLocation id);

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
     *                      all players should be updated on a data pack reload.
     */
    void sendGlobalResearchTreePackets(final ServerPlayerEntity player);
}
