package com.minecolonies.api.research.factories;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.IGlobalResearch;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.PARAMS_GLOBAL_RESEARCH;

/**
 * Interface for the IResearchFactory which is responsible for creating and maintaining Research objects.
 */
public interface IGlobalResearchFactory extends IFactory<FactoryVoidInput, IGlobalResearch>
{
    @NotNull
    @Override
    default IGlobalResearch getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput token, @NotNull final Object... context)
    {
        if (context.length < PARAMS_GLOBAL_RESEARCH)
        {
            throw new IllegalArgumentException("Unsupported context - Not correct number of parameters. Only " + PARAMS_GLOBAL_RESEARCH + " are allowed!");
        }

        if (!(context[0] instanceof final ResourceLocation id))
        {
            throw new IllegalArgumentException("Parameter 1 is supposed to be the ID property (ResourceLocation)!");
        }

        if (!(context[1] instanceof final ResourceLocation parent))
        {
            throw new IllegalArgumentException("Parameter 2 is supposed to be the parent property (ResourceLocation)!");
        }

        if (!(context[2] instanceof final ResourceLocation branch))
        {
            throw new IllegalArgumentException("Parameter 3 is supposed to be the branch property (ResourceLocation)!");
        }

        if (!(context[3] instanceof final TranslatableContents name))
        {
            throw new IllegalArgumentException("Parameter 4 is supposed to be the name property (Translation Text Component)!");
        }

        if (!(context[4] instanceof final TranslatableContents subtitle))
        {
            throw new IllegalArgumentException("Parameter 5 is supposed to be the subtitle property (Translation Text Component)!");
        }

        if (!(context[5] instanceof Integer depth))
        {
            throw new IllegalArgumentException("Parameter 6 is supposed to be the depth property (int)!");
        }

        if (!(context[6] instanceof Integer sortOrder))
        {
            throw new IllegalArgumentException("Parameter 7 is supposed to be the sort order property (int)!");
        }

        if (!(context[7] instanceof Boolean onlyChild))
        {
            throw new IllegalArgumentException("Parameter 8 is supposed to be the only child property (boolean)!");
        }

        if (!(context[8] instanceof Boolean hidden))
        {
            throw new IllegalArgumentException("Parameter 9 is supposed to be the hidden property (boolean)!");
        }

        if (!(context[9] instanceof Boolean autostart))
        {
            throw new IllegalArgumentException("Parameter 10 is supposed to be the autostart property (boolean)!");
        }

        if (!(context[10] instanceof Boolean instant))
        {
            throw new IllegalArgumentException("Parameter 11 is supposed to be the instant property (boolean)!");
        }

        if (!(context[11] instanceof Boolean immutable))
        {
            throw new IllegalArgumentException("Parameter 12 is supposed to be the immutable property (boolean)!");
        }
        return getNewInstance(id, parent, branch, name, subtitle, depth, sortOrder, onlyChild, hidden, autostart, instant, immutable);
    }

    /**
     * Method to get a new Instance of research.
     *
     * @param id        its id.
     * @param parent    the research's parent, if one is present, or null if not.
     * @param branch    the branch it is on.
     * @param name      the optional name of the research. If empty, a key generated from the id will be used instead.
     * @param subtitle  the optional short description of the research, in plaintext or as a translation key.
     * @param depth     the depth in the tree.
     * @param sortOrder the relative vertical order of the research's display, in relation to its siblings.
     * @param onlyChild if the research allows only one child research to be completed.
     * @param hidden    if the research is only visible when eligible to be researched.
     * @param autostart if the research should begin automatically, or notify the player, when it is eligible.
     * @param instant   if the research should be completed instantly (ish) from when begun.
     * @param immutable if the research can not be reset once unlocked.
     * @return a new instance of research.
     */
    @NotNull IGlobalResearch getNewInstance(
        final ResourceLocation id,
        final ResourceLocation parent,
        final ResourceLocation branch,
        final TranslatableContents name,
        final TranslatableContents subtitle,
        final int depth,
        final int sortOrder,
        final boolean onlyChild,
        final boolean hidden,
        final boolean autostart,
        final boolean instant,
        final boolean immutable);
}
