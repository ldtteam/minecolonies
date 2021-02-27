package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenExperienceHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.LEVELING;
import static com.minecolonies.api.util.constant.Constants.XP_PARTICLE_EXPLOSION_SIZE;

/**
 * Handles all experience related things of the citizen.
 */
public class CitizenExperienceHandler implements ICitizenExperienceHandler
{
    /**
     * The percentage share primary skills get.
     */
    public static final int PRIMARY_DEPENDENCY_SHARE = 10;

    /**
     * The percentage share secondary skills get.
     */
    public static final int SECONDARY_DEPENDENCY_SHARE = 5;

    /**
     * Number of times to attempt XP pickup before forcing it
     */
    public static final int MAX_XP_PICKUP_ATTEMPTS = 5;

    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * Number of times we've moved XP consecutively
     */
    private int counterMovedXp = 0;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenExperienceHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    @Override
    public void updateLevel()
    {
        if (citizen.getCitizenData().getJob() != null)
        {
            citizen.getCitizenData().getJob().onLevelUp();
        }
    }

    @Override
    public void addExperience(final double xp)
    {
        final IBuilding home = citizen.getCitizenColonyHandler().getHomeBuilding();
        final double citizenHutLevel = home == null ? 0 : home.getBuildingLevel();

        final ICitizenData data = citizen.getCitizenData();
        final IBuilding workBuilding = data.getWorkBuilding();
        if (!(workBuilding instanceof AbstractBuildingWorker))
        {
            return;
        }

        final double workBuildingLevel = workBuilding.getBuildingLevel();
        final double buildingXPModifier = 1 + (workBuildingLevel + citizenHutLevel) / 10;
        double localXp = xp * buildingXPModifier;
        final double saturation = citizen.getCitizenData().getSaturation();
        final int intelligenceLevel = data.getCitizenSkillHandler().getLevel(Skill.Intelligence);
        localXp += localXp * (intelligenceLevel / 100.0);

        if (saturation <= 0)
        {
            return;
        }

        localXp *= (1 + citizen.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(LEVELING));

        localXp = citizen.getCitizenItemHandler().applyMending(localXp);

        final Skill primary = ((AbstractBuildingWorker) workBuilding).getPrimarySkill();
        final Skill secondary = ((AbstractBuildingWorker) workBuilding).getSecondarySkill();

        data.getCitizenSkillHandler().addXpToSkill(primary, localXp, data);
        data.getCitizenSkillHandler().addXpToSkill(primary.getComplimentary(), localXp / (100.0 / PRIMARY_DEPENDENCY_SHARE), data);
        data.getCitizenSkillHandler().removeXpFromSkill(primary.getAdverse(), localXp / (100.0 / PRIMARY_DEPENDENCY_SHARE), data);

        data.getCitizenSkillHandler().addXpToSkill(secondary, localXp / 2.0, data);
        data.getCitizenSkillHandler().addXpToSkill(secondary.getComplimentary(), localXp / (100.0 / SECONDARY_DEPENDENCY_SHARE), data);
        data.getCitizenSkillHandler().removeXpFromSkill(secondary.getAdverse(), localXp / (100.0 / SECONDARY_DEPENDENCY_SHARE), data);
    }

    @Override
    public void dropExperience()
    {
        int experience;

        if (!CompatibilityUtils.getWorldFromCitizen(citizen).isRemote && citizen.getRecentlyHit() > 0 && citizen.checkCanDropLoot() && CompatibilityUtils.getWorldFromCitizen(
          citizen).getGameRules().getBoolean(
          GameRules.DO_MOB_LOOT))
        {
            experience = (int) (citizen.getCitizenData().getCitizenSkillHandler().getTotalXP());

            while (experience > 0)
            {
                final int j = ExperienceOrbEntity.getXPSplit(experience);
                experience -= j;
                CompatibilityUtils.getWorldFromCitizen(citizen)
                  .addEntity(new ExperienceOrbEntity(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.getPosX(), citizen.getPosY(), citizen.getPosZ(), j));
            }
        }

        //Spawn particle explosion of xp orbs on death
        for (int i = 0; i < XP_PARTICLE_EXPLOSION_SIZE; ++i)
        {
            final double d2 = citizen.getRandom().nextGaussian() * 0.02D;
            final double d0 = citizen.getRandom().nextGaussian() * 0.02D;
            final double d1 = citizen.getRandom().nextGaussian() * 0.02D;
            CompatibilityUtils.getWorldFromCitizen(citizen).addParticle(ParticleTypes.EXPLOSION,
              citizen.getPosX() + (citizen.getRandom().nextDouble() * citizen.getWidth() * 2.0F) - (double) citizen.getWidth(),
              citizen.getPosY() + (citizen.getRandom().nextDouble() * citizen.getHeight()),
              citizen.getPosZ() + (citizen.getRandom().nextDouble() * citizen.getWidth() * 2.0F) - (double) citizen.getWidth(),
              d2,
              d0,
              d1);
        }
    }

    @Override
    public void gatherXp()
    {
        if (citizen.world.isRemote)
        {
            return;
        }

        final int growSize = counterMovedXp > 0 || citizen.getRandom().nextInt(100) < 20 ? 8 : 2;

        final AxisAlignedBB box = citizen.getBoundingBox().grow(growSize);
        if (!WorldUtil.isAABBLoaded(citizen.world, box))
        {
            return;
        }

        boolean movedXp = false;

        for (@NotNull final ExperienceOrbEntity orb : citizen.world.getLoadedEntitiesWithinAABB(ExperienceOrbEntity.class, box))
        {
            Vector3d vec3d = new Vector3d(citizen.getPosX() - orb.getPosX(), citizen.getPosY() + (double) this.citizen.getEyeHeight() / 2.0D - orb.getPosY(), citizen.getPosZ() - orb.getPosZ());
            double d1 = vec3d.lengthSquared();

            if (d1 < 1.0D)
            {
                addExperience(orb.getXpValue() / 2.5D);
                orb.remove();
                counterMovedXp = 0;
            }
            else if (counterMovedXp > MAX_XP_PICKUP_ATTEMPTS)
            {
                addExperience(orb.getXpValue() / 2.0D);
                orb.remove();
                counterMovedXp = 0;
                return;
            }

            double d2 = 1.0D - Math.sqrt(d1) / 8.0D;
            orb.setMotion(orb.getMotion().add(vec3d.normalize().scale(d2 * d2 * 0.1D)));
            movedXp = true;
            counterMovedXp++;
        }
        if(!movedXp)
        {
            counterMovedXp = 0;
        }
    }
}
