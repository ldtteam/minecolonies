package com.minecolonies.api.research.factories;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.IGlobalResearch;
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

        if (!(context[0] instanceof String))
        {
            throw new IllegalArgumentException("First parameter is supposed to be the ID (String)!");
        }

        if (!(context[1] instanceof String))
        {
            throw new IllegalArgumentException("Second parameter is supposed to be the branchID (String)!");
        }

        if (!(context[2] instanceof String))
        {
            throw new IllegalArgumentException("Third parameter is supposed to be the parent (String)!");
        }

        if (!(context[3] instanceof String))
        {
            throw new IllegalArgumentException("Fourth parameter is supposed to be the description (String)!");
        }

        if (!(context[4] instanceof Integer))
        {
            throw new IllegalArgumentException("Fifth parameter is supposed to be the Depth (int)!");
        }

        final String id = (String) context[0];
        final String branch = (String) context[1];
        final String parent = (String) context[2];
        final String desc = (String) context[3];
        final int depth = (int) context[4];
        final String icon;
        if(context.length > 5)
        {
            icon = (String) context[5];
        }
        else
        {
            icon = "";
        }
        final String subtitle;
        if(context.length > 6)
        {
            subtitle = (String) context[6];
        }
        else
        {
            subtitle = "";
        }
        final boolean onlyChild;
        final boolean hidden;
        final boolean autostart;
        final boolean instant;
        final boolean immutable;
        if (context.length == 12)
        {
            onlyChild = (boolean) context[7];
            hidden = (boolean) context[8];
            autostart = (boolean) context[9];
            instant = (boolean) context[10];
            immutable = (boolean) context[11];
        }
        else
        {
            onlyChild = false;
            hidden = false;
            autostart = false;
            instant = false;
            immutable = false;
        }
        return getNewInstance(id, branch, parent, desc, depth, icon, subtitle, onlyChild, hidden, autostart, instant, immutable);
    }

    /**
     * Method to get a new Instance of a Research.
     *
     * @param id                the id.
     * @param branch            the branch.
     * @param parent            the research's parent, or "" if no parent.
     * @param desc              the description of the research.
     * @param universityLevel   the university tier of the research.
     * @param icon              the string of the icon's characteristics.
     * @param subtitle          the optional subtitle description of the research.
     * @param onlyChild         if the research's completion prohibits its siblings from being completed.
     * @param hidden            if the research is visible only when it is eligible for research.
     * @param autostart         if the research attempts to automatically start when eligible, or reports to the player if unable.
     * @param instant           if the research should complete immediately.
     * @param immutable         if the research is locking, and can not be undone once completed.
     * @return a new Instance of Research.
     */
    @NotNull
    IGlobalResearch getNewInstance(final String id, final String branch, final String parent, final String desc, final int universityLevel,
      final String icon,final String subtitle, final boolean onlyChild, final boolean hidden, final boolean autostart, final boolean instant, final boolean immutable);
}
