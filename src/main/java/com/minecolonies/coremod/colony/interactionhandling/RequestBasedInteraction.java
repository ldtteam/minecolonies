package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.interactionhandling.*;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.client.gui.WindowRequestDetail;
import com.minecolonies.coremod.client.gui.citizen.RequestWindowCitizen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

import static com.minecolonies.coremod.colony.interactionhandling.StandardInteraction.*;

/**
 * The request based interaction response handler.
 */
public class RequestBasedInteraction extends ServerCitizenInteraction
{
    private static final String TOKEN_TAG = "token";

    @SuppressWarnings("unchecked")
    private static final Tuple<Component, Component>[] tuples = (Tuple<Component, Component>[]) new Tuple[] {
      new Tuple<>(Component.translatable(INTERACTION_R_OKAY), null),
      new Tuple<>(Component.translatable(INTERACTION_R_REMIND), null),
      new Tuple<>(Component.translatable("com.minecolonies.coremod.gui.chat.cancel"), null),
      new Tuple<>(Component.translatable("com.minecolonies.coremod.gui.chat.fulfill"), null)};

    @SuppressWarnings("unchecked")
    private static final Tuple<Component, Component>[] tuplesAsync = (Tuple<Component, Component>[]) new Tuple[] {
      new Tuple<>(Component.translatable(INTERACTION_R_OKAY), null),
      new Tuple<>(Component.translatable(INTERACTION_R_IGNORE), null),
      new Tuple<>(Component.translatable(INTERACTION_R_REMIND), null),
      new Tuple<>(Component.translatable(INTERACTION_R_SKIP), null)};

    /**
     * The request this is related to.
     */
    private IToken<?> token = null;

    /**
     * Specific validator for this one.
     */
    private BiPredicate<ICitizenData, IToken<?>> validator;

    /**
     * The server interaction response handler.
     *
     * @param inquiry   the client inquiry.
     * @param priority  the interaction priority.
     * @param token     the token this is related to.
     * @param validator the validator id.
     */
    public RequestBasedInteraction(
      final Component inquiry,
      final IChatPriority priority,
      final Component validator,
      final IToken<?> token)
    {
        super(inquiry, true, priority, null, validator, priority == ChatPriority.BLOCKING ? tuples : tuplesAsync);
        this.validator = InteractionValidatorRegistry.getTokenBasedInteractionValidatorPredicate(validator);
        this.token = token;
    }

    /**
     * The server interaction response handler.
     *
     * @param inquiry  the client inquiry.
     * @param priority the interaction priority.
     * @param token    the token this is related to.
     */
    public RequestBasedInteraction(
      final Component inquiry,
      final IChatPriority priority,
      final IToken<?> token)
    {
        super(inquiry, true, priority, null, inquiry, tuples);
        this.validator = InteractionValidatorRegistry.getTokenBasedInteractionValidatorPredicate(inquiry);
        this.token = token;
    }

    /**
     * Way to load the response handler for a citizen.
     *
     * @param data the citizen owning this handler.
     */
    public RequestBasedInteraction(final ICitizen data)
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
        return (validator == null && !this.parents.isEmpty()) || (validator != null && validator.test(citizen, token));
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = super.serializeNBT();
        tag.put(TOKEN_TAG, StandardFactoryController.getInstance().serialize(token));
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final CompoundTag compoundNBT)
    {
        super.deserializeNBT(compoundNBT);
        this.token = StandardFactoryController.getInstance().deserialize(compoundNBT.getCompound(TOKEN_TAG));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onWindowOpened(final BOWindow window, final ICitizenDataView dataView)
    {
        final IColony colony = IColonyManager.getInstance().getColonyView(dataView.getColonyId(), Minecraft.getInstance().player.level.dimension());

        if (colony != null)
        {
            final IRequest<?> request = colony.getRequestManager().getRequestForToken(token);
            if (request != null)
            {
                final View group = window.findPaneOfTypeByID("interactionView", View.class);
                ItemIcon icon = window.findPaneOfTypeByID("request_item", ItemIcon.class);
                if (icon == null)
                {
                    icon = new ItemIcon();
                    group.addChild(icon);
                }

                icon.setID("request_item");
                icon.setSize(32, 32);
                if (!request.getDisplayStacks().isEmpty())
                {
                    icon.setItem((request.getDisplayStacks().get(0)));
                }
                icon.setPosition(30, 60);
                icon.setVisible(true);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClientResponseTriggered(final int responseId, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        if (((TranslatableContents) getPossibleResponses().get(responseId).getContents()).getKey().equals("com.minecolonies.coremod.gui.chat.fulfill"))
        {
            final IColony colony = IColonyManager.getInstance().getColonyView(data.getColonyId(), player.level.dimension());

            if (colony != null)
            {
                final IRequest<?> request = colony.getRequestManager().getRequestForToken(token);
                if (request != null)
                {
                    final RequestWindowCitizen windowCitizen = new RequestWindowCitizen(data);
                    windowCitizen.open();

                    final WindowRequestDetail windowRequestDetail = new WindowRequestDetail(windowCitizen, request, data.getColonyId());
                    windowRequestDetail.open();


                    return false;
                }
            }
        }
        else
        {
            return super.onClientResponseTriggered(responseId, player, data, window);
        }
        return true;
    }

    @Override
    public void onServerResponseTriggered(final int responseId, final Player player, final ICitizenData data)
    {
        super.onServerResponseTriggered(responseId, player, data);
        final Component response = getPossibleResponses().get(responseId);
        if (response.equals(Component.translatable("com.minecolonies.coremod.gui.chat.cancel")) && data.getColony() != null)
        {
            data.getColony().getRequestManager().updateRequestState(token, RequestState.CANCELLED);
        }
    }

    @Override
    protected void loadValidator()
    {
        this.validator = InteractionValidatorRegistry.getTokenBasedInteractionValidatorPredicate(validatorId);
    }

    @Override
    public String getType()
    {
        return ModInteractionResponseHandlers.REQUEST.getPath();
    }
}
