package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenExperienceHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
        if (workBuilding == null || !workBuilding.hasModule(WorkerBuildingModule.class))
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

        localXp *= (1 + citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(LEVELING));

        final WorkerBuildingModule module = workBuilding.getModuleMatching(WorkerBuildingModule.class, m -> m.getAssignedCitizen().contains(data));
        final Skill primary = module.getPrimarySkill();
        final Skill secondary = module.getSecondarySkill();

        data.getCitizenSkillHandler().addXpToSkill(primary, localXp, data);
        if (primary.getComplimentary() != null)
        {
            data.getCitizenSkillHandler().addXpToSkill(primary.getComplimentary(), localXp / (100.0 / PRIMARY_DEPENDENCY_SHARE), data);
        }

        if (primary.getAdverse() != null)
        {
            data.getCitizenSkillHandler().removeXpFromSkill(primary.getAdverse(), localXp / (100.0 / PRIMARY_DEPENDENCY_SHARE), data);
        }

        data.getCitizenSkillHandler().addXpToSkill(secondary, localXp / 2.0, data);

        if (secondary.getComplimentary() != null)
        {
            data.getCitizenSkillHandler().addXpToSkill(secondary.getComplimentary(), localXp / (100.0 / SECONDARY_DEPENDENCY_SHARE), data);
        }

        if (secondary.getAdverse() != null)
        {
            data.getCitizenSkillHandler().removeXpFromSkill(secondary.getAdverse(), localXp / (100.0 / SECONDARY_DEPENDENCY_SHARE), data);
        }
    }

    @Override
    public void dropExperience()
    {
        int experience;

        if (!CompatibilityUtils.getWorldFromCitizen(citizen).isClientSide && citizen.getRecentlyHit() > 0 && citizen.checkCanDropLoot() && CompatibilityUtils.getWorldFromCitizen(
          citizen).getGameRules().getBoolean(
          GameRules.RULE_DOMOBLOOT))
        {
            experience = (int) (citizen.getCitizenData().getCitizenSkillHandler().getTotalXP());

            while (experience > 0)
            {
                final int j = ExperienceOrb.getExperienceValue(experience);
                experience -= j;
                CompatibilityUtils.getWorldFromCitizen(citizen)
                  .addFreshEntity(new ExperienceOrb(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.getX(), citizen.getY(), citizen.getZ(), j));
            }
        }

        //Spawn particle explosion of xp orbs on death
        for (int i = 0; i < XP_PARTICLE_EXPLOSION_SIZE; ++i)
        {
            final double d2 = citizen.getRandom().nextGaussian() * 0.02D;
            final double d0 = citizen.getRandom().nextGaussian() * 0.02D;
            final double d1 = citizen.getRandom().nextGaussian() * 0.02D;
            CompatibilityUtils.getWorldFromCitizen(citizen).addParticle(ParticleTypes.EXPLOSION,
              citizen.getX() + (citizen.getRandom().nextDouble() * citizen.getBbWidth() * 2.0F) - (double) citizen.getBbWidth(),
              citizen.getY() + (citizen.getRandom().nextDouble() * citizen.getBbHeight()),
              citizen.getZ() + (citizen.getRandom().nextDouble() * citizen.getBbWidth() * 2.0F) - (double) citizen.getBbWidth(),
              d2,
              d0,
              d1);
        }
    }

    @Override
    public void gatherXp()
    {
        if (citizen.level.isClientSide)
        {
            return;
        }

        final int growSize = counterMovedXp > 0 || citizen.getRandom().nextInt(100) < 20 ? 4 : 2;

        final AABB box = citizen.getBoundingBox().inflate(growSize);
        if (!WorldUtil.isAABBLoaded(citizen.level, box))
        {
            return;
        }

        boolean movedXp = false;

        for (@NotNull final ExperienceOrb orb : citizen.level.getEntitiesOfClass(ExperienceOrb.class, box))
        {
            if (orb.tickCount < 5)
            {
                continue;
            }

            Vec3 vec3d = new Vec3(citizen.getX() - orb.getX(), citizen.getY() + (double) this.citizen.getEyeHeight() / 2.0D - orb.getY(), citizen.getZ() - orb.getZ());
            double d1 = vec3d.lengthSqr();

            if (d1 < 1.0D)
            {
                double localXp = orb.getValue();
                localXp = citizen.getCitizenItemHandler().applyMending(localXp);
                addExperience(localXp);
                orb.remove(Entity.RemovalReason.DISCARDED);
                counterMovedXp = 0;
            }
            else if (counterMovedXp > MAX_XP_PICKUP_ATTEMPTS)
            {
                double localXp = orb.getValue();
                localXp = citizen.getCitizenItemHandler().applyMending(localXp);
                addExperience(localXp);
                orb.remove(Entity.RemovalReason.DISCARDED);
                counterMovedXp = 0;
                return;
            }

            double d2 = 1.0D - Math.sqrt(d1) / 8.0D;
            orb.setDeltaMovement(orb.getDeltaMovement().add(vec3d.normalize().scale(d2 * d2 * 0.1D)));
            movedXp = true;
            counterMovedXp++;
        }
        if(!movedXp)
        {
            counterMovedXp = 0;
        }
    }
}
