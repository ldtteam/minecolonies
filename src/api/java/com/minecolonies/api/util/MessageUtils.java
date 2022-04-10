package com.minecolonies.api.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.Level;

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
        if (key == null)
        {
            Log.getLogger().log(Level.ERROR, "Attempting to send player message without a translation key, please report this to the developers!");
            return;
        }
        sendPlayerMessage(player, new TranslationTextComponent(key, args));
    }

    /**
     * Send a message to the given player.
     *
     * @param player    the player.
     * @param component the message component.
     */
    public static void sendPlayerMessage(PlayerEntity player, ITextComponent component)
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
        player.sendMessage(component, player.getUUID());
    }
}
