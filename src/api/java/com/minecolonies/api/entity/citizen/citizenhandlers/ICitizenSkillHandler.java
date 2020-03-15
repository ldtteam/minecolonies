package com.minecolonies.api.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
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

    void tryLevelUpIntelligence(Random random, int customChance, AbstractEntityCitizen citizen);

    void tryLevelUpIntelligence(Random random, int customChance, ICitizenData citizen);

    void addExperience(IBuilding building, double xp);
}
