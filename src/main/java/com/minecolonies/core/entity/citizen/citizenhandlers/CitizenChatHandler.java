package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenChatHandler;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_MOURN;
import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_GRAVE_SPAWNED;

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

    @Override
    public void notifyDeath(final DamageSource damageSource, final boolean mourn, final boolean graveSpawned)
    {
        if (citizen.getCitizenColonyHandler().getColony() != null && citizen.getCitizenData() != null)
        {
            MessageUtils.format(citizen.getCombatTracker().getDeathMessage())
              .append(Component.literal("! "))
              .append(Component.translatableEscape(TranslationConstants.COLONIST_GRAVE_LOCATION, Math.round(citizen.getX()), Math.round(citizen.getY()), Math.round(citizen.getZ())))
              .append(mourn ? Component.translatableEscape(COM_MINECOLONIES_COREMOD_MOURN, citizen.getCitizenData().getName()) : Component.empty())
              .append(graveSpawned ? Component.translatableEscape(WARNING_GRAVE_SPAWNED) : Component.empty())
              .withPriority(MessagePriority.DANGER)
              .sendTo(citizen.getCitizenColonyHandler().getColony()).forManagers();
        }
    }

    @Override
    public void sendLocalizedChat(final String keyIn, final Object... msg)
    {
        sendLocalizedChat(Component.translatableEscape(keyIn, msg));
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
}
