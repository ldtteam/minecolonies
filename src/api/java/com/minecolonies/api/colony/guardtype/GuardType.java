package com.minecolonies.api.colony.guardtype;

import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;

import java.util.function.Supplier;

/**
 * Guard type class.
 */
public class GuardType
{
    /**
     * The job entry.
     */
    private final Supplier<JobEntry> jobEntry;

    /**
     * Job translation key.
     */
    private final String jobTranslationKey;

    /**
     * Primary skill.
     */
    private final Skill primarySkill;

    /**
     * Secondary skill.
     */
    private final Skill secondarySkill;

    /**
     * Constructor to create the type.
     *
     * @param jobEntry             the job entry..
     * @param jobTranslationKey    job translation key.
     * @param buttonTranslationKey button translation ky.
     * @param primarySkill         primary skill.
     * @param secondarySkill       secondary skill.
     * @param workerSoundName      worker sound name.
     * @param clazz                  the class of the job.
     */
    public GuardType(
      final Supplier<JobEntry> jobEntry,
      final String jobTranslationKey,
      final Skill primarySkill,
      final Skill secondarySkill)
    {
        super();
        this.jobEntry = jobEntry;
        this.jobTranslationKey = jobTranslationKey;
        this.primarySkill = primarySkill;
        this.secondarySkill = secondarySkill;
    }

    /**
     * Getter for the job producer.
     *
     * @return the producer.
     */
    public Supplier<JobEntry> getJobEntry()
    {
        return jobEntry;
    }

    /**
     * Getter for the job translation key.
     *
     * @return the key.
     */
    public String getJobTranslationKey()
    {
        return jobTranslationKey;
    }

    /**
     * Getter for the primary key.
     *
     * @return the skill.
     */
    public Skill getPrimarySkill()
    {
        return primarySkill;
    }

    /**
     * Getter for the secondary skill.
     *
     * @return the skill.
     */
    public Skill getSecondarySkill()
    {
        return secondarySkill;
    }

    /**
     * The builder.
     */
    public static class Builder
    {
        private Supplier<JobEntry> jobEntry;
        private String             jobTranslationKey;
        private Skill              primarySkill;
        private Skill              secondarySkill;

        public Builder setJobEntry(final Supplier<JobEntry> jobEntry)
        {
            this.jobEntry = jobEntry;
            return this;
        }

        public Builder setJobTranslationKey(final String jobTranslationKey)
        {
            this.jobTranslationKey = jobTranslationKey;
            return this;
        }

        public Builder setPrimarySkill(final Skill primarySkill)
        {
            this.primarySkill = primarySkill;
            return this;
        }

        public Builder setSecondarySkill(final Skill secondarySkill)
        {
            this.secondarySkill = secondarySkill;
            return this;
        }

        public GuardType createGuardType()
        {
            return new GuardType(jobEntry, jobTranslationKey, primarySkill, secondarySkill);
        }
    }
}
