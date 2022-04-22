package com.minecolonies.api.util;

import com.minecolonies.api.colony.IColony;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.*;
import org.apache.logging.log4j.Level;

import java.util.Collection;
import java.util.Collections;

/**
 * Simple class for containing reusable player messaging logic.
 */
public class MessageUtils
{
    /**
     * Send a message to the given player.
     *
     * @param player the player.
     * @param key    the translation component key.
     * @param args   the additional args to send in the translation component.
     */
    public static void sendPlayerMessage(PlayerEntity player, String key, Object... args)
    {
        sendPlayerMessageWithColor(player, TextFormatting.WHITE, new TranslationTextComponent(key, args));
    }

    /**
     * Send a message to the given player.
     *
     * @param player    the player.
     * @param component the message component.
     */
    public static void sendPlayerMessage(PlayerEntity player, ITextComponent component)
    {
        sendPlayerMessageWithColor(player, TextFormatting.WHITE, component);
    }

    /**
     * Send a message to the given player.
     *
     * @param player the player.
     * @param color  the colour of the message.
     * @param key    the translation component key.
     * @param args   the additional args to send in the translation component.
     */
    public static void sendPlayerMessageWithColor(PlayerEntity player, TextFormatting color, String key, Object... args)
    {
        sendPlayerMessageWithColor(player, color, new TranslationTextComponent(key, args));
    }

    /**
     * Send a message to the given player.
     *
     * @param player    the player.
     * @param color     the colour of the message.
     * @param component the message component.
     */
    public static void sendPlayerMessageWithColor(PlayerEntity player, TextFormatting color, ITextComponent component)
    {
        sendMessageInternal(player, component, Collections.singletonList(color));
    }

    /**
     * Send a message to the given player.
     *
     * @param player the player.
     * @param colony the colony which sent the message.
     * @param key    the translation component key.
     * @param args   the additional args to send in the translation component.
     */
    public static void sendPlayerColonyMessage(PlayerEntity player, IColony colony, String key, Object... args)
    {
        sendPlayerColonyMessageWithColor(player, colony, TextFormatting.WHITE, new TranslationTextComponent(key, args));
    }

    /**
     * Send a message to the given player.
     *
     * @param player    the player.
     * @param colony    the colony which sent the message.
     * @param component the message component.
     */
    public static void sendPlayerColonyMessage(PlayerEntity player, IColony colony, ITextComponent component)
    {
        sendPlayerColonyMessageWithColor(player, colony, TextFormatting.WHITE, component);
    }

    /**
     * Send a message to the given player.
     *
     * @param player the player.
     * @param colony the colony which sent the message.
     * @param key    the translation component key.
     * @param args   the additional args to send in the translation component.
     */
    public static void sendPlayerColonyMessageWithColor(PlayerEntity player, IColony colony, TextFormatting color, String key, Object... args)
    {
        sendPlayerColonyMessageWithColor(player, colony, color, new TranslationTextComponent(key, args));
    }

    /**
     * Send a message to the given player.
     *
     * @param player    the player.
     * @param colony    the colony which sent the message.
     * @param component the message component.
     */
    public static void sendPlayerColonyMessageWithColor(PlayerEntity player, IColony colony, TextFormatting color, ITextComponent component)
    {
        ITextComponent fullComponent = component;

        if (colony != null && !colony.isCoordInColony(player.level, player.blockPosition()))
        {
            fullComponent = new StringTextComponent("[" + colony.getName() + "] ")
                              .withStyle(TextFormatting.WHITE)
                              .append(component);
        }

        sendMessageInternal(player, fullComponent, Collections.singletonList(color));
    }

    /**
     * Internal message sending handler.
     *
     * @param player     the player to send to.
     * @param component  the text component to send.
     * @param formatters a collection of formatter to apply to the root component.
     */
    private static void sendMessageInternal(PlayerEntity player, ITextComponent component, Collection<TextFormatting> formatters)
    {
        if (player == null)
        {
            Log.getLogger().log(Level.ERROR, "Attempting to send player message without passing a player, please report this to the developers!");
            return;
        }
        if (component == null)
        {
            Log.getLogger().log(Level.ERROR, "Attempting to send player message without passing a message component, please report this to the developers!");
            return;
        }

        if (component instanceof IFormattableTextComponent)
        {
            for (TextFormatting formatter : formatters)
            {
                ((IFormattableTextComponent) component).withStyle(formatter);
            }
        }

        player.sendMessage(component, player.getUUID());
    }
}
