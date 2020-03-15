package com.minecolonies.api.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import net.minecraft.nbt.CompoundNBT;

import java.util.Random;

/**
 * The interface for the citizen skill handler.
 */
public interface ICitizenSkillHandler
{
    void init(int levelCap);

    CompoundNBT write();

    void read(CompoundNBT compoundNBT);

    void tryLevelUpIntelligence(Random random, int customChance, ICitizenData citizen);

    int getLevel(Skill intelligence);

    void addXpToSkill(Skill primary, double xp, ICitizenData data);

    void removeXpFromSkill(Skill skill, double xp, ICitizenData data);
}
