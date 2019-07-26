package com.minecolonies.coremod.entity.citizenhandlers;

public interface ICitizenExperienceHandler
{
    /**
     * Updates the level of the citizen.
     */
    void updateLevel();

    /**
     * Set the skill modifier which defines how fast a citizen levels in a
     * certain skill.
     *
     * @param modifier input modifier.
     */
    void setSkillModifier(int modifier);

    /**
     * Add experience points to citizen.
     * Increases the citizen level if he has sufficient experience.
     * This will reset the experience.
     *
     * @param xp the amount of points added.
     */
    void addExperience(double xp);

    /**
     * Drop some experience share depending on the experience and
     * experienceLevel.
     */
    void dropExperience();

    /**
     * Collect exp orbs around the entity.
     */
    void gatherXp();

    /**
     * Get the level of the citizen.
     * @return the level.
     */
    int getLevel();

    /**
     * Setter for the level.
     * @param level the level.
     */
    void setLevel(int level);
}
