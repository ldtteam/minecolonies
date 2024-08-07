package com.minecolonies.api.research.factories;

import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.IGlobalResearch;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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

        if (!(context[0] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("First parameter is supposed to be the ID (ResourceLocation)!");
        }

        if (!(context[1] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("Second parameter is supposed to be the branchID (ResourceLocation)!");
        }

        if (!(context[2] instanceof ResourceLocation))
        {
            throw new IllegalArgumentException("Third parameter is supposed to be the parent (ResourceLocation)!");
        }

        if (!(context[3] instanceof TranslatableContents))
        {
            throw new IllegalArgumentException("Fourth parameter is supposed to be the description (Translation Text Component)!");
        }

        if (!(context[4] instanceof Integer))
        {
            throw new IllegalArgumentException("Fifth parameter is supposed to be the Depth (int)!");
        }

        final ResourceLocation id = (ResourceLocation) context[0];
        final ResourceLocation branch = (ResourceLocation) context[1];
        final ResourceLocation parent = (ResourceLocation) context[2];
        final TranslatableContents desc = (TranslatableContents) context[3];
        final int depth = (int) context[4];
        final int sortOrder;
        if(context.length > 5)
        {
            sortOrder = (int) context[5];
        }
        else
        {
            sortOrder = 1;
        }
        final ResourceLocation iconTexture;
        if(context.length > 6)
        {
            iconTexture = (ResourceLocation) context[6];
        }
        else
        {
            iconTexture = ResourceLocation.parse(" : ");
        }
        final ItemStack iconStack;
        if(context.length > 7)
        {
            iconStack = (ItemStack) context[7];
        }
        else
        {
            iconStack = ItemStack.EMPTY;
        }
        final TranslatableContents subtitle;
        if(context.length > 8)
        {
            subtitle = (TranslatableContents) context[8];
        }
        else
        {
            subtitle = new TranslatableContents("", (String) null, TranslatableContents.NO_ARGS);
        }
        final boolean onlyChild;
        final boolean hidden;
        final boolean autostart;
        final boolean instant;
        final boolean immutable;
        if (context.length == 14)
        {
            onlyChild = (boolean) context[9];
            hidden = (boolean) context[10];
            autostart = (boolean) context[11];
            instant = (boolean) context[12];
            immutable = (boolean) context[13];
        }
        else
        {
            onlyChild = false;
            hidden = false;
            autostart = false;
            instant = false;
            immutable = false;
        }
        return getNewInstance(id, branch, parent, desc, depth, sortOrder, iconTexture, iconStack, subtitle, onlyChild, hidden, autostart, instant, immutable);
    }

    /**
     * Method to get a new Instance of a Research.
     *
     * @param id                the id.
     * @param branch            the branch.
     * @param parent            the research's parent, or "" if no parent.
     * @param desc              the description of the research.
     * @param universityLevel   the university tier of the research.
     * @param sortOrder         the sorting order for display of the research in comparison to its siblings.
     * @param iconTexture       the resource location of the icon's texture, if one is present.
     * @param iconStack         the ItemStack for an icon, if one is used.
     * @param subtitle          the optional subtitle description of the research.
     * @param onlyChild         if the research's completion prohibits its siblings from being completed.
     * @param hidden            if the research is visible only when it is eligible for research.
     * @param autostart         if the research attempts to automatically start when eligible, or reports to the player if unable.
     * @param instant           if the research should complete immediately.
     * @param immutable         if the research is locking, and can not be undone once completed.
     * @return a new Instance of Research.
     */
    @NotNull
    IGlobalResearch getNewInstance(final ResourceLocation id, final ResourceLocation branch, final ResourceLocation parent, final TranslatableContents desc, final int universityLevel, final int sortOrder,
      final ResourceLocation iconTexture, final ItemStack iconStack, final TranslatableContents subtitle, final boolean onlyChild,
      final boolean hidden, final boolean autostart, final boolean instant, final boolean immutable);
}
