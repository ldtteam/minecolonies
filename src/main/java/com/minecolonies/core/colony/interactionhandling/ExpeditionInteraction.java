package com.minecolonies.core.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.visitor.expeditionary.MainWindowExpeditionary;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType;
import com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType;
import com.minecolonies.core.network.messages.server.colony.InteractionResponse;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.ExpeditionConstants.*;

/**
 * Interaction for expeditionary visitors.
 */
public class ExpeditionInteraction extends ServerCitizenInteraction
{
    /**
     * The view GUI answer.
     */
    private static final Tuple<Component, Component> viewAnswer = new Tuple<>(Component.translatable(EXPEDITION_INTERACTION_RESPONSE_VIEW), null);

    /**
     * The return answer.
     */
    private static final Tuple<Component, Component> returnAnswer = new Tuple<>(Component.translatable(EXPEDITION_INTERACTION_RESPONSE_NOT_NOW), null);

    /**
     * The cancel answer.
     */
    private static final Tuple<Component, Component> cancelAnswer = new Tuple<>(Component.translatable(EXPEDITION_INTERACTION_RESPONSE_NOT_INTERESTED), null);

    /**
     * Default constructor.
     *
     * @param expeditionType the type of expedition to send.
     */
    public ExpeditionInteraction(final ColonyExpeditionType expeditionType)
    {
        super(Component.translatable(EXPEDITION_INTERACTION_INQUIRY, expeditionType.getToText()),
          true,
          ChatPriority.IMPORTANT,
          data -> data instanceof IVisitorData visitorData && visitorData.getVisitorType() instanceof ExpeditionaryVisitorType,
          null,
          viewAnswer,
          returnAnswer,
          cancelAnswer);
    }

    /**
     * Initializer constructor.
     *
     * @param data the input citizen data.
     */
    public ExpeditionInteraction(final ICitizen data)
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
        return ModInteractionResponseHandlers.EXPEDITION.getPath();
    }

    @Override
    public void onServerResponseTriggered(final int responseId, final Player player, final ICitizenData data)
    {
        super.onServerResponseTriggered(responseId, player, data);
        final Component response = getPossibleResponses().get(responseId);
        if (response.equals(cancelAnswer.getA()) && data instanceof IVisitorData visitorData)
        {
            data.getColony().getVisitorManager().removeCivilian(visitorData);
        }
    }

    @Override
    public boolean onClientResponseTriggered(final int responseId, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        final Component response = getPossibleResponses().get(responseId);
        if (response.equals(viewAnswer.getA()) && data instanceof IVisitorViewData visitorData && visitorData.getVisitorType() instanceof ExpeditionaryVisitorType)
        {
            final MainWindowExpeditionary windowExpeditionary = new MainWindowExpeditionary(visitorData);
            windowExpeditionary.open();
            return false;
        }
        else if (response.equals(cancelAnswer.getA()))
        {
            window.setParentView(null);
            Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), this.getInquiry(), responseId));
            return true;
        }

        return true;
    }
}