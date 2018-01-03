package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.client.render.RenderBipedCitizen;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIMeleeGuard;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIRangeGuard;
import com.minecolonies.coremod.sounds.ArcherSounds;
import com.minecolonies.coremod.sounds.KnightSounds;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Job class of the guard.
 */
public class JobGuard extends AbstractJob
{
    /**
     * The higher the number the lower the chance to spawn a knight. Default: 3, 50% chance.
     */
    private static final int GUARD_CHANCE = 3;

    /**
     * Public constructor of the farmer job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobGuard(final CitizenData entity)
    {
        super(entity);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.Guard";
    }

    @NotNull
    @Override
    public RenderBipedCitizen.Model getModel()
    {
        final AbstractBuilding building = getCitizen().getWorkBuilding();
        if (building instanceof AbstractBuildingGuards)
        {
            AbstractBuildingGuards.GuardJob job = ((AbstractBuildingGuards) building).getJob();
            if (job == null)
            {
                job = generateRandomAI((AbstractBuildingGuards) building);
            }

            if (job == AbstractBuildingGuards.GuardJob.KNIGHT)
            {
                return RenderBipedCitizen.Model.KNIGHT_GUARD;
            }
            return RenderBipedCitizen.Model.ARCHER_GUARD;
        }
        return RenderBipedCitizen.Model.ARCHER_GUARD;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public AbstractAISkeleton<? extends AbstractJob> generateAI()
    {
        final AbstractBuilding building = getCitizen().getWorkBuilding();
        if (building instanceof AbstractBuildingGuards)
        {
            final AbstractBuildingGuards.GuardJob job = ((AbstractBuildingGuards) building).getJob();
            if (job == AbstractBuildingGuards.GuardJob.KNIGHT)
            {
                return new EntityAIMeleeGuard(this);
            }
            return new EntityAIRangeGuard(this);
        }
        return new EntityAIRangeGuard(this);
    }

    @Nullable
    @Override
    public SoundEvent getBadWeatherSound()
    {
        if (getCitizen() != null)
        {
            if (getModel().equals(RenderBipedCitizen.Model.ARCHER_GUARD))
            {
                return getCitizen().isFemale() ? ArcherSounds.Female.badWeather : null;
            }
            else
            {
                return getCitizen().isFemale() ? null : KnightSounds.Male.badWeather;
            }
        }
        return null;
    }

    /**
     * Sets a random job of the job hasn't been set yet.
     *
     * @param building the building of the guard.
     * @return the new job.
     */
    @NotNull
    private static AbstractBuildingGuards.GuardJob generateRandomAI(@NotNull final AbstractBuildingGuards building)
    {
        final int chance = new Random().nextInt(GUARD_CHANCE);
        if (chance == 1)
        {
            building.setJob(AbstractBuildingGuards.GuardJob.KNIGHT);
            return AbstractBuildingGuards.GuardJob.KNIGHT;
        }
        building.setJob(AbstractBuildingGuards.GuardJob.RANGER);
        return AbstractBuildingGuards.GuardJob.RANGER;
    }

    @Override
    public void triggerDeathAchievement(final DamageSource source, final EntityCitizen citizen)
    {
        super.triggerDeathAchievement(source, citizen);
        if (source.getEntity() instanceof EntityEnderman)
        {
            citizen.getColony().getStatsManager().triggerAchievement(ModAchievements.achievementGuardDeathEnderman);
        }
    }
}
