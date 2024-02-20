package com.minecolonies.core.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.core.client.gui.visitor.expeditionary.MainWindowExpeditionary;
import com.minecolonies.core.colony.expeditions.ExpeditionVisitorMember;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionEvent;
import com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.List;

/**
 * Interaction for expeditionary visitors.
 */
public class ExpeditionaryInteraction extends ServerCitizenInteraction
{
    /**
     * The view GUI answer.
     */
    private static final Tuple<Component, Component> viewAnswer = new Tuple<>(Component.translatable("com.minecolonies.coremod.gui.chat.recruit"), null);

    /**
     * The return answer.
     */
    private static final Tuple<Component, Component> returnAnswer = new Tuple<>(Component.translatable("com.minecolonies.coremod.gui.chat.notnow"), null);

    /**
     * Default constructor.
     *
     * @param inquiry  the inquiry text.
     * @param priority the chat priority.
     */
    public ExpeditionaryInteraction(final Component inquiry, final IChatPriority priority)
    {
        super(inquiry, true, priority, d -> true, null, viewAnswer, returnAnswer);
    }

    /**
     * Initializer constructor.
     *
     * @param data the input citizen data.
     */
    public ExpeditionaryInteraction(final ICitizen data)
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
        return ModInteractionResponseHandlers.EXPEDITIONARY.getPath();
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

        return !response.equals(returnAnswer);
    }

    @Override
    public boolean isValid(final ICitizenData citizen)
    {
        if (!super.isValid(citizen))
        {
            return false;
        }

        if (!(citizen instanceof IVisitorData visitorData))
        {
            return false;
        }

        for (final IColonyEvent event : citizen.getColony().getEventManager().getEvents().values())
        {
            if (event instanceof ColonyExpeditionEvent expeditionEvent)
            {
                for (final IExpeditionMember member : expeditionEvent.getExpedition().getMembers())
                {
                    // If the leader of the input expedition is already present as part of another expedition, we can remove this interaction.
                    if (new ExpeditionVisitorMember(visitorData).equals(member))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}