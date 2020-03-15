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

    @Override
    public void updateLevel()
    {
        level = citizen.getCitizenData() == null ? 0 : citizen.getCitizenData().getLevel();
        citizen.getDataManager().set(DATA_LEVEL, level);
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

        if (saturation <= 0)
        {
            return;
        }

        if (saturation < AVERAGE_SATURATION)
        {
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

    @Override
    public void gatherXp()
    {
        //todo test BB
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
