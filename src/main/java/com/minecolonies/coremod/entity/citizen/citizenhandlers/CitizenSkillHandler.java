package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.client.VanillaParticleMessage;
import com.minecolonies.coremod.util.ExperienceUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.minecolonies.api.sounds.EventType.GREETING;
import static com.minecolonies.api.sounds.EventType.SUCCESS;
import static com.minecolonies.api.util.SoundUtils.playSoundAtCitizenWith;
import static com.minecolonies.api.util.constant.CitizenConstants.MAX_CITIZEN_LEVEL;
import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The citizen skill handler of the citizen.
 */
public class CitizenSkillHandler implements ICitizenSkillHandler
{
    /**
     * Skill map.
     */
    public Map<Skill, Tuple<Integer, Double>> skillMap = new HashMap<>();

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
    public void init(@NotNull final IColony colony, @Nullable final ICitizenData firstParent, @Nullable final ICitizenData secondParent, final Random rand)
    {
        ICitizenData roleModelA;
        ICitizenData roleModelB;

        if (firstParent == null)
        {
            roleModelA = colony.getCitizenManager().getRandomCitizen();
        }
        else
        {
            roleModelA = firstParent;
        }

        if (secondParent == null)
        {
            roleModelB = colony.getCitizenManager().getRandomCitizen();
        }
        else
        {
            roleModelB = secondParent;
        }

        final int levelCap = (int) colony.getOverallHappiness();
        init(levelCap);

        final int bonusPoints = 25 + rand.nextInt(25);

        int totalPoints = 0;
        for (final Skill skill : Skill.values())
        {
            final int firstRoleModelLevel = roleModelA.getCitizenSkillHandler().getSkills().get(skill).getA();
            final int secondRoleModelLevel = roleModelB.getCitizenSkillHandler().getSkills().get(skill).getA();
            totalPoints += firstRoleModelLevel + secondRoleModelLevel;
        }

        for (final Skill skill : Skill.values())
        {
            final double firstRoleModelLevel = roleModelA.getCitizenSkillHandler().getSkills().get(skill).getA();
            final double secondRoleModelLevel = roleModelB.getCitizenSkillHandler().getSkills().get(skill).getA();

            int newPoints = (int) (((firstRoleModelLevel + secondRoleModelLevel) / totalPoints) * bonusPoints);

            skillMap.put(skill, new Tuple<>(skillMap.get(skill).getA() + newPoints, 0.0D));
        }
    }

    @NotNull
    @Override
    public CompoundTag write()
    {
        final CompoundTag compoundNBT = new CompoundTag();

        @NotNull final ListTag levelTagList = new ListTag();
        for (@NotNull final Map.Entry<Skill, Tuple<Integer, Double>> entry : skillMap.entrySet())
        {
            if (entry.getKey() != null && entry.getValue() != null)
            {
                @NotNull final CompoundTag levelCompound = new CompoundTag();
                levelCompound.putInt(TAG_SKILL, entry.getKey().ordinal());
                levelCompound.putInt(TAG_LEVEL, entry.getValue().getA());
                levelCompound.putDouble(TAG_EXPERIENCE, entry.getValue().getB());
                levelTagList.add(levelCompound);
            }
        }
        compoundNBT.put(TAG_LEVEL_MAP, levelTagList);

        return compoundNBT;
    }

    @Override
    public void read(@NotNull final CompoundTag compoundNBT)
    {
        final ListTag levelTagList = compoundNBT.getList(TAG_LEVEL_MAP, Tag.TAG_COMPOUND);
        for (int i = 0; i < levelTagList.size(); ++i)
        {
            final CompoundTag levelExperienceAtJob = levelTagList.getCompound(i);
            skillMap.put(Skill.values()[levelExperienceAtJob.getInt(TAG_SKILL)],
              new Tuple<>(Math.max(1, Math.min(levelExperienceAtJob.getInt(TAG_LEVEL), MAX_CITIZEN_LEVEL)), levelExperienceAtJob.getDouble(TAG_EXPERIENCE)));
        }
    }

    @Override
    public void tryLevelUpIntelligence(@NotNull final Random random, final double customChance, @NotNull final ICitizenData citizen)
    {
        if (customChance > 0 && random.nextDouble() * customChance < 1)
        {
            return;
        }

        final int levelCap = (int) citizen.getCitizenHappinessHandler().getHappiness(citizen.getColony(), citizen);
        if (skillMap.get(Skill.Intelligence).getA() < levelCap * 9)
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
        final Tuple<Integer, Double> tuple = skillMap.getOrDefault(skill, new Tuple<>(0, 0.0D));
        int level = tuple.getA();
        final double currentXp = tuple.getB();

        final IBuilding home = data.getHomeBuilding();

        final double citizenHutLevel = home == null ? 0 : home.getBuildingLevel();
        final double citizenHutMaxLevel = home == null ? MAX_BUILDING_LEVEL : home.getMaxBuildingLevel();

        if (((citizenHutLevel < citizenHutMaxLevel || citizenHutMaxLevel < MAX_BUILDING_LEVEL) && (citizenHutLevel + 1) * 10 <= level) || level >= MAX_CITIZEN_LEVEL)
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
            if (currentXp >= xpToDiscount || level <= 1)
            {
                skillMap.put(skill, new Tuple<>(Math.max(1, level), Math.max(0, currentXp - xpToDiscount)));
                break;
            }
            else
            {
                xpToDiscount -= currentXp;
                currentXp = ExperienceUtils.getXPNeededForNextLevel(level - 1);
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
        if (data.getEntity().isPresent())
        {
            final AbstractEntityCitizen citizen = data.getEntity().get();
            playSoundAtCitizenWith(citizen.level, citizen.blockPosition(), SUCCESS, data);
            Network.getNetwork()
              .sendToTrackingEntity(new VanillaParticleMessage(citizen.getX(), citizen.getY(), citizen.getZ(), ParticleTypes.HAPPY_VILLAGER),
                data.getEntity().get());
        }

        if (data.getJob() != null)
        {
            data.getJob().onLevelUp();
        }
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
