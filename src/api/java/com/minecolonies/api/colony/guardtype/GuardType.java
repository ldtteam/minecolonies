package com.minecolonies.api.colony.guardtype;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import net.minecraft.resources.ResourceLocation;

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
     * Button translation key.
     */
    private final String buttonTranslationKey;

    /**
     * Primary skill.
     */
    private final Skill primarySkill;

    /**
     * Secondary skill.
     */
    private final Skill secondarySkill;

    /**
     * Worker sound name.
     */
    private final String workerSoundName;

    /**
     * The class of the job.
     */
    private final Class<IJob<?>> clazz;

    /**
     * Unique registry name.
     */
    private final ResourceLocation registryName;

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
      final String buttonTranslationKey,
      final Skill primarySkill,
      final Skill secondarySkill,
      final String workerSoundName,
      final Class<IJob<?>> clazz,
      final ResourceLocation registryName)
    {
        super();
        this.jobEntry = jobEntry;
        this.jobTranslationKey = jobTranslationKey;
        this.buttonTranslationKey = buttonTranslationKey;
        this.primarySkill = primarySkill;
        this.secondarySkill = secondarySkill;
        this.workerSoundName = workerSoundName;
        this.clazz = clazz;
        this.registryName = registryName;
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
     * Getter for the button translation key.
     *
     * @return the key.
     */
    public String getButtonTranslationKey()
    {
        return buttonTranslationKey;
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
     * Getter for the worker sound name.
     *
     * @return the sound name.
     */
    public String getWorkerSoundName()
    {
        return workerSoundName;
    }

    /**
     * Check if the job is of this type.
     * @param job the job to check.
     * @return true if so.
     */
    public boolean isInstance(final IJob<?> job)
    {
        return this.clazz.isInstance(job);
    }

    /**
     * The builder.
     */
    public static class Builder
    {
        private Supplier<JobEntry> jobEntry;
        private String             jobTranslationKey;
        private String                          buttonTranslationKey;
        private Skill                           primarySkill;
        private Skill                           secondarySkill;
        private String                          workerSoundName;
        private ResourceLocation                registryName;
        private Class<IJob<?>>                  clazz;

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

        public Builder setButtonTranslationKey(final String buttonTranslationKey)
        {
            this.buttonTranslationKey = buttonTranslationKey;
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

        public Builder setWorkerSoundName(final String workerSoundName)
        {
            this.workerSoundName = workerSoundName;
            return this;
        }

        public Builder setRegistryName(final ResourceLocation registryName)
        {
            this.registryName = registryName;
            return this;
        }

        public Builder setClazz(final Class clazz)
        {
            this.clazz = clazz;
            return this;
        }

        public GuardType createGuardType()
        {
            return new GuardType(jobEntry, jobTranslationKey, buttonTranslationKey, primarySkill, secondarySkill, workerSoundName, clazz, registryName);
        }
    }
}
