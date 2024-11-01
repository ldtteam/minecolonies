package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import com.minecolonies.core.Network;
import com.minecolonies.core.network.messages.client.VanillaParticleMessage;
import com.minecolonies.core.util.ExperienceUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

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
    public Map<Skill, SkillData> skillMap = new EnumMap<>(Skill.class);

    public CitizenSkillHandler()
    {
        for (final Skill skill : Skill.values())
        {
            skillMap.put(skill, new SkillData(1, 0.0D));
        }
    }

    @Override
    public void init(final int levelCap)
    {
        if (levelCap <= 1)
        {
            for (final Skill skill : Skill.values())
            {
                skillMap.put(skill, new SkillData(1, 0.0D));
            }
        }
        else
        {
            final Random random = new Random();
            for (final Skill skill : Skill.values())
            {
                skillMap.put(skill, new SkillData(random.nextInt(levelCap - 1) + 1, 0.0D));
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
            final int firstRoleModelLevel = roleModelA.getCitizenSkillHandler().getSkills().get(skill).level;
            final int secondRoleModelLevel = roleModelB.getCitizenSkillHandler().getSkills().get(skill).level;
            totalPoints += firstRoleModelLevel + secondRoleModelLevel;
        }

        for (final Skill skill : Skill.values())
        {
            final double firstRoleModelLevel = roleModelA.getCitizenSkillHandler().getSkills().get(skill).level;
            final double secondRoleModelLevel = roleModelB.getCitizenSkillHandler().getSkills().get(skill).level;

            int newPoints = (int) (((firstRoleModelLevel + secondRoleModelLevel) / totalPoints) * bonusPoints);
            skillMap.get(skill).level += newPoints;
        }
    }

    @NotNull
    @Override
    public CompoundTag write()
    {
        final CompoundTag compoundNBT = new CompoundTag();

        @NotNull final ListTag levelTagList = new ListTag();
        for (@NotNull final Map.Entry<Skill, SkillData> entry : skillMap.entrySet())
        {
            if (entry.getKey() != null && entry.getValue() != null)
            {
                @NotNull final CompoundTag levelCompound = new CompoundTag();
                levelCompound.putInt(TAG_SKILL, entry.getKey().ordinal());
                levelCompound.putInt(TAG_LEVEL, entry.getValue().level);
                levelCompound.putDouble(TAG_EXPERIENCE, entry.getValue().experience);
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
              new SkillData(Math.max(1, Math.min(levelExperienceAtJob.getInt(TAG_LEVEL), MAX_CITIZEN_LEVEL)), levelExperienceAtJob.getDouble(TAG_EXPERIENCE)));
        }
    }

    @Override
    public boolean tryLevelUpIntelligence(@NotNull final Random random, final double customChance, @NotNull final ICitizenData citizen)
    {
        if (customChance > 0 && random.nextDouble() * customChance < 1)
        {
            return false;
        }

        final int levelCap = (int) citizen.getCitizenHappinessHandler().getHappiness(citizen.getColony(), citizen);
        if (skillMap.get(Skill.Intelligence).level < levelCap * 9)
        {
            addXpToSkill(Skill.Intelligence, 10, citizen);
        }
        return true;
    }

    @Override
    public int getLevel(@NotNull final Skill skill)
    {
        return skillMap.get(skill).level;
    }

    @Override
    public void incrementLevel(@NotNull final Skill skill, final int level)
    {
        final SkillData current = skillMap.get(skill);
        current.level = Math.min(MAX_CITIZEN_LEVEL, Math.max(current.level + level, 1));
    }

    @Override
    public void addXpToSkill(final Skill skill, final double xp, final ICitizenData data)
    {
        final SkillData skillData = skillMap.getOrDefault(skill, new SkillData(0, 0.0D));

        final IBuilding home = data.getHomeBuilding();

        final double citizenHutLevel = home == null ? 0 : home.getBuildingLevel();
        final double citizenHutMaxLevel = home == null ? MAX_BUILDING_LEVEL : home.getMaxBuildingLevel();

        if (((citizenHutLevel < citizenHutMaxLevel || citizenHutMaxLevel < MAX_BUILDING_LEVEL) && (citizenHutLevel + 1) * 10 <= skillData.level)
              || skillData.level >= MAX_CITIZEN_LEVEL)
        {
            return;
        }

        final int orgLevel = skillData.level;
        double xpToLevelUp = Math.min(Double.MAX_VALUE, skillData.experience + xp);
        while (xpToLevelUp > 0)
        {
            final double nextLevel = ExperienceUtils.getXPNeededForNextLevel(skillData.level);
            if (nextLevel > xpToLevelUp)
            {
                skillData.experience = xpToLevelUp;
                break;
            }
            else
            {
                xpToLevelUp = xpToLevelUp - nextLevel;
                skillData.level++;
            }
        }

        if (skillData.level > orgLevel)
        {
            levelUp(data);
            data.markDirty(10);
        }
    }

    @Override
    public void removeXpFromSkill(@NotNull final Skill skill, final double xp, @NotNull final ICitizenData data)
    {
        final SkillData skillData = skillMap.get(skill);

        double xpToRemove = xp;
        while (xpToRemove > 0)
        {
            if (skillData.experience >= xpToRemove || skillData.level <= 1)
            {
                skillData.experience = Math.max(0, skillData.experience - xpToRemove);
                break;
            }
            else
            {
                xpToRemove -= skillData.experience;
                skillData.experience = ExperienceUtils.getXPNeededForNextLevel(skillData.level - 1);
                skillData.level--;
                data.markDirty(40);
            }
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
        for (SkillData tuple : skillMap.values())
        {
            totalXp += tuple.experience;
        }
        return totalXp;
    }

    @Override
    public Map<Skill, SkillData> getSkills()
    {
        return Collections.unmodifiableMap(skillMap);
    }

    public static class SkillData
    {
        private int    level;
        private double experience;

        private SkillData(final int level, final double experience)
        {
            this.level = level;
            this.experience = experience;
        }

        public int getLevel()
        {
            return level;
        }

        public void setLevel(final int level)
        {
            this.level = level;
        }

        public double getExperience()
        {
            return experience;
        }

        public void setExperience(final double experience)
        {
            this.experience = experience;
        }
    }
}
