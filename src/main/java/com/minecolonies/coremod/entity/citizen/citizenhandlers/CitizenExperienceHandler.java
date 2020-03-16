package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenExperienceHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.XP_PARTICLE_EXPLOSION_SIZE;

/**
 * Handles all experience related things of the citizen.
 */
public class CitizenExperienceHandler implements ICitizenExperienceHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final EntityCitizen citizen;

    /**
     * Constructor for the experience handler.
     * @param citizen the citizen owning the handler.
     */
    public CitizenExperienceHandler(final EntityCitizen citizen)
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
        final double bonusXp = 1 + (workBuildingLevel * citizenHutLevel) / 10;
        double localXp = xp * bonusXp;
        final double saturation = citizen.getCitizenData().getSaturation();
        final int intelligenceLevel = data.getCitizenSkillHandler().getLevel(Skill.Intelligence);
        localXp += localXp * ( intelligenceLevel / 10.0 );

        if (saturation <= 0)
        {
            return;
        }

        if (saturation < AVERAGE_SATURATION)
        {
            if (saturation < LOW_SATURATION)
            {
                localXp -= localXp * BIG_SATURATION_FACTOR;
            }
            else
            {
                localXp -= localXp * LOW_SATURATION_FACTOR;
            }
        }
        else if (saturation > AVERAGE_SATURATION)
        {
            if (saturation > HIGH_SATURATION)
            {
                localXp += localXp * BIG_SATURATION_FACTOR;
            }
            else
            {
                localXp += localXp * LOW_SATURATION_FACTOR;
            }
        }

        localXp = citizen.getCitizenItemHandler().applyMending(localXp);

        final Skill primary = ((AbstractBuildingWorker) workBuilding).getPrimarySkill();
        final Skill secondary = ((AbstractBuildingWorker) workBuilding).getSecondarySkill();

        data.getCitizenSkillHandler().addXpToSkill(primary, localXp, data);
        data.getCitizenSkillHandler().addXpToSkill(primary.getComplimentary(), localXp / 10, data);
        data.getCitizenSkillHandler().removeXpFromSkill(primary.getAdverse(), localXp / 10, data);

        data.getCitizenSkillHandler().addXpToSkill(secondary, localXp / 2.0, data);
        data.getCitizenSkillHandler().addXpToSkill(secondary.getComplimentary(), localXp / 20, data);
        data.getCitizenSkillHandler().removeXpFromSkill(secondary.getAdverse(), localXp / 20, data);
    }

    @Override
    public void dropExperience()
    {
        int experience;

        if (!CompatibilityUtils.getWorldFromCitizen(citizen).isRemote && citizen.getRecentlyHit() > 0 && citizen.checkCanDropLoot() && CompatibilityUtils.getWorldFromCitizen(citizen).getGameRules().getBoolean(
          GameRules.DO_MOB_LOOT))
        {
            experience = (int) (citizen.getCitizenData().getCitizenSkillHandler().getTotalXP());

            while (experience > 0)
            {
                final int j = ExperienceOrbEntity.getXPSplit(experience);
                experience -= j;
                CompatibilityUtils.getWorldFromCitizen(citizen)
                  .addEntity(new ExperienceOrbEntity(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.posX, citizen.posY, citizen.posZ, j));
            }
        }

        //Spawn particle explosion of xp orbs on death
        for (int i = 0; i < XP_PARTICLE_EXPLOSION_SIZE; ++i)
        {
            final double d2 = citizen.getRandom().nextGaussian() * 0.02D;
            final double d0 = citizen.getRandom().nextGaussian() * 0.02D;
            final double d1 = citizen.getRandom().nextGaussian() * 0.02D;
            CompatibilityUtils.getWorldFromCitizen(citizen).addParticle(ParticleTypes.EXPLOSION,
              citizen.posX + (citizen.getRandom().nextDouble() * citizen.getWidth() * 2.0F) - (double) citizen.getWidth(),
              citizen.posY + (citizen.getRandom().nextDouble() * citizen.getHeight()),
              citizen.posZ + (citizen.getRandom().nextDouble() * citizen.getWidth() * 2.0F) - (double) citizen.getWidth(),
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

        for (@NotNull final ExperienceOrbEntity orb : citizen.world.getEntitiesWithinAABB(ExperienceOrbEntity.class, citizen.getBoundingBox().grow(2)))
        {
            Vec3d vec3d = new Vec3d(citizen.posX - orb.getPosX(), citizen.posY + (double)this.citizen.getEyeHeight() / 2.0D - orb.getPosY(), citizen.getPosZ() - orb.getPosZ());
            double d1 = vec3d.lengthSquared();

            if (d1 < 1.0D) {
                addExperience(orb.getXpValue() / 2.0D);
                orb.remove();
                return;
            }
            double d2 = 1.0D - Math.sqrt(d1) / 8.0D;
            orb.setMotion(orb.getMotion().add(vec3d.normalize().scale(d2 * d2 * 0.1D)));
        }
    }
}
