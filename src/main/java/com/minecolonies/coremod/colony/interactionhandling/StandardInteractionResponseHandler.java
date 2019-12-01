package com.minecolonies.coremod.colony.interactionhandling;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorPredicates;
import com.minecolonies.api.colony.interactionhandling.ServerCitizenInteractionResponseHandler;
import com.minecolonies.api.util.Tuple;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * The server side interaction response handler.
 */
public class StandardInteractionResponseHandler extends ServerCitizenInteractionResponseHandler
{
    private static final Tuple[] tuples = {
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.okay"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.ignore"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.remindmelater"), null)
    };

    /**
     * The server interaction response handler with custom validator.
     * @param inquiry the client inquiry.
     * @param validator the id of the validator.
     * @param priority the interaction priority.
     */
    public StandardInteractionResponseHandler(
      final ITextComponent inquiry,
      final ITextComponent validator,
      final ChatPriority priority)
    {
        super(inquiry, true, priority, InteractionValidatorPredicates.map.getOrDefault(validator, null), validator, tuples);
    }

    /**
     * The server interaction response handler.
     * @param inquiry the client inquiry.
     * @param priority the interaction priority.
     */
    public StandardInteractionResponseHandler(
      final ITextComponent inquiry,
      final ChatPriority priority)
    {
        super(inquiry, true, priority, InteractionValidatorPredicates.map.getOrDefault(inquiry, null), inquiry, tuples);
    }


    /**
     * The inquiry of the citizen from NBT.
     * @param compoundNBT the compound to deserialize it from.
     */
    public StandardInteractionResponseHandler(@NotNull final CompoundNBT compoundNBT)
    {
        super(compoundNBT);
    }

    /**
     * Way to load the response handler for a citizen.
     * @param data the citizen owning this handler.
     */
    public StandardInteractionResponseHandler(final ICitizenData data)
    {
        super(data);
    }

    @Override
    public List<IInteractionResponseHandler> genChildInteractions()
    {
        return Collections.emptyList();
    }
}
