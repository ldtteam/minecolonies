package com.minecolonies.core.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpedition;
import com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITION_INTERACTION_RESPONSE_NOT_NOW;
import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITION_INTERACTION_RESPONSE_VIEW_RESULTS;

/**
 * Interaction for expeditionary visitors.
 */
public class ExpeditionFinishedInteraction extends ServerCitizenInteraction
{
    /**
     * The view GUI answer.
     */
    private static final Tuple<Component, Component> viewAnswer = new Tuple<>(Component.translatable(EXPEDITION_INTERACTION_RESPONSE_VIEW_RESULTS), null);

    /**
     * The return answer.
     */
    private static final Tuple<Component, Component> returnAnswer = new Tuple<>(Component.translatable(EXPEDITION_INTERACTION_RESPONSE_NOT_NOW), null);

    /**
     * Default constructor.
     *
     * @param expedition the expedition that was just completed.
     */
    public ExpeditionFinishedInteraction(final ColonyExpedition expedition)
    {
        super(Component.empty(),
          true,
          ChatPriority.IMPORTANT,
          data -> data instanceof IVisitorData visitorData && visitorData.getVisitorType() instanceof ExpeditionaryVisitorType,
          null,
          viewAnswer,
          returnAnswer);
    }

    /**
     * Initializer constructor.
     *
     * @param data the input citizen data.
     */
    public ExpeditionFinishedInteraction(final ICitizen data)
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
    public boolean onClientResponseTriggered(final int responseId, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        final Component response = getPossibleResponses().get(responseId);
        if (response.equals(viewAnswer.getA()))
        {
            Network.getNetwork().sendToServer(new OpenInventoryMessage(data.getColony(), data.getName(), data.getId()));
            return false;
        }

        return true;
    }
}