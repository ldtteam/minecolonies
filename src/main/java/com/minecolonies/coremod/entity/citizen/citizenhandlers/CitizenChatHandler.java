package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenChatHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;

public class CitizenChatHandler implements ICitizenChatHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final EntityCitizen citizen;

    /**
     * The Status message of the citizen. (What he is up to currently).
     */
    @NotNull
    private final Map<String, Integer> statusMessages = new HashMap<>();

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
     * Sends a localized message from the citizen containing a language string
     * with a key and arguments.
     *
     * @param key  the key to retrieve the string.
     * @param args additional arguments.
     */
    @Override
    public void sendLocalizedChat(final String key, final Object... args)
    {
        sendChat(key, args);
    }

    /**
     * Sends a chat string close to the citizen.
     *
     * @param msg the message string.
     */
    private void sendChat(final String keyIn, @Nullable final Object... msg)
    {
        final String key = keyIn.toLowerCase(Locale.US);
        if (msg == null || statusMessages.containsKey(key))
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
            statusMessages.put(key + msg[0], citizen.ticksExisted);
            requiredItem = new TranslationTextComponent(key, msg);
        }

        final ITextComponent citizenDescription = new StringTextComponent(citizen.getCustomName().getFormattedText());
        if (citizen.getCitizenColonyHandler().getColony() != null)
        {
            final StringTextComponent colonyDescription = new StringTextComponent(" at " + citizen.getCitizenColonyHandler().getColony().getName() + ": ");
            final List<PlayerEntity> players = new ArrayList<>(citizen.getCitizenColonyHandler().getColony().getMessagePlayerEntitys());
            final PlayerEntity owner = ServerUtils.getPlayerFromUUID(CompatibilityUtils.getWorldFromCitizen(citizen), citizen.getCitizenColonyHandler().getColony().getPermissions().getOwner());

            if (owner != null)
            {
                players.remove(owner);
                LanguageHandler.sendPlayerMessage(owner,
                  citizen.getCitizenJobHandler().getColonyJob() == null ? "" : citizen.getCitizenJobHandler().getColonyJob().getName(), citizenDescription, requiredItem);
            }

            LanguageHandler.sendPlayersMessage(players,
              citizen.getCitizenJobHandler().getColonyJob() == null ? "" : citizen.getCitizenJobHandler().getColonyJob().getName(), citizenDescription, colonyDescription, requiredItem);
        }
    }

    @Override
    public void cleanupChatMessages()
    {
        //Only check if there are messages and once a second
        if (!statusMessages.isEmpty() && citizen.ticksExisted % TICKS_20 == 0)
        {
            statusMessages.entrySet().removeIf(stringIntegerEntry -> citizen.ticksExisted - stringIntegerEntry.getValue() > TICKS_20 * MineColonies.getConfig().getCommon().chatFrequency.get());
        }
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
                final ITextComponent component = new TranslationTextComponent("block.blockhuttownhall.messageworkerdead", new TranslationTextComponent(job.getName()), citizen.getCitizenData().getName(), (int) citizen.posX, (int) citizen.posY, (int) citizen.posZ, damageSource.damageType);
                LanguageHandler.sendPlayersMessage(
                  citizen.getCitizenColonyHandler().getColony().getMessagePlayerEntitys(), component.getUnformattedComponentText());
            }
            else
            {
                LanguageHandler.sendPlayersMessage(
                  citizen.getCitizenColonyHandler().getColony().getMessagePlayerEntitys(),
                  "block.blockhuttownhall.messagecolonistdead",
                  citizen.getCitizenData().getName(), (int) citizen.posX, (int) citizen.posY, (int) citizen.posZ, damageSource.damageType);
            }
        }
    }
}
