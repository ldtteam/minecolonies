package com.minecolonies.util;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.util.List;

/**
 * Created by Colton on 5/12/2014.
 */
public class LanguageHandler
{
    private static String getString(String key)
    {
        return LanguageRegistry.instance().getStringLocalization(key);
    }

    public static String format(String key, Object... args)
    {
        return String.format(getString(key), args);
    }

    private static void sendPlayerMessage(EntityPlayer player, String message)
    {
        player.addChatComponentMessage(new ChatComponentText(message));
    }

    public static void sendPlayerLocalizedMessage(EntityPlayer player, String key, Object... args)
    {
        sendPlayerMessage(player, format(key, args));
    }

    private static void sendPlayersMessage(List<EntityPlayer> players, String message)
    {
        for (EntityPlayer player : players)
        {
            sendPlayerMessage(player, message);
        }
    }

    public static void sendPlayersLocalizedMessage(List<EntityPlayer> players, String key, Object... args)
    {
        sendPlayersMessage(players, format(key, args));
    }
}
