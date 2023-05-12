package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers.SIMPLE_NOTIFICATION;

/**
 * A simple interaction which displays until an acceptable response is clicked
 */
public class SimpleNotificationInteraction extends StandardInteraction
{
    /**
     * Whether this interaction is active
     */
    private boolean active = true;

    public SimpleNotificationInteraction(
      final Component inquiry,
      final IChatPriority priority)
    {
        super(inquiry, null, priority);
    }

    @Override
    public void onServerResponseTriggered(final int responseId, final Player player, final ICitizenData data)
    {
        super.onServerResponseTriggered(responseId, player, data);
        onResponse(responseId);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClientResponseTriggered(final int responseId, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        onResponse(responseId);
        return super.onClientResponseTriggered(responseId, player, data, window);
    }

    /**
     * Removes the interaction after a response
     *
     * @param responseId response
     */
    private void onResponse(final int responseId)
    {
        final Component response = getPossibleResponses().get(responseId);
        if (response.getContents() instanceof TranslatableContents)
        {
            if (((TranslatableContents) response.getContents()).getKey().equals(INTERACTION_R_OKAY)
                  || ((TranslatableContents) response.getContents()).getKey().equals(INTERACTION_R_IGNORE))
            {
                active = false;
            }
        }
    }

    @Override
    public String getType()
    {
        return SIMPLE_NOTIFICATION.getPath();
    }

    @Override
    public boolean isValid(final ICitizenData citizen)
    {
        return active;
    }
}
