package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.VanillaParticleMessage;
import com.minecolonies.coremod.util.ExperienceUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.MAX_CITIZEN_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_LEVEL_MAP;

/**
 * The citizen skill handler of the citizen.
 */
public class CitizenSkillHandler implements ICitizenSkillHandler
{
    /**
     * Chance to level up intelligence.
     */
    private static final int CHANCE_TO_LEVEL = 50;

    /**
     * Defines how much child stats very from parents average(+ or -).
     */
    private static final int CHILD_STATS_VARIANCE = 3;

    /**
     * Max inheritance of stats.
     */
    private static final int MAX_INHERITANCE = 10;

    /**
     * Skill map.
     */
    public Map<Skill, Tuple<Integer, Double>> skillMap        = new HashMap<>();

    @Override
    public void init(final int levelCap)
    {
        if (levelCap <= 1)
        {
            for (final Skill skill : Skill.values())
            {
                skillMap.put(skill, new Tuple<>(1, 0.0D));
            }
        }
        else
        {
            final Random random = new Random();
            for (final Skill skill : Skill.values())
            {
                skillMap.put(skill, new Tuple<>(random.nextInt(levelCap - 1) + 1, 0.0D));
            }
        }
    }

    @Override
    public void init(@NotNull final ICitizenData mom, @NotNull final ICitizenData dad, final Random rand)
    {
        for (final Skill skill : Skill.values())
        {
            skillMap.put(skill, new Tuple<>((Math.min(MAX_INHERITANCE, mom.getCitizenSkillHandler().getLevel(skill)) + Math.min(MAX_INHERITANCE, dad.getCitizenSkillHandler().getLevel(skill))) / 2 + rand.nextInt(CHILD_STATS_VARIANCE) - rand.nextInt(CHILD_STATS_VARIANCE), 0.0D));
        }
    }

    @NotNull
    @Override
    public CompoundNBT write()
    {
        final CompoundNBT compoundNBT = new CompoundNBT();

        @NotNull final ListNBT levelTagList = new ListNBT();
        for (@NotNull final Map.Entry<Skill, Tuple<Integer, Double>> entry : skillMap.entrySet())
        {
            @NotNull final CompoundNBT levelCompound = new CompoundNBT();
            levelCompound.putInt(TAG_SKILL, entry.getKey().ordinal());
            levelCompound.putInt(TAG_LEVEL, entry.getValue().getA());
            levelCompound.putDouble(TAG_EXPERIENCE, entry.getValue().getB());
            levelTagList.add(levelCompound);
        }
        compoundNBT.put(TAG_LEVEL_MAP, levelTagList);

        return compoundNBT;
    }

    @Override
    public void read(@NotNull final CompoundNBT compoundNBT)
    {
        final ListNBT levelTagList = compoundNBT.getList(TAG_LEVEL_MAP, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < levelTagList.size(); ++i)
        {
            final CompoundNBT levelExperienceAtJob = levelTagList.getCompound(i);
            skillMap.put(Skill.values()[levelExperienceAtJob.getInt(TAG_SKILL)],
              new Tuple<>(Math.min(levelExperienceAtJob.getInt(TAG_LEVEL), MAX_CITIZEN_LEVEL), levelExperienceAtJob.getDouble(TAG_EXPERIENCE)));
        }
    }

    @Override
    public void tryLevelUpIntelligence(@NotNull final Random random, final int customChance, @NotNull final ICitizenData citizen)
    {
        if ((customChance > 0 && random.nextInt(customChance) > 0) || (customChance < 1 && random.nextInt(CHANCE_TO_LEVEL) > 0))
        {
            return;
        }

        final int levelCap = (int) citizen.getCitizenHappinessHandler().getHappiness();
        if (skillMap.get(Skill.Intelligence).getB() < levelCap * 9)
        {
            addXpToSkill(Skill.Intelligence, 10, citizen);
        }
    }

    @Override
    public int getLevel(@NotNull final Skill skill)
    {
        return skillMap.get(skill).getA();
    }

    @Override
    public void incrementLevel(@NotNull final Skill skill, final int level)
    {
        final Tuple<Integer, Double> current = skillMap.get(skill);
        skillMap.put(skill, new Tuple<>(Math.min(MAX_CITIZEN_LEVEL, Math.max(current.getA() + level, 1)), current.getB()));
    }

    @Override
    public void addXpToSkill(final Skill skill, final double xp, final ICitizenData data)
    {
        final Tuple<Integer, Double> tuple = skillMap.get(skill);
        int level = tuple.getA();
        final double currentXp = tuple.getB();

        final IBuilding home = data.getHomeBuilding();

        final double citizenHutLevel = home == null ? 0 : home.getBuildingLevel();
        final double citizenHutMaxLevel = home == null ? 5 : home.getMaxBuildingLevel();

        if ((citizenHutLevel < citizenHutMaxLevel && Math.pow(2.0, citizenHutLevel + 1.0) <= level) || level >= MAX_CITIZEN_LEVEL)
        {
            return;
        }

        double xpToLevelUp = Math.min(Double.MAX_VALUE, currentXp + xp);
        while (xpToLevelUp > 0)
        {
            final double nextLevel = ExperienceUtils.getXPNeededForNextLevel(level);
            if (nextLevel > xpToLevelUp)
            {
                skillMap.put(skill, new Tuple<>(Math.min(MAX_CITIZEN_LEVEL, level), xpToLevelUp));
                xpToLevelUp = 0;
            }
            else
            {
                xpToLevelUp = xpToLevelUp - nextLevel;
                level++;
            }
        }

        if (level > tuple.getA())
        {
            levelUp(data);
            data.markDirty();
        }
    }

    @Override
    public void removeXpFromSkill(@NotNull final Skill skill, final double xp, @NotNull final ICitizenData data)
    {
        final Tuple<Integer, Double> tuple = skillMap.get(skill);
        int level = tuple.getA();
        double currentXp = tuple.getB();

        double xpToDiscount = xp;
        while (xpToDiscount > 0)
        {
            if (currentXp >= xpToDiscount)
            {
                skillMap.put(skill, new Tuple<>(Math.max(1, level), currentXp - xpToDiscount));
                xpToDiscount = 0;
            }
            else
            {
                xpToDiscount -= currentXp;
                currentXp = ExperienceUtils.getXPNeededForNextLevel(level-1);
                level--;
            }
        }

        if (level < tuple.getA())
        {
            data.markDirty();
        }
    }

    @Override
    public void levelUp(final ICitizenData data)
    {
        // Show level-up particles
        if (data.getCitizenEntity().isPresent())
        {
            final AbstractEntityCitizen citizen = data.getCitizenEntity().get();
            Network.getNetwork()
              .sendToTrackingEntity(new VanillaParticleMessage(citizen.getPosX(), citizen.getPosY(), citizen.getPosZ(), ParticleTypes.HAPPY_VILLAGER), data.getCitizenEntity().get());
        }

        if (data.getJob() != null)
        {
            data.getJob().onLevelUp();
        }
    }

    @Override
    public int getJobModifier(@NotNull final ICitizenData data)
    {
        final IBuilding workBuilding = data.getWorkBuilding();
        if (workBuilding instanceof AbstractBuildingWorker)
        {
            final Skill primary = ((AbstractBuildingWorker) workBuilding).getPrimarySkill();
            final Skill secondary = ((AbstractBuildingWorker) workBuilding).getSecondarySkill();
            return (getLevel(primary) + getLevel(secondary))/4;
        }
        return 0;
    }

    @Override
    public int getJobModifier(@NotNull final IBuildingView workBuilding)
    {
        if (workBuilding instanceof AbstractBuildingWorker.View)
        {
            final Skill primary = ((AbstractBuildingWorker.View) workBuilding).getPrimarySkill();
            final Skill secondary = ((AbstractBuildingWorker.View) workBuilding).getSecondarySkill();
            return (getLevel(primary) + getLevel(secondary))/4;
        }
        return 0;
    }

    @Override
    public double getTotalXP()
    {
        double totalXp = 0;
        for (final Tuple<Integer, Double> tuple : skillMap.values())
        {
            totalXp += tuple.getB();
        }
        return totalXp;
    }

    @Override
    public Map<Skill, Tuple<Integer, Double>> getSkills()
    {
        return ImmutableMap.copyOf(skillMap);
    }
}
