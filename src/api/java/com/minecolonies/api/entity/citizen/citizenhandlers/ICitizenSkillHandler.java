package com.minecolonies.api.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.Skill;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

/**
 * The interface for the citizen skill handler.
 */
public interface ICitizenSkillHandler
{
    /**
     * Initiate a citizen skill handler with a level cap.
     * @param levelCap the max level.
     */
    void init(final int levelCap);

    /**
     * Initiate a citizen with parent citizen datas.
     * @param mom the mother data.
     * @param dad the father data.
     * @param rand random var.
     */
    void init(@NotNull final ICitizenData mom, @NotNull final ICitizenData dad, Random rand);

    /**
     * Write the handler to NBT.
     * @return the handler written to NBT.
     */
    @NotNull
    CompoundNBT write();

    /**
     * Init the handler from NBT.
     * @param compoundNBT the input nbt.
     */
    void read(@NotNull final CompoundNBT compoundNBT);

    /**
     * Attempt an intelligence level up with a base chance.
     * @param random the random var.
     * @param customChance the custom chance.
     * @param citizen the citizen that is trying to level up.
     */
    void tryLevelUpIntelligence(@NotNull final Random random, final int customChance, @NotNull final ICitizenData citizen);

    /**
     * Get the level for a certain skill.
     * @param skill the skill.
     * @return the level.
     */
    int getLevel(@NotNull final Skill skill);

    /**
     * Increment the level for a certain skill.
     * @param skill the skill to increment.
     * @param level the quantity.
     */
    void incrementLevel(@NotNull final Skill skill, int level);

    /**
     * Add experience to a skill.
     * @param skill the skill.
     * @param xp the xp to add.
     * @param data the citizen.
     */
    void addXpToSkill(Skill skill, double xp, ICitizenData data);

    /**
     * Remove xp from a skill.
     * @param skill the skill to remove it from.
     * @param xp the qty of xp.
     * @param data the citizen.
     */
    void removeXpFromSkill(@NotNull final Skill skill, final double xp, @NotNull final ICitizenData data);

    /**
     * Level-up actions for the citizen, increases levels and notifies the Citizen's Job
     * @param data the citizen to level up.
     */
    void levelUp(ICitizenData data);

    /**
     * Get the job modifier.
     * @param data the citizen data.
     * @return the job modifier.
     */
    int getJobModifier(@NotNull final ICitizenData data);

    /**
     * Get the job modifier from the building on the client side.
     * @param workBuilding the building instance.
     * @return the modifier.
     */
    int getJobModifier(@NotNull IBuildingView workBuilding);

    /**
     * Get the total xp that the citizen has.
     * @return the total xp.
     */
    double getTotalXP();

    /**
     * Get a copy of the map of skills.
     * @return the skills.
     */
    Map<Skill, Tuple<Integer, Double>> getSkills();
}
