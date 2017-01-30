package com.minecolonies.coremod.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

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
        sendPlayerMessage(player, key, args);
    }

    /**
     * Send a message to the player.
     * @param player the player to send to.
     * @param key the key of the message.
     * @param message the message to send.
     */
    public static void sendPlayerMessage(@NotNull final EntityPlayer player, final String key, final Object... message)
    {
        TextComponentTranslation translation = null;

        int onlyArgsUntil = 0;
        for (final Object object : message)
        {
            if (object instanceof ITextComponent || object instanceof TextComponentTranslation)
            {
                if(onlyArgsUntil == 0)
                {
                    onlyArgsUntil = -1;
                }
                break;
            }
            onlyArgsUntil++;
        }

        if (onlyArgsUntil >= 0)
        {
            final Object[] args = new Object[onlyArgsUntil];
            for(int i = 0; i < onlyArgsUntil; i++)
            {
                args[i] = message[i];
            }
            translation = new TextComponentTranslation(key, args);
        }

        for (final Object object : message)
        {
            if (translation == null)
            {
                if (object instanceof ITextComponent)
                {
                    translation = new TextComponentTranslation(key);
                }
                else
                {
                    translation = new TextComponentTranslation(key, object);
                    continue;
                }
            }
            if (object instanceof ITextComponent)
            {
                translation.appendSibling((ITextComponent) object);
            }
            else if (object instanceof String)
            {
                boolean isInArgs = false;
                for(Object obj: translation.getFormatArgs())
                {
                    if(obj.equals(object))
                    {
                        isInArgs = true;
                    }
                }
                if(!isInArgs)
                {
                    translation.appendText((String) object);
                }
            }
        }

        if (translation == null)
        {
            translation = new TextComponentTranslation(key);
        }

        player.sendMessage(translation);
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
        final String result;
        if(args.length == 0)
        {
            result = new TextComponentTranslation(key).getUnformattedText();
        }
        else
        {
            result = new TextComponentTranslation(key, args).getUnformattedText();
        }
        return result.isEmpty() ? key : result;
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
        sendPlayersMessage(players, key, args);
    }

    /**
     * Send message to a list of players.
     *
     * @param players the list of players.
     * @param key key of the message.
     * @param message the message.
     */
    public static void sendPlayersMessage(@Nullable final List<EntityPlayer> players, final String key, final Object... message)
    {
        if (players == null || players.isEmpty())
        {
            return;
        }
        for (@NotNull final EntityPlayer player : players)
        {
            sendPlayerMessage(player, key, message);
        }
    }
}
