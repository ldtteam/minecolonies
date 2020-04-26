package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenChatHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Set;

/**
 * The citizen chat handler which handles all possible notifications (blocking or not).
 */
public class CitizenChatHandler implements ICitizenChatHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final EntityCitizen citizen;
    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenChatHandler(final EntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Notify about death of citizen.
     * @param damageSource the damage source.
     */
    @Override
    public void notifyDeath(final DamageSource damageSource)
    {
        if (citizen.getCitizenColonyHandler().getColony() != null && citizen.getCitizenData() != null)
        {
            final IJob job = citizen.getCitizenJobHandler().getColonyJob();
            if (job != null)
            {
                final ITextComponent component = new TextComponentTranslation(
                  "tile.blockhuttownhall.messageworkerdead",
                  new TextComponentTranslation(job.getName()),
                  citizen.getCitizenData().getName(),
                  (int) citizen.posX,
                  (int) citizen.posY,
                  (int) citizen.posZ, damageSource.damageType);
                LanguageHandler.sendPlayersMessage(citizen.getCitizenColonyHandler().getColony().getImportantMessageEntityPlayers(), component.getUnformattedComponentText());
            }
            else
            {
                LanguageHandler.sendPlayersMessage(
                  citizen.getCitizenColonyHandler().getColony().getImportantMessageEntityPlayers(),
                  "tile.blockhuttownhall.messagecolonistdead",
                  citizen.getCitizenData().getName(), (int) citizen.posX, (int) citizen.posY, (int) citizen.posZ, damageSource.damageType);
            }
        }
    }

    @Override
    public void sendLocalizedChat(final String key, final Object... msg)
    {
        if (msg == null)
        {
            return;
        }

        final TextComponentTranslation requiredItem;

        if (msg.length == 0)
        {
            requiredItem = new TextComponentTranslation(key);
        }
        else
        {
            requiredItem = new TextComponentTranslation(key, msg);
        }

        final TextComponentString citizenDescription = new TextComponentString(" ");
        citizenDescription.appendText(citizen.getCustomNameTag()).appendText(": ");
        if (citizen.getCitizenColonyHandler().getColony() != null)
        {
            final TextComponentString colonyDescription = new TextComponentString(" at " + citizen.getCitizenColonyHandler().getColony().getName() + ":");
            final Set<EntityPlayer> players = citizen.getCitizenColonyHandler().getColony().getMessageEntityPlayers();
            final EntityPlayer owner = ServerUtils.getPlayerFromUUID(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.getCitizenColonyHandler().getColony().getPermissions().getOwner());

            if (owner != null)
            {
                players.remove(owner);
                LanguageHandler.sendPlayerMessage(owner,
                  citizen.getCitizenJobHandler().getColonyJob() == null ? "" : citizen.getCitizenJobHandler().getColonyJob().getName(), citizenDescription, requiredItem);
            }

            LanguageHandler.sendPlayersMessage(players,
              citizen.getCitizenJobHandler().getColonyJob() == null ? "" : citizen.getCitizenJobHandler().getColonyJob().getName(), colonyDescription, citizenDescription, requiredItem);
        }
    }
}
