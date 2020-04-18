package com.minecolonies.api.colony;

import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenHappinessHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
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
     * @param buf
     *            Byte buffer to deserialize.
     */
    void deserialize(@NotNull PacketBuffer buf);

    /**
     * @return returns the current modifier related to food.
     */
    double getFoodModifier();

    /**
     * @return returns the current modifier related to health.
     */
    double getHealthmodifier();

    /**
     * @return current health.
     */
    double getHealth();

    /**
     * @return max health.
     */
    double getMaxHealth();

    /**
     * @return returns the current modifier related to damage.
     */
    double getDamageModifier();

    /**
     * @return returns the current modifier related to house.
     */
    double getHouseModifier();

    /**
     * @return returns the current modifier related to job.
     */
    double getJobModifier();

    /**
     * @return returns the current modifier related to fields.
     */
    double getFieldsModifier();

    /**
     * @return returns the current modifier related to tools.
     */
    double getToolsModifiers();

    /**
     * Get the list of ordered interactions.
     * @return the list.
     */
    List<IInteractionResponseHandler> getOrderedInteractions();

    /**
     * Get a specific interaction by key.
     * @param component the key.
     * @return the interaction or null.
     */
    @Nullable
    IInteractionResponseHandler getSpecificInteraction(@NotNull ITextComponent component);

    /**
     * Check if the citizen has important interactions.
     * @return true if so.
     */
    boolean hasBlockingInteractions();

    /**
     * Check if the citizen has any interactions.
     * @return true if so.
     */
    boolean hasPendingInteractions();

    /**
     * Get an instance of the skill handler.
     * @return the instance.
     */
    ICitizenSkillHandler getCitizenSkillHandler();

    /**
     * The citizen happiness handler.
     * @return the handler.
     */
    ICitizenHappinessHandler getHappinessHandler();
}
