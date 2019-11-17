package com.minecolonies.api.colony.interactionhandling.registry;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.lang3.Validate;

import java.util.function.Function;

/**
 * Entry for the {@link IInteractionResponseHandler} registry.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") //Use the builder to create one.
public final class InteractionResponseHandlerEntry extends ForgeRegistryEntry<InteractionResponseHandlerEntry> implements IForgeRegistryEntry<InteractionResponseHandlerEntry>
{

    private final Function<ICitizenData, IInteractionResponseHandler> jobProducer;

    /**
     * Builder for a {@link InteractionResponseHandlerEntry}.
     */
    public static final class Builder
    {
        private Function<ICitizenData, IInteractionResponseHandler> jobProducer;
        private ResourceLocation                                       registryName;

        /**
         * Setter the for the producer.
         *
         * @param jobProducer The producer for {@link IInteractionResponseHandler}.
         * @return The builder.
         */
        public Builder setJobProducer(final Function<ICitizenData, IInteractionResponseHandler> jobProducer)
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
         * Creates a new {@link InteractionResponseHandlerEntry} builder.
         *
         * @return The created {@link InteractionResponseHandlerEntry}.
         */
        @SuppressWarnings("PMD.AccessorClassGeneration") //The builder is explicitly allowed to create one.
        public InteractionResponseHandlerEntry createJobEntry()
        {
            Validate.notNull(jobProducer);
            Validate.notNull(registryName);

            return new InteractionResponseHandlerEntry(jobProducer).setRegistryName(registryName);
        }
    }

    /**
     * The producer for the {@link IInteractionResponseHandler}. Creates the job from a {@link ICitizenData} instance.
     *
     * @return The created {@link IInteractionResponseHandler}.
     */
    public Function<ICitizenData, IInteractionResponseHandler> getProducer()
    {
        return jobProducer;
    }

    private InteractionResponseHandlerEntry(final Function<ICitizenData, IInteractionResponseHandler> jobProducer)
    {
        super();
        this.jobProducer = jobProducer;
    }
}
