package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenExperienceHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.util.ExperienceUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
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
        final ItemStack tool = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, citizen);

        if (!ItemStackUtils.isEmpty(tool) && tool.isItemDamaged())
        {
            //2 xp to heal 1 dmg
            final double dmgHealed = Math.min(localXp / 2, tool.getItemDamage());
            localXp -= dmgHealed * 2;
            tool.setItemDamage(tool.getItemDamage() - (int) Math.ceil(dmgHealed));
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

        if (!CompatibilityUtils.getWorldFromCitizen(citizen).isRemote && citizen.getRecentlyHit() > 0 && citizen.checkCanDropLoot() && CompatibilityUtils.getWorldFromCitizen(citizen).getGameRules().getBoolean("doMobLoot"))
        {
            experience = (int) (citizen.getCitizenData().getExperience());

            while (experience > 0)
            {
                final int j = EntityXPOrb.getXPSplit(experience);
                experience -= j;
                CompatibilityUtils.getWorldFromCitizen(citizen).spawnEntity(new EntityXPOrb(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.posX, citizen.posY, citizen.posZ, j));
            }
        }

        //Spawn particle explosion of xp orbs on death
        for (int i = 0; i < XP_PARTICLE_EXPLOSION_SIZE; ++i)
        {
            final double d2 = citizen.getRandom().nextGaussian() * 0.02D;
            final double d0 = citizen.getRandom().nextGaussian() * 0.02D;
            final double d1 = citizen.getRandom().nextGaussian() * 0.02D;
            CompatibilityUtils.getWorldFromCitizen(citizen).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE,
              citizen.posX + (citizen.getRandom().nextDouble() * citizen.width * 2.0F) - (double) citizen.width,
              citizen.posY + (citizen.getRandom().nextDouble() * citizen.height),
              citizen.posZ + (citizen.getRandom().nextDouble() * citizen.width * 2.0F) - (double) citizen.width,
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
        for (@NotNull final EntityXPOrb orb : getXPOrbsOnGrid())
        {
            addExperience(orb.getXpValue() / 2.0D);
            orb.setDead();
        }
    }

    /**
     * Defines the area in which the citizen automatically gathers experience.
     *
     * @return a list of xp orbs around the entity.
     */
    private List<EntityXPOrb> getXPOrbsOnGrid()
    {
        @NotNull final AxisAlignedBB bb = new AxisAlignedBB(citizen.posX - 2, citizen.posY - 2, citizen.posZ - 2, citizen.posX + 2, citizen.posY + 2, citizen.posZ + 2);

        return CompatibilityUtils.getWorldFromCitizen(citizen).getEntitiesWithinAABB(EntityXPOrb.class, bb);
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
