package com.minecolonies.api.colony.guardtype;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;

/**
 * Guard type class.
 */
public class GuardType extends ForgeRegistryEntry<GuardType>
{
    /**
     * Producer function.
     */
    private final Function<ICitizenData, IJob<?>> guardJobProducer;

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
     * Constructor to create the type.
     * @param guardJobProducer the producer.
     * @param jobTranslationKey job translation key.
     * @param buttonTranslationKey button translation ky.
     * @param primarySkill primary skill.
     * @param secondarySkill secondary skill.
     * @param workerSoundName worker sound name.
     */
    public GuardType(
      final Function<ICitizenData, IJob<?>> guardJobProducer,
      final String jobTranslationKey,
      final String buttonTranslationKey,
      final Skill primarySkill, final Skill secondarySkill, final String workerSoundName)
    {
        super();
        this.guardJobProducer = guardJobProducer;
        this.jobTranslationKey = jobTranslationKey;
        this.buttonTranslationKey = buttonTranslationKey;
        this.primarySkill = primarySkill;
        this.secondarySkill = secondarySkill;
        this.workerSoundName = workerSoundName;
    }

    /**
     * Getter for the job producer.
     * @return the producer.
     */
    public Function<ICitizenData, IJob<?>> getGuardJobProducer()
    {
        return guardJobProducer;
    }

    /**
     * Getter for the job translation key.
     * @return the key.
     */
    public String getJobTranslationKey()
    {
        return jobTranslationKey;
    }

    /**
     * Getter for the button translation key.
     * @return the key.
     */
    public String getButtonTranslationKey()
    {
        return buttonTranslationKey;
    }

    /**
     * Getter for the primary key.
     * @return the skill.
     */
    public Skill getPrimarySkill()
    {
        return primarySkill;
    }

    /**
     * Getter for the secondary skill.
     * @return the skill.
     */
    public Skill getSecondarySkill()
    {
        return secondarySkill;
    }

    /**
     * Getter for the worker sound name.
     * @return the sound name.
     */
    public String getWorkerSoundName()
    {
        return workerSoundName;
    }

    /**
     * The builder.
     */
    public static class Builder
    {
        private Function<ICitizenData, IJob<?>> guardJobProducer;
        private String                          jobTranslationKey;
        private String                          buttonTranslationKey;
        private Skill                           primarySkill;
        private Skill                           secondarySkill;
        private String                          workerSoundName;
        private ResourceLocation                registryName;

        public Builder setGuardJobProducer(final Function<ICitizenData, IJob<?>> guardJobProducer)
        {
            this.guardJobProducer = guardJobProducer;
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

        public GuardType createGuardType()
        {
            return new GuardType(guardJobProducer, jobTranslationKey, buttonTranslationKey, primarySkill, secondarySkill, workerSoundName).setRegistryName(registryName);
        }
    }
}
