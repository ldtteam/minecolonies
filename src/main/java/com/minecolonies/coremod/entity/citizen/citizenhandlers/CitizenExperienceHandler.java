package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenExperienceHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.util.ExperienceUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.DATA_LEVEL;
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
     * Skill modifier defines how fast a citizen levels in a certain skill.
     */
    private double skillModifier = 0;

    /**
     * The current level.
     */
    private int level;

    /**
     * Constructor for the experience handler.
     * @param citizen the citizen owning the handler.
     */
    public CitizenExperienceHandler(final EntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Updates the level of the citizen.
     */
    @Override
    public void updateLevel()
    {
        level = citizen.getCitizenData() == null ? 0 : citizen.getCitizenData().getLevel();
        citizen.getDataManager().set(DATA_LEVEL, level);
        if (citizen.getCitizenData().getJob() != null)
        {
            citizen.getCitizenData().getJob().onLevelUp(level);
        }
    }

    /**
     * Set the skill modifier which defines how fast a citizen levels in a
     * certain skill.
     *
     * @param modifier input modifier.
     */
    @Override
    public void setSkillModifier(final int modifier)
    {
        this.skillModifier = modifier;
    }

    /**
     * Add experience points to citizen.
     * Increases the citizen level if he has sufficient experience.
     * This will reset the experience.
     *
     * @param xp the amount of points added.
     */
    @Override
    public void addExperience(final double xp)
    {
        final IBuilding home = citizen.getCitizenColonyHandler().getHomeBuilding();

        final double citizenHutLevel = home == null ? 0 : home.getBuildingLevel();
        final double citizenHutMaxLevel = home == null ? 1 : home.getMaxBuildingLevel();
        if (citizen.getCitizenData() != null)
        {
            addExperienceToCitizenData(xp, citizenHutLevel, citizenHutMaxLevel);
        }
    }

    private void addExperienceToCitizenData(final double xp, final double citizenHutLevel, final double citizenHutMaxLevel)
    {
        if ((citizenHutLevel < citizenHutMaxLevel
               && Math.pow(2.0, citizenHutLevel + 1.0) <= citizen.getCitizenData().getLevel()) || citizen.getCitizenData().getLevel() >= MAX_CITIZEN_LEVEL)
        {
            return;
        }

        double localXp = xp * skillModifier / EXP_DIVIDER;
        final double workBuildingLevel = citizen.getCitizenColonyHandler().getWorkBuilding() == null ? 0 : citizen.getCitizenColonyHandler().getWorkBuilding().getBuildingLevel();
        final double bonusXp = (workBuildingLevel * (1 + citizenHutLevel) / Math.log(citizen.getCitizenData().getLevel() + 2.0D)) / 2;
        localXp = localXp * bonusXp;
        final double saturation = citizen.getCitizenData().getSaturation();

        XpBasedOnSaturationCalculator xpBasedOnSaturationCalculator = new XpBasedOnSaturationCalculator(localXp, saturation).invoke();
        if (xpBasedOnSaturationCalculator.is())
        {
            return;
        }
        localXp = xpBasedOnSaturationCalculator.getLocalXp();

        final double maxValue = Integer.MAX_VALUE - citizen.getCitizenData().getExperience();
        if (localXp > maxValue)
        {
            localXp = maxValue;
        }

        localXp = applyMending(localXp);
        citizen.getCitizenData().addExperience(localXp);

        while (ExperienceUtils.getXPNeededForNextLevel(citizen.getCitizenData().getLevel()) < citizen.getCitizenData().getExperience())
        {
            citizen.getCitizenData().levelUp();
        }
        updateLevel();
        citizen.markDirty();
    }

    /**
     * repair random equipped/held item with mending enchant.
     *
     * @param xp amount of xp available to mend with
     * @return xp left after mending
     */
    private double applyMending(final double xp)
    {
        double localXp = xp;

        final int toolSlot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventoryCitizen(), stack -> stack.isEnchanted() && EnchantmentHelper.getEnchantments(stack).containsKey(Enchantments.MENDING));
        if (toolSlot == -1)
        {
            return localXp;
        }

        final ItemStack tool = citizen.getInventoryCitizen().getStackInSlot(toolSlot);
        if (!ItemStackUtils.isEmpty(tool) && tool.isDamaged())
        {
            //2 xp to heal 1 dmg
            final double dmgHealed = Math.min(localXp / 2, tool.getDamage());
            localXp -= dmgHealed * 2;
            tool.setDamage(tool.getDamage() - (int) Math.ceil(dmgHealed));
        }

        return localXp;
    }

    /**
     * Drop some experience share depending on the experience and
     * experienceLevel.
     */
    @Override
    public void dropExperience()
    {
        int experience;

        if (!CompatibilityUtils.getWorldFromCitizen(citizen).isRemote && citizen.getRecentlyHit() > 0 && citizen.checkCanDropLoot() && CompatibilityUtils.getWorldFromCitizen(citizen).getGameRules().getBoolean(
          GameRules.DO_MOB_LOOT))
        {
            experience = (int) (citizen.getCitizenData().getExperience());

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

    /**
     * Collect exp orbs around the entity.
     */
    @Override
    public void gatherXp()
    {
        for (@NotNull final ExperienceOrbEntity orb : getXPOrbsOnGrid())
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

    /**
     * Defines the area in which the citizen automatically gathers experience.
     *
     * @return a list of xp orbs around the entity.
     */
    private List<ExperienceOrbEntity> getXPOrbsOnGrid()
    {
        @NotNull final AxisAlignedBB bb = new AxisAlignedBB(citizen.posX - 2, citizen.posY - 2, citizen.posZ - 2, citizen.posX + 2, citizen.posY + 2, citizen.posZ + 2);

        return CompatibilityUtils.getWorldFromCitizen(citizen).getEntitiesWithinAABB(ExperienceOrbEntity.class, bb);
    }

    /**
     * Get the level of the citizen.
     * @return the level.
     */
    @Override
    public int getLevel()
    {
        return this.level;
    }

    /**
     * Setter for the level.
     * @param level the level.
     */
    @Override
    public void setLevel(final int level)
    {
        this.level = level;
    }

    private class XpBasedOnSaturationCalculator
    {
        private final double  saturation;
        private       boolean myResult;
        private       double  localXp;

        public XpBasedOnSaturationCalculator(final double localXp, final double saturation)
        {
            this.localXp = localXp;
            this.saturation = saturation;
        }

        boolean is() {return myResult;}

        public double getLocalXp()
        {
            return localXp;
        }

        public XpBasedOnSaturationCalculator invoke()
        {
            if (saturation < AVERAGE_SATURATION)
            {
                if (saturation <= 0)
                {
                    myResult = true;
                    return this;
                }

                if (saturation < LOW_SATURATION)
                {
                    localXp -= localXp * BIG_SATURATION_FACTOR * saturation;
                }
                else
                {
                    localXp -= localXp * LOW_SATURATION_FACTOR * saturation;
                }
            }
            else if (saturation > AVERAGE_SATURATION)
            {
                if (saturation > HIGH_SATURATION)
                {
                    localXp += localXp * BIG_SATURATION_FACTOR * saturation;
                }
                else
                {
                    localXp += localXp * LOW_SATURATION_FACTOR * saturation;
                }
            }
            myResult = false;
            return this;
        }
    }
}
