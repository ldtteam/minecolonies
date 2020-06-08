package com.minecolonies.api.research.factories;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.effects.IResearchEffect;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.PARAMS_RESEARCH_EFFECT;

/**
 * Interface for the IResearchEffectFactory which is responsible for creating and maintaining ResearchEffect objects.
 */
public interface IResearchEffectFactory<T extends IResearchEffect<?>> extends IFactory<FactoryVoidInput, T>
{
    @NotNull
    @Override
    default T getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput token, @NotNull final Object... context)
    {
        if (context.length < PARAMS_RESEARCH_EFFECT)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only 2 are allowed!");
        }

        if(!(context[0] instanceof String))
        {
            throw new IllegalArgumentException("First parameter is supposed to be an ItemStack!");
        }

        if(context[1] == null)
        {
            throw new IllegalArgumentException("Second parameter is supposed to be an Object!");
        }

        final String id = (String) context[0];
        final Object obj = context[1];
        return getNewInstance(id, obj);
    }

    /**
     * Method to get a new Instance of a ResearchEffect.
     * @param id the input.
     * @param obj the grid size.
     * @return a new Instance of ResearchEffect.
     */
    @NotNull
    T getNewInstance(@NotNull final String id, final Object obj);
}

