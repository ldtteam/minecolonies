package com.minecolonies.coremod.colony.jobs;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.entity.ai.citizen.research.EntityAIWorkResearcher;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_CURR_MANA;

/**
 * Job class of the researcher.
 */
public class JobResearch extends AbstractJob<EntityAIWorkResearcher, JobResearch>
{
    /**
     * Max mana at level 99 in seconds.
     */
    private static final int MANA_PER_SECOND = (4 * 60 * 60) / 100;

    /**
     * How often the max tickrate per second is executed.
     */
    private static final int MAX_TICKRATE_PER_SECOND = MAX_TICKRATE/TICKS_SECOND;

    /**
     * The amount of Mana the researcher stored additionally.
     * Up to Mana Level.
     */
    private int currentMana = 0;

    //when the colony was offline and goes into online state, we compare how long the colony was offline, then we take the speed in which they level (level 99 = almost realtime) and fill up mana up to mana level (max 99 = 4h).
    // then with this AI we tick randomly the research additionally (up to twice the rate until currentMana is depleted.
    /**
     * Public constructor of the researcher job.
     *
     * @param entity the entity to assign to the job.
     */
    public JobResearch(final ICitizenData entity)
    {
        super(entity);
    }

    @Override
    public JobEntry getJobRegistryEntry()
    {
        return ModJobs.researcher;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "com.minecolonies.coremod.job.researcher";
    }

    @NotNull
    @Override
    public BipedModelType getModel()
    {
        return BipedModelType.STUDENT;
    }

    /**
     * Override to add Job-specific AI tasks to the given EntityAITask list.
     */
    @NotNull
    @Override
    public EntityAIWorkResearcher generateAI()
    {
        return new EntityAIWorkResearcher(this);
    }

    /**
     * Get the current mana filling.
     * @return the current quantity.
     */
    public int getCurrentMana()
    {
        return currentMana;
    }

    @Override
    public void processOfflineTime(final long time)
    {
        super.processOfflineTime(time);

        final int maxMana = getCitizen().getCitizenSkillHandler().getLevel(Skill.Mana);
        final int speed = getCitizen().getCitizenSkillHandler().getLevel(Skill.Knowledge);

        final long currentMaxManaInSeconds = MANA_PER_SECOND * maxMana;
        final long newMana = (time / 100) * speed;

        final long both = newMana + (this.currentMana * MAX_TICKRATE_PER_SECOND);
        final long result = Math.min(both, currentMaxManaInSeconds);

        this.currentMana = (int) (result / MAX_TICKRATE_PER_SECOND);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compoundNBT = super.serializeNBT();
        compoundNBT.putInt(TAG_CURR_MANA, this.currentMana);
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        this.currentMana = compound.getInt(TAG_CURR_MANA);
    }

    /**
     * Reduce the current mana by one.
     */
    public void reduceCurrentMana()
    {
        this.currentMana--;
    }
}
