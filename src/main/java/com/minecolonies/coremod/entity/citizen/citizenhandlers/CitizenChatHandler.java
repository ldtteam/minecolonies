package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenChatHandler;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;

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
            MessageUtils.format("")
                    .with(ChatFormatting.RED)
                    .append(citizen.getCombatTracker().getDeathMessage())
                    .append(Component.literal("! "))
                    .append(Component.translatable(TranslationConstants.COLONIST_GRAVE_LOCATION,
                            Math.round(citizen.getX()),
                            Math.round(citizen.getY()),
                            Math.round(citizen.getZ())))
                    .sendTo(citizen.getCitizenColonyHandler().getColony()).forManagers();
        }
    }

    @Override
    public void sendLocalizedChat(final String keyIn, final Object... msg)
    {
        sendLocalizedChat(Component.translatable(keyIn, msg));
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
                            .append(Component.literal(" "))
                            .append(citizen.getCustomName())
                            .append(Component.literal(": "))
                            .append(component);
            }
            else
            {
                builder = MessageUtils.format(citizen.getCustomName())
                            .append(Component.literal(": "))
                            .append(component);
            }

            builder.sendTo(citizen.getCitizenColonyHandler().getColony()).forAllPlayers();
        }
    }

    private static boolean isAlphanumeric(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }
}
