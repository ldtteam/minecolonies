package com.minecolonies.api.colony.jobs.registry;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.IJobView;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Entry for the {@link IJob} registry.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") //Use the builder to create one.
public final class JobEntry extends ForgeRegistryEntry<JobEntry>
{

    private final Function<ICitizenData, IJob<?>> jobProducer;

    /**
     * Builder for a {@link JobEntry}.
     */
    public static final class Builder
    {
        private Function<ICitizenData, IJob<?>> jobProducer;
        private ResourceLocation                registryName;
        private Supplier<BiFunction<IColonyView, ICitizenDataView, IJobView>> jobViewProducer;

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
         * Sets the callback that is used to create the {@link IJobView} from the {@link IColonyView} and its position in the world.
         *
         * @param jobViewProducer The callback used to create the {@link IJobView}.
         * @return The builder.
         */
        public JobEntry.Builder setJobViewProducer(final Supplier<BiFunction<IColonyView, ICitizenDataView, IJobView>> jobViewProducer)
        {
            this.jobViewProducer = jobViewProducer;
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
            Validate.notNull(jobViewProducer);

            return new JobEntry(jobProducer, jobViewProducer).setRegistryName(registryName);
        }
    }

    private final Supplier<BiFunction<IColonyView, ICitizenDataView, IJobView>> jobViewProducer;

    /**
     * The producer for the {@link IJob}. Creates the job from a {@link ICitizenData} instance.
     *
     * @return The created {@link IJob}.
     */
    public Function<ICitizenData, IJob<?>> getHandlerProducer()
    {
        return jobProducer;
    }

    private JobEntry(
      final Function<ICitizenData, IJob<?>> jobProducer,
      final Supplier<BiFunction<IColonyView, ICitizenDataView, IJobView>> jobViewProducer)
    {
        super();
        this.jobProducer = jobProducer;
        this.jobViewProducer = jobViewProducer;
    }

    public Supplier<BiFunction<IColonyView, ICitizenDataView, IJobView>> getJobViewProducer()
    {
        return jobViewProducer;
    }
}
