package com.minecolonies.api.colony.guardtype;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.jobs.IJob;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;

public class GuardType extends ForgeRegistryEntry<GuardType>
{

    private final Function<ICitizenData, IJob> guardJobProducer;
    private final String                       jobTranslationKey;
    private final String                       buttonTranslationKey;
    private final IBuildingWorker.Skill        primarySkill;
    private final IBuildingWorker.Skill        secondarySkill;
    private final String                       workerSoundName;

    public GuardType(
      final Function<ICitizenData, IJob> guardJobProducer,
      final String jobTranslationKey,
      final String buttonTranslationKey,
      final IBuildingWorker.Skill primarySkill, final IBuildingWorker.Skill secondarySkill, final String workerSoundName)
    {
        super();
        this.guardJobProducer = guardJobProducer;
        this.jobTranslationKey = jobTranslationKey;
        this.buttonTranslationKey = buttonTranslationKey;
        this.primarySkill = primarySkill;
        this.secondarySkill = secondarySkill;
        this.workerSoundName = workerSoundName;
    }

    public Function<ICitizenData, IJob> getGuardJobProducer()
    {
        return guardJobProducer;
    }

    public String getJobTranslationKey()
    {
        return jobTranslationKey;
    }

    public String getButtonTranslationKey()
    {
        return buttonTranslationKey;
    }

    public IBuildingWorker.Skill getPrimarySkill()
    {
        return primarySkill;
    }

    public IBuildingWorker.Skill getSecondarySkill()
    {
        return secondarySkill;
    }

    public String getWorkerSoundName()
    {
        return workerSoundName;
    }

    public static class Builder
    {
        private Function<ICitizenData, IJob> guardJobProducer;
        private String                       jobTranslationKey;
        private String                       buttonTranslationKey;
        private IBuildingWorker.Skill        primarySkill;
        private IBuildingWorker.Skill        secondarySkill;
        private String                       workerSoundName;
        private ResourceLocation             registryName;

        public Builder setGuardJobProducer(final Function<ICitizenData, IJob> guardJobProducer)
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

        public Builder setPrimarySkill(final IBuildingWorker.Skill primarySkill)
        {
            this.primarySkill = primarySkill;
            return this;
        }

        public Builder setSecondarySkill(final IBuildingWorker.Skill secondarySkill)
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
