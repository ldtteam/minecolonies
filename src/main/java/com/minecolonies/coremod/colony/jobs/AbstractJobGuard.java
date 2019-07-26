package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.entity.IEntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.sounds.ArcherSounds;
import com.minecolonies.coremod.sounds.KnightSounds;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract Class for Guard Jobs.
 */
public abstract class AbstractJobGuard extends AbstractJob
{
    /**
     * Initialize citizen data.
     *
     * @param entity the citizen data.
     */
    public AbstractJobGuard(final ICitizenData entity)
    {
        super(entity);
    }

    protected abstract AbstractEntityAIGuard generateGuardAI();

    @Override
    public AbstractAISkeleton<? extends IJob> generateAI()
    {
        return generateGuardAI();
    }

    @Override
    public void triggerDeathAchievement(final DamageSource source, final IEntityCitizen citizen)
    {
        super.triggerDeathAchievement(source, citizen);
        if (source.getTrueSource() instanceof EntityEnderman && citizen.getCitizenColonyHandler().getColony() != null)
        {
            citizen.getCitizenColonyHandler().getColony().getStatsManager().triggerAchievement(ModAchievements.achievementGuardDeathEnderman);
        }
    }

    /**
     * Custom Action on Levelup, increases Guard HP
     */
    @Override
    public void onLevelUp(final int newLevel)
    {
        // Bonus Health for guards(gets reset upon Firing)
        if (getCitizen().getCitizenEntity().isPresent())
        {
            getCitizen().getCitizenEntity().get().increaseHPForGuards();
        }
    }

    @Nullable
    @Override
    public SoundEvent getBadWeatherSound()
    {
        if (getCitizen() != null)
        {
            return getCitizen().isFemale() ? ArcherSounds.Female.badWeather : KnightSounds.Male.badWeather;
        }
        return null;
    }

    @Override
    public SoundEvent getBedTimeSound()
    {
        if (getCitizen() != null)
        {
                return getCitizen().isFemale() ? ArcherSounds.Female.offToBed : KnightSounds.Male.offToBed;
        }
        return null;
    }
}
