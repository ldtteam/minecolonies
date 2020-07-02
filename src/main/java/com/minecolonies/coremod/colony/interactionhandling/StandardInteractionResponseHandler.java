package com.minecolonies.coremod.colony.interactionhandling;

import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;

/**
 * The server side interaction response handler.
 */
public class StandardInteractionResponseHandler extends ServerCitizenInteractionResponseHandler
{
    @SuppressWarnings("unchecked")
    private static final Tuple<ITextComponent, ITextComponent>[] tuples = (Tuple<ITextComponent, ITextComponent>[]) new Tuple[] {
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.okay"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.ignore"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.remindmelater"), null)};

    /**
     * The server interaction response handler with custom validator.
     *
     * @param inquiry   the client inquiry.
     * @param validator the id of the validator.
     * @param priority  the interaction priority.
     */
    public StandardInteractionResponseHandler(
      final ITextComponent inquiry,
      final ITextComponent validator,
      final IChatPriority priority)
    {
        super(inquiry, true, priority, InteractionValidatorRegistry.getStandardInteractionValidatorPredicate(validator), validator, tuples);
    }

    /**
     * The server interaction response handler.
     *
     * @param inquiry  the client inquiry.
     * @param priority the interaction priority.
     */
    public StandardInteractionResponseHandler(
      final ITextComponent inquiry,
      final IChatPriority priority)
    {
        super(inquiry, true, priority, InteractionValidatorRegistry.getStandardInteractionValidatorPredicate(inquiry), inquiry, tuples);
    }

    /**
     * Way to load the response handler for a citizen.
     *
     * @param data the citizen owning this handler.
     */
    public StandardInteractionResponseHandler(final ICitizen data)
    {
        super(data);
    }

    @Override
    public List<IInteractionResponseHandler> genChildInteractions()
    {
        return Collections.emptyList();
    }

    @Override
    public String getType()
    {
        return ModInteractionResponseHandlers.STANDARD.getPath();
    }
}
