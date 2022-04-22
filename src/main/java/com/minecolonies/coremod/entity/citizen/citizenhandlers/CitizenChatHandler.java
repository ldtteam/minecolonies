package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenChatHandler;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.*;

import java.util.Objects;

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
            citizen.getCitizenColonyHandler().getColony().notifyColonyManagers(contentComponent, TextFormatting.RED);
        }
    }

    @Override
    public void sendLocalizedChat(final String keyIn, final Object... msg)
    {
        sendLocalizedChat(new TranslationTextComponent(keyIn, msg));
    }

    @Override
    public void sendLocalizedChat(ITextComponent component)
    {
        if (citizen.getCitizenColonyHandler().getColony() != null)
        {
            final IJob<?> job = citizen.getCitizenJobHandler().getColonyJob();

            IFormattableTextComponent fullComponent;
            if (job != null)
            {
                fullComponent = new StringTextComponent("")
                                  .append(new TranslationTextComponent(job.getJobRegistryEntry().getTranslationKey()))
                                  .append(" ")
                                  .append(Objects.requireNonNull(citizen.getCustomName()))
                                  .append(": ")
                                  .append(component);
            }
            else
            {
                fullComponent = new StringTextComponent("")
                                  .append(Objects.requireNonNull(citizen.getCustomName()))
                                  .append(": ")
                                  .append(component);
            }

            citizen.getCitizenColonyHandler().getColony().notifyColonyMembers(fullComponent);
        }
    }
}
