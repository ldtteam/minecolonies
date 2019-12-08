package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.interactionhandling.*;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.client.gui.WindowCitizen;
import com.minecolonies.coremod.client.gui.WindowRequestDetail;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * The request based interaction response handler.
 */
public class RequestBasedInteractionResponseHandler extends ServerCitizenInteractionResponseHandler
{
    private static final String TOKEN_TAG = "token";

    private static final Tuple[] tuples  = {
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.okay"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.remindmelater"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.cancel"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.fulfill"), null)
    };

    private static final Tuple[] tuplesAsync  = {
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.okay"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.ignore"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.remindmelater"), null)
    };

    /**
     * The request this is related to.
     */
    private IToken token = null;

    /**
     * Specific validator for this one.
     */
    private BiPredicate<ICitizenData, IToken> validator;

    /**
     * The server interaction response handler.
     * @param inquiry the client inquiry.
     * @param priority the interaction priority.
     * @param token the token this is related to.
     */
    public RequestBasedInteractionResponseHandler(
      final ITextComponent inquiry,
      final IChatPriority priority,
      final ITextComponent validator,
      final IToken token)
    {
        super(inquiry, true, priority, null, validator, priority == ChatPriority.BLOCKING ? tuples : tuplesAsync);
        this.validator = InteractionValidatorPredicates.getTokenBasedInteractionValidatorPredicate(validator);
        this.token = token;
    }

    /**
     * The server interaction response handler.
     * @param inquiry the client inquiry.
     * @param priority the interaction priority.
     * @param token the token this is related to.
     */
    public RequestBasedInteractionResponseHandler(
      final ITextComponent inquiry,
      final IChatPriority priority,
      final IToken token)
    {
        super(inquiry, true, priority, null, inquiry, tuples);
        this.validator = InteractionValidatorPredicates.getTokenBasedInteractionValidatorPredicate(inquiry);
        this.token = token;
    }

    /**
     * Way to load the response handler for a citizen.
     * @param data the citizen owning this handler.
     */
    public RequestBasedInteractionResponseHandler(final ICitizen data)
    {
        super(data);
    }

    @Override
    public List<IInteractionResponseHandler> genChildInteractions()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isValid(final ICitizenData citizen)
    {
        return (validator == null && !this.parents.isEmpty()) || ( validator != null && validator.test(citizen, token) );
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = super.serializeNBT();
        tag.put(TOKEN_TAG, StandardFactoryController.getInstance().serialize(token));
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final CompoundNBT compoundNBT)
    {
        super.deserializeNBT(compoundNBT);
        this.token = StandardFactoryController.getInstance().deserialize(compoundNBT.getCompound(TOKEN_TAG));
    }

    @Override
    public boolean onClientResponseTriggered(final ITextComponent response, final World world, final ICitizenDataView data, final Window window)
    {
        if (response.equals(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.fulfill")))
        {
            final IColony colony = IColonyManager.getInstance().getColonyView(data.getColonyId(), world.getDimension().getType().getId());

            if (colony != null)
            {
                final IRequest request = colony.getRequestManager().getRequestForToken(token);
                if (request != null)
                {
                    final WindowCitizen windowCitizen = new WindowCitizen(data);
                    windowCitizen.open();
                    windowCitizen.goToRequestList();

                    final WindowRequestDetail windowRequestDetail = new WindowRequestDetail(windowCitizen, request, data.getColonyId() );
                    windowRequestDetail.open();


                    return false;
                }
            }
        }
        else
        {
            return super.onClientResponseTriggered(response, world, data, window);
        }
        return true;
    }

    @Override
    public void onServerResponseTriggered(final ITextComponent response, final World world, final ICitizenData data)
    {
        super.onServerResponseTriggered(response, world, data);
        if (response.equals(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.cancel")) && data.getColony() != null)
        {
            data.getColony().getRequestManager().updateRequestState(token, RequestState.CANCELLED);
        }
    }

    @Override
    protected void loadValidator()
    {
        this.validator = InteractionValidatorPredicates.getTokenBasedInteractionValidatorPredicate(validatorId);
    }

    @Override
    public String getType()
    {
        return ModInteractionResponseHandlers.REQUEST.getPath();
    }

}
