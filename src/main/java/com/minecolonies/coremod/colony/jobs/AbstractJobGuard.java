package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.EntityCitizen;
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
    public AbstractJobGuard(final CitizenData entity)
    {
        super(entity);
    }

    protected abstract AbstractEntityAIGuard generateGuardAI();

    @Override
    public AbstractAISkeleton<? extends AbstractJob> generateAI()
    {
        return generateGuardAI();
    }

    @Override
    public void triggerDeathAchievement(final DamageSource source, final EntityCitizen citizen)
    {
        super.triggerDeathAchievement(source, citizen);
        if (source.getTrueSource() instanceof EntityEnderman && citizen.getCitizenColonyHandler().getColony() != null)
        {
            citizen.getCitizenColonyHandler().getColony().getStatsManager().triggerAchievement(ModAchievements.achievementGuardDeathEnderman);
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
