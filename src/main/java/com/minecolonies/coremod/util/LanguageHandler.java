package com.minecolonies.coremod.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Helper class for localization and sending player messages.
 *
 * @author Colton
 */
public final class LanguageHandler
{
    /**
     * Private constructor to hide implicit one.
     */
    private LanguageHandler()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Send a localized and formatted message to a player.
     *
     * @param player player to send the message to.
     * @param key    unlocalized key.
     * @param args   Objects for String.format().
     */
    public static void sendPlayerLocalizedMessage(@NotNull final EntityPlayer player, final String key, final Object... args)
    {
        sendPlayerMessage(player, format(key, args));
    }

    /**
     * Send a message to the player.
     * @param player the player to send to.
     * @param message the message to send.
     */
    public static void sendPlayerMessage(@NotNull final EntityPlayer player, final String message)
    {
        player.sendMessage(new TextComponentString(message));
    }

    /**
     * Localize a string and use String.format().
     *
     * @param key  unlocalized key.
     * @param args Objects for String.format().
     * @return Localized string.
     */
    public static String format(final String key, final Object... args)
    {
        return String.format(getString(key), args);
    }

    /**
     * Localize a non-formatted string.
     *
     * @param key unlocalized key.
     * @return Localized string.
     */
    public static String getString(final String key)
    {
        return getString(key, key);
    }

    /**
     * Localize a non-formatted string.
     *
     * @param key          unlocalized key.
     * @param defaultValue the value to return if no key is found.
     * @return Localized string.
     */
    @SuppressWarnings("deprecation")
    public static String getString(final String key, final String defaultValue)
    {
        //todo: use TextComponentTranslation like mojang wants us to
        //using fully qualified name to remove deprecation warning on import
        return net.minecraft.util.text.translation.I18n.translateToLocal(key);
    }

    /**
     * Send a localized and formatted message to multiple players.
     *
     * @param players EntityPlayers to send the message to.
     * @param key     unlocalized key.
     * @param args    Objects for String.format().
     */
    public static void sendPlayersLocalizedMessage(final List<EntityPlayer> players, final String key, final Object... args)
    {
        sendPlayersMessage(players, format(key, args));
    }

    /**
     * Send message to a list of players.
     *
     * @param players the list of players.
     * @param message the message.
     */
    public static void sendPlayersMessage(@Nullable final List<EntityPlayer> players, final String message)
    {
        if (players == null || players.isEmpty())
        {
            return;
        }
        for (@NotNull final EntityPlayer player : players)
        {
            sendPlayerMessage(player, message);
        }
    }
}
