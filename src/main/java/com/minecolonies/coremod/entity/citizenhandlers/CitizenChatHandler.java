package com.minecolonies.coremod.entity.citizenhandlers;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;

public class CitizenChatHandler
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
    public void sendLocalizedChat(final String key, final Object... args)
    {
        sendChat(key, args);
    }

    /**
     * Sends a chat string close to the citizen.
     *
     * @param msg the message string.
     */
    private void sendChat(final String key, @Nullable final Object... msg)
    {
        if (msg == null || statusMessages.containsKey(key))
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
            statusMessages.put(key + msg[0], citizen.ticksExisted);
            requiredItem = new TextComponentTranslation(key, msg);
        }

        final TextComponentString citizenDescription = new TextComponentString(" ");
        citizenDescription.appendText(citizen.getCustomNameTag()).appendText(": ");
        if (citizen.getCitizenColonyHandler().getColony() != null)
        {
            final TextComponentString colonyDescription = new TextComponentString(" at " + citizen.getCitizenColonyHandler().getColony().getName() + ":");
            final List<EntityPlayer> players = new ArrayList<>(citizen.getCitizenColonyHandler().getColony().getMessageEntityPlayers());
            final EntityPlayer owner = ServerUtils.getPlayerFromUUID(CompatibilityUtils.getWorld(citizen), citizen.getCitizenColonyHandler().getColony().getPermissions().getOwner());

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

    public void cleanupChatMessages()
    {
        //Only check if there are messages and once a second
        if (statusMessages.size() > 0 && citizen.ticksExisted % TICKS_20 == 0)
        {
            statusMessages.entrySet().removeIf(stringIntegerEntry -> citizen.ticksExisted - stringIntegerEntry.getValue() > TICKS_20 * Configurations.gameplay.chatFrequency);
        }
    }

    /**
     * Notify about death of citizen.
     * @param damageSource the damage source.
     */
    public void notifyDeath(final DamageSource damageSource)
    {
        if (citizen.getCitizenColonyHandler().getColony() != null && citizen.getCitizenData() != null)
        {
            if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard)
            {
                LanguageHandler.sendPlayersMessage(
                  citizen.getCitizenColonyHandler().getColony().getMessageEntityPlayers(),
                  "tile.blockHutTownHall.messageGuardDead",
                  citizen.getCitizenData().getName(), (int) citizen.posX, (int) citizen.posY, (int) citizen.posZ, damageSource.damageType);
            }
            else
            {
                LanguageHandler.sendPlayersMessage(
                  citizen.getCitizenColonyHandler().getColony().getMessageEntityPlayers(),
                  "tile.blockHutTownHall.messageColonistDead",
                  citizen.getCitizenData().getName(), (int) citizen.posX, (int) citizen.posY, (int) citizen.posZ, damageSource.damageType);
            }
        }
    }
}
