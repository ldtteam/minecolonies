package com.minecolonies.api.colony.interactionhandling.registry;

import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import org.apache.commons.lang3.Validate;

import java.util.function.Function;

/**
 * Entry for the {@link IInteractionResponseHandler} registry.
 */
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass") //Use the builder to create one.
public final class InteractionResponseHandlerEntry
{
    private final Function<ICitizen, IInteractionResponseHandler> responseHandlerProducer;

    /**
     * Builder for a {@link InteractionResponseHandlerEntry}.
     */
    public static final class Builder
    {
        private Function<ICitizen, IInteractionResponseHandler> responseHandlerProducer;

        /**
         * Setter the for the producer.
         *
         * @param responseHandlerProducer The producer for {@link IInteractionResponseHandler}.
         * @return The builder.
         */
        public Builder setResponseHandlerProducer(final Function<ICitizen, IInteractionResponseHandler> responseHandlerProducer)
        {
            this.responseHandlerProducer = responseHandlerProducer;
            return this;
        }

        /**
         * Creates a new {@link InteractionResponseHandlerEntry} builder.
         *
         * @return The created {@link InteractionResponseHandlerEntry}.
         */
        @SuppressWarnings("PMD.AccessorClassGeneration") //The builder is explicitly allowed to create one.
        public InteractionResponseHandlerEntry createEntry()
        {
            Validate.notNull(responseHandlerProducer);

            return new InteractionResponseHandlerEntry(responseHandlerProducer);
        }
    }

    /**
     * The producer for the {@link IInteractionResponseHandler}. Creates the interaction from a {@link ICitizenData} instance.
     *
     * @return The created {@link IInteractionResponseHandler}.
     */
    public Function<ICitizen, IInteractionResponseHandler> getProducer()
    {
        return responseHandlerProducer;
    }

    private InteractionResponseHandlerEntry(final Function<ICitizen, IInteractionResponseHandler> producer)
    {
        super();
        this.responseHandlerProducer = producer;
    }
}
