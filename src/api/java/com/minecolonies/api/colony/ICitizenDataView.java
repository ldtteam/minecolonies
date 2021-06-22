package com.minecolonies.api.colony;

import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.jobs.IJobView;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenHappinessHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import com.minecolonies.api.util.Tuple;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICitizenDataView extends ICitizen
{
    /**
     * Entity Id getter.
     *
     * @return entity id.
     */
    int getEntityId();

    /**
     * Entity job getter.
     *
     * @return the job as a string.
     */
    String getJob();

    /**
     * Get the entities home building.
     *
     * @return the home coordinates.
     */
    @Nullable
    BlockPos getHomeBuilding();

    /**
     * Get the entities work building.
     *
     * @return the work coordinates.
     */
    @Nullable
    BlockPos getWorkBuilding();

    /**
     * DEPRECATED
     *
     * @param bp the position.
     */
    void setWorkBuilding(BlockPos bp);

    /**
     * Get the colony id of the citizen.
     *
     * @return unique id of the colony.
     */
    int getColonyId();

    /**
     * Gets the current Happiness value for the citizen
     *
     * @return citizens current Happiness value
     */
    double getHappiness();

    /**
     * Get the last registered position of the citizen.
     *
     * @return the BlockPos.
     */
    BlockPos getPosition();

    /**
     * Deserialize the attributes and variables from transition.
     *
     * @param buf Byte buffer to deserialize.
     */
    void deserialize(@NotNull PacketBuffer buf);

    /**
     * @return current health.
     */
    double getHealth();

    /**
     * @return max health.
     */
    double getMaxHealth();

    /**
     * Get the list of ordered interactions.
     *
     * @return the list.
     */
    List<IInteractionResponseHandler> getOrderedInteractions();

    /**
     * Get a specific interaction by key.
     *
     * @param component the key.
     * @return the interaction or null.
     */
    @Nullable
    IInteractionResponseHandler getSpecificInteraction(@NotNull ITextComponent component);

    /**
     * Check if the citizen has important interactions.
     *
     * @return true if so.
     */
    boolean hasBlockingInteractions();

    /**
     * Check if the citizen has any interactions.
     *
     * @return true if so.
     */
    boolean hasPendingInteractions();

    /**
     * Get an instance of the skill handler.
     *
     * @return the instance.
     */
    ICitizenSkillHandler getCitizenSkillHandler();

    /**
     * The citizen happiness handler.
     *
     * @return the handler.
     */
    ICitizenHappinessHandler getHappinessHandler();

    /**
     * The texture to render for interactions
     *
     * @return resourcelocation
     */
    ResourceLocation getInteractionIcon();

    /**
     * Get the visible citizen status
     *
     * @return status
     */
    VisibleCitizenStatus getVisibleStatus();

    /**
     * Get the job view that belongs to this citizen (or null).
     * @return the job.
     */
    @Nullable
    IJobView getJobView();

    /**
     * Get the partner of the citizen.
     * @return the partner or null if non existent.
     */
    @Nullable
    Integer getPartner();

    /**
     * Get the list of children of a citizen.
     * @return the citizen ids.
     */
    List<Integer> getChildren();

    /**
     * Get the list of children of a citizen.
     * @return the citizen ids.
     */
    List<Integer> getSiblings();

    /**
     * Get the names of the parents.
     * @return the name.
     */
    Tuple<String, String> getParents();
}
