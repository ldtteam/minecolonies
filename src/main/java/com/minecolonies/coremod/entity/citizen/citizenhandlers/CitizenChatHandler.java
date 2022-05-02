package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenChatHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
            TranslatableComponent contentComponent;
            if (job != null)
            {
                contentComponent = new TranslatableComponent(
                  TranslationConstants.WORKER_DIED,
                  new TranslatableComponent(job.getJobRegistryEntry().getTranslationKey()),
                  citizen.getCitizenData().getName(),
                  Math.round(citizen.getX()),
                  Math.round(citizen.getY()),
                  Math.round(citizen.getZ()),
                  new TranslatableComponent(damageSource.msgId));
            }
            else
            {
                contentComponent = new TranslatableComponent(
                  TranslationConstants.COLONIST_DIED,
                  citizen.getCitizenData().getName(),
                  Math.round(citizen.getX()),
                  Math.round(citizen.getY()),
                  Math.round(citizen.getZ()),
                  new TranslatableComponent(damageSource.msgId));
            }

            MessageUtils.format(contentComponent)
              .with(ChatFormatting.RED)
              .sendTo(citizen.getCitizenColonyHandler().getColony()).forManagers();
        }
    }

    @Override
    public void sendLocalizedChat(final String keyIn, final Object... msg)
    {
        sendLocalizedChat(new TranslatableComponent(keyIn, msg));
    }

    @Override
    public void sendLocalizedChat(Component component)
    {
        if (citizen.getCitizenColonyHandler().getColony() != null)
        {
            final IJob<?> job = citizen.getCitizenJobHandler().getColonyJob();

            MessageUtils.MessageBuilder builder;
            if (job != null)
            {
                builder = MessageUtils.format(job.getJobRegistryEntry().getTranslationKey())
                            .append(new TextComponent(" "))
                            .append(citizen.getCustomName())
                            .append(new TextComponent(": "))
                            .append(component);
            }
            else
            {
                builder = MessageUtils.format(citizen.getCustomName())
                            .append(new TextComponent(": "))
                            .append(component);
            }

            builder.sendTo(citizen.getCitizenColonyHandler().getColony()).forAllPlayers();
        }
    }
}
