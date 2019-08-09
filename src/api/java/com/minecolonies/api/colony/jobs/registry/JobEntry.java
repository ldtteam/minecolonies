package com.minecolonies.api.colony.jobs.registry;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.jobs.IJob;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.Validate;

import java.util.function.Function;

/**
 * Entry for the {@link IJob} registry.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") //Use the builder to create one.
public final class JobEntry extends IForgeRegistryEntry.Impl<JobEntry> implements IForgeRegistryEntry<JobEntry>
{

    private final Function<ICitizenData, IJob<?>> jobProducer;

    /**
     * Builder for a {@link JobEntry}.
     */
    public static final class Builder
    {
        private Function<ICitizenData, IJob<?>> jobProducer;
        private ResourceLocation                registryName;

        /**
         * Setter the for the producer.
         *
         * @param jobProducer The producer for {@link IJob}.
         * @return The builder.
         */
        public Builder setJobProducer(final Function<ICitizenData, IJob<?>> jobProducer)
        {
            this.jobProducer = jobProducer;
            return this;
        }

        /**
         * Setter for the registry name.
         *
         * @param registryName The registry name.
         * @return The builder.
         */
        public Builder setRegistryName(final ResourceLocation registryName)
        {
            this.registryName = registryName;
            return this;
        }

        /**
         * Creates a new {@link JobEntry} builder.
         *
         * @return The created {@link JobEntry}.
         */
        @SuppressWarnings("PMD.AccessorClassGeneration") //The builder is explicitly allowed to create one.
        public JobEntry createJobEntry()
        {
            Validate.notNull(jobProducer);
            Validate.notNull(registryName);

            return new JobEntry(jobProducer).setRegistryName(registryName);
        }
    }

    /**
     * The producer for the {@link IJob}. Creates the job from a {@link ICitizenData} instance.
     *
     * @return The created {@link IJob}.
     */
    public Function<ICitizenData, IJob<?>> getJobProducer()
    {
        return jobProducer;
    }

    private JobEntry(final Function<ICitizenData, IJob<?>> jobProducer)
    {
        super();
        this.jobProducer = jobProducer;
    }
}
