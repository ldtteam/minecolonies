package com.minecolonies.api.research;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.effects.IResearchEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

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
     * @return the IGlobalResearch object.
     */
    @Nullable
    IGlobalResearch getResearch(final ResourceLocation branch, final ResourceLocation id);

    /**
     * Get the first research with a given id, on any branch.
     * Avoid using when branch is available.
     * @param id       the id of the research.
     * @return the first matching IGlobalResearch object.
     */
    @Nullable
    IGlobalResearch getResearch(final ResourceLocation id);

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
     * Add data for a research branch, used to contain translation texts, research speed, and other properties.
     * @param branchId    the branch Id.
     * @param branchData  the data.
     */
    void addBranchData(final ResourceLocation branchId, final IGlobalResearchBranch branchData);

    /**
     * Get the list of all branch ids.
     *
     * @return the list of branches.
     */
    List<ResourceLocation> getBranches();

    /**
     * Get the specific GlobalResearchBranch data for a specific branch id.
     *
     * @param branchId  the branch of the research
     */
    IGlobalResearchBranch getBranchData(final ResourceLocation branchId);

    /**
     * Resets all dynamically assigned research.
     */
    void reset();

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
    Set<IGlobalResearch> getAutostartResearches();

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
     * Gets all research for a given research effect id, if any are present.
     * @param id   the effect's identifier.
     * @return The set of researches for the effect, or null if no research has this effect.
     */
    @Nullable
    Set<IGlobalResearch> getResearchForEffect(final ResourceLocation id);

    /**
     * Checks if the research requirements are completed, for a given colony.
     * @param requirements   the research requirements.
     * @param colony         the colony to test against.
     * @return               true if complete.
     */
    boolean isResearchRequirementsFulfilled(final List<IResearchRequirement> requirements, final IColony colony);

    /**
     * Handle messages in the client from the server describing the Global Research Tree.
     * Only used for remote clients.
     * @param buf       the buffer of received network data.
     */
    void handleGlobalResearchTreeMessage(final RegistryFriendlyByteBuf buf);

    /**
     * Sends messages to the client from the server describing the Global Research Tree.
     * Only used for dedicated servers.
     * @param player        the player to send the message
     *                      all players should be updated on a data pack reload.
     */
    void sendGlobalResearchTreePackets(final ServerPlayer player);
}
