package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenChatHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The citizen chat handler which handles all possible notifications (blocking or not).
 */
public class CitizenChatHandler implements ICitizenChatHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final AbstractEntityCitizen citizen;

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenChatHandler(final AbstractEntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Notify about death of citizen.
     *
     * @param damageSource the damage source.
     */
    @Override
    public void notifyDeath(final DamageSource damageSource)
    {
        if (citizen.getCitizenColonyHandler().getColony() != null && citizen.getCitizenData() != null)
        {
            final IJob<?> job = citizen.getCitizenJobHandler().getColonyJob();
            if (job != null)
            {
                final Component component = new TranslatableComponent(
                  "block.blockhuttownhall.messageworkerdead",
                  new TranslatableComponent(job.getName().toLowerCase()),
                  citizen.getCitizenData().getName(),
                  (int) citizen.getX(), (int) citizen.getY(),
                  (int) citizen.getZ(), new TranslatableComponent(damageSource.msgId));
                LanguageHandler.sendPlayersMessage(citizen.getCitizenColonyHandler().getColony().getImportantMessageEntityPlayers(), "", component);
            }
            else
            {
                LanguageHandler.sendPlayersMessage(
                  citizen.getCitizenColonyHandler().getColony().getImportantMessageEntityPlayers(), "",
                  new TranslatableComponent("block.blockhuttownhall.messagecolonistdead",
                    citizen.getCitizenData().getName(), (int) citizen.getX(), (int) citizen.getY(),
                    (int) citizen.getZ(), new TranslatableComponent(damageSource.msgId)));
            }
        }
    }

    @Override
    public void sendLocalizedChat(final String keyIn, final Object... msg)
    {
        final String key = keyIn.toLowerCase(Locale.US);
        if (msg == null)
        {
            return;
        }

        final TranslatableComponent requiredItem;

        if (msg.length == 0)
        {
            requiredItem = new TranslatableComponent(key);
        }
        else
        {
            requiredItem = new TranslatableComponent(key, msg);
        }

        final Component citizenDescription = new TextComponent(citizen.getCustomName().getString());
        if (citizen.getCitizenColonyHandler().getColony() != null)
        {
            final TextComponent colonyDescription = new TextComponent(" at " + citizen.getCitizenColonyHandler().getColony().getName() + ": ");
            final List<Player> players = new ArrayList<>(citizen.getCitizenColonyHandler().getColony().getMessagePlayerEntities());
            final Player owner = ServerUtils.getPlayerFromUUID(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.getCitizenColonyHandler().getColony().getPermissions().getOwner());

            if (owner != null)
            {
                players.remove(owner);
                LanguageHandler.sendPlayerMessage(owner,
                  citizen.getCitizenJobHandler().getColonyJob() == null ? "" : citizen.getCitizenJobHandler().getColonyJob().getName(), new TextComponent(" "), citizenDescription, requiredItem);
            }

            LanguageHandler.sendPlayersMessage(players,
              citizen.getCitizenJobHandler().getColonyJob() == null ? "" : citizen.getCitizenJobHandler().getColonyJob().getName(), new TextComponent(" "),
              citizenDescription,
              colonyDescription,
              requiredItem);
        }
    }
}
