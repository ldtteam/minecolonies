package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenChatHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.*;

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
            TranslationTextComponent contentComponent;
            if (job != null)
            {
                contentComponent = new TranslationTextComponent(
                  TranslationConstants.WORKER_DIED,
                  new TranslationTextComponent(job.getJobRegistryEntry().getTranslationKey()),
                  citizen.getCitizenData().getName(),
                  Math.round(citizen.getX()),
                  Math.round(citizen.getY()),
                  Math.round(citizen.getZ()),
                  new TranslationTextComponent(damageSource.msgId));
            }
            else
            {
                contentComponent = new TranslationTextComponent(
                  TranslationConstants.COLONIST_DIED,
                  citizen.getCitizenData().getName(),
                  Math.round(citizen.getX()),
                  Math.round(citizen.getY()),
                  Math.round(citizen.getZ()),
                  new TranslationTextComponent(damageSource.msgId));
            }
            citizen.getCitizenColonyHandler().getColony().notifyColonyManagers(contentComponent.setStyle(Style.EMPTY.withColor(TextFormatting.RED)));
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

        final TranslationTextComponent requiredItem;

        if (msg.length == 0)
        {
            requiredItem = new TranslationTextComponent(key);
        }
        else
        {
            requiredItem = new TranslationTextComponent(key, msg);
        }

        sendLocalizedChat(requiredItem);
    }

    @Override
    public void sendLocalizedChat(ITextComponent component)
    {
        final ITextComponent citizenDescription = new StringTextComponent(citizen.getCustomName().getString());
        if (citizen.getCitizenColonyHandler().getColony() != null)
        {
            final StringTextComponent colonyDescription = new StringTextComponent(" at " + citizen.getCitizenColonyHandler().getColony().getName() + ": ");
            final List<PlayerEntity> players = new ArrayList<>(citizen.getCitizenColonyHandler().getColony().getMessagePlayerEntities());
            final PlayerEntity owner = ServerUtils.getPlayerFromUUID(
              CompatibilityUtils.getWorldFromCitizen(citizen),
              citizen.getCitizenColonyHandler().getColony().getPermissions().getOwner());

            if (owner != null)
            {
                players.remove(owner);
                LanguageHandler.sendPlayerMessage(owner,
                  citizen.getCitizenJobHandler().getColonyJob() == null ? "" : citizen.getCitizenJobHandler().getColonyJob().getJobRegistryEntry().getTranslationKey(),
                  new StringTextComponent(" "),
                  citizenDescription,
                  component);
            }

            LanguageHandler.sendPlayersMessage(players,
              citizen.getCitizenJobHandler().getColonyJob() == null ? "" : citizen.getCitizenJobHandler().getColonyJob().getJobRegistryEntry().getTranslationKey(),
              new StringTextComponent(" "),
              citizenDescription,
              colonyDescription,
              component);
        }
    }
}
