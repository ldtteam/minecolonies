package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
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
    public void onServerResponseTriggered(final Component response, final Player player, final ICitizenData data)
    {
        super.onServerResponseTriggered(response, player, data);
        onResponse(response);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClientResponseTriggered(final Component response, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        onResponse(response);
        return super.onClientResponseTriggered(response, player, data, window);
    }

    /**
     * Removes the interaction after a response
     *
     * @param response response
     */
    private void onResponse(final Component response)
    {
        if (response instanceof MutableComponent)
        {
            if (((MutableComponent) response).getKey().equals(INTERACTION_R_OKAY)
                  || ((MutableComponent) response).getKey().equals(INTERACTION_R_IGNORE))
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
