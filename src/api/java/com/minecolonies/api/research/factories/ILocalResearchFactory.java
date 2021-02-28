package com.minecolonies.api.research.factories;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.ILocalResearch;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.PARAMS_LOCAL_RESEARCH;

/**
 * Interface for the IResearchFactory which is responsible for creating and maintaining Research objects.
 */
public interface ILocalResearchFactory extends IFactory<FactoryVoidInput, ILocalResearch>
{
    @NotNull
    @Override
    default ILocalResearch getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput token, @NotNull final Object... context)
    {
        if (context.length < PARAMS_LOCAL_RESEARCH)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only " + PARAMS_LOCAL_RESEARCH + " are allowed!");
        }

        if (!(context[0] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("First parameter is supposed to be the Research ID!");
        }

        if (!(context[2] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("Third parameter is supposed to be the Branch (String)!");
        }

        if (!(context[4] instanceof Integer))
        {
            throw new IllegalArgumentException("Fifth parameter is supposed to be the Depth (int)!");
        }

        final ResourceLocation id = (ResourceLocation) context[0];
        final ResourceLocation branch = (ResourceLocation) context[2];
        final int depth = (int) context[4];
        return getNewInstance(id, branch, depth);
    }

    /**
     * Method to get a new Instance of a Research.
     *
     * @param id     the id.
     * @param branch the branch.
     * @param depth  the depth.
     * @return a new Instance of Research.
     */
    @NotNull
    ILocalResearch getNewInstance(final ResourceLocation id, final ResourceLocation branch, final int depth);
}
