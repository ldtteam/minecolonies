package com.minecolonies.api.colony;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.Citizen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ReadOnly Interface used to describe objects that hold data particular to given Citizen
 */
public interface ICitizenData
{
    /**
     * Return the entity instance of the citizen data. Respawn the citizen if
     * needed.
     *
     * @return {@link Citizen} of the citizen data.
     */
    @Nullable
    Citizen getCitizen();

    /**
     * Sets the entity of the citizen data.
     *
     * @param citizen {@link Citizen} instance of the citizen data.
     */
    void setCitizenEntity(Citizen citizen);

    /**
     * Getter for the {@link IColony} that this citizen belongs to.
     *
     * @return The {@link IColony} that this colony belongs to.
     */
    @NotNull
    IColony getColony();

    /**
     * Getter for the ID of the citizen.
     *
     * @return The id of the citizen.
     */
    int getId();

    /**
     * Create a CitizenData given a CitizenEntity.
     *
     * @param entity Entity to initialize from.
     */
    void initializeFromEntity(@NotNull Citizen entity);

    /**
     * Getter for the name of the Citizen.
     *
     * @return The name of the citizen.
     */
    @NotNull
    String getName();

    /**
     * Getter for the sex of the Citizen.
     *
     * @return True when she is female, false when not.
     */
    boolean isFemale();

    /**
     * Getter for the client side texture Id of the citizen.
     *
     * @return The client sided texture of the citizen.
     */
    int getTextureId();

    /**
     * Returns the work building of a citizen.
     *
     * @return home building of a citizen.
     */
    @Nullable
    IBuilding getWorkBuilding();

    /**
     * Returns the job of the citizen.
     *
     * @return Job of the citizen.
     */
    IJob getJob();

    /**
     * Sets the job of this citizen.
     *
     * @param job Job of the citizen.
     */
    void setJob(IJob job);

    /**
     * Method used to get the Level of the citizen.
     *
     * @return The level of the citizen.
     */
    int getLevel();

    /**
     * Method used to get the experience of the citizen.
     *
     * @return The experience of the citizen.
     */
    double getExperience();

    /**
     * Method used to get the strength of the citizen.
     *
     * @return The strength of the citizen.
     */
    int getStrength();

    /**
     * Method used to get the Endurance of the citizen.
     *
     * @return The Endurance of the citizen.
     */
    int getEndurance();

    /**
     * Method used to get the Charisma of the citizen.
     *
     * @return The Charisma of the citizen.
     */
    int getCharisma();

    /**
     * Method used to get the Intelligence of the citizen.
     *
     * @return The Intelligence of the citizen.
     */
    int getIntelligence();

    /**
     * Method used to get the Dexterity of the citizen.
     *
     * @return The Dexterity of the citizen.
     */
    int getDexterity();

    /**
     * Method used to get the saturation of the citizen.
     *
     * @return The saturation of the citizen.
     */
    double getSaturation();
}
