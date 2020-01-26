package com.minecolonies.api.research.factories;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.*;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.PARAMS_RESEARCH;

/**
 * Interface for the IResearchFactory which is responsible for creating and maintaining Research objects.
 */
public interface IResearchFactory extends IFactory<FactoryVoidInput, IResearch>
{
    @NotNull
    @Override
    default IResearch getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput token, @NotNull final Object... context)
    {
        if (context.length < PARAMS_RESEARCH)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only 2 are allowed!");
        }

        if(!(context[0] instanceof String))
        {
            throw new IllegalArgumentException("First parameter is supposed to be the String ID!");
        }

        if(!(context[1] instanceof String))
        {
            throw new IllegalArgumentException("Second parameter is supposed to be the IResearch parent!");
        }

        if(!(context[2] instanceof String))
        {
            throw new IllegalArgumentException("Third parameter is supposed to be the Branch (String)!");
        }

        if(!(context[3] instanceof String))
        {
            throw new IllegalArgumentException("Fourth parameter is supposed to be the Desc (String)!");
        }

        if(!(context[4] instanceof Integer))
        {
            throw new IllegalArgumentException("Fifth parameter is supposed to be the Depth (int)!");
        }

        if(!(context[5] instanceof IResearchEffect))
        {
            throw new IllegalArgumentException("Last parameter is supposed to be the IResearchEffect!");
        }

        final String id = (String) context[0];
        final String parent = (String) context[1];
        final String branch = (String) context[2];
        final String desc = (String) context[3];
        final int depth = (int) context[4];
        final IResearchEffect effect = (IResearchEffect) context[5];
        return getNewInstance(id, parent, branch, desc, depth, effect);
    }

    /**
     * Method to get a new Instance of a Research.
     *
     * @param id the id.
     * @param parent the parent.
     * @param branch the branch.
     * @param desc the description.
     * @param depth the depth.
     * @param effect the effect.
     * @return a new Instance of Research.
     */
    @NotNull
    IResearch getNewInstance(final String id, final String parent, final String branch, @NotNull final String desc, final int depth, final IResearchEffect effect);
}

