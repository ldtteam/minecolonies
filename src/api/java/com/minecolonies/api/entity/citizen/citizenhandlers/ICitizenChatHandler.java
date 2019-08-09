package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.util.DamageSource;

public interface ICitizenChatHandler
{
    /**
     * Sends a localized message from the citizen containing a language string
     * with a key and arguments.
     *
     * @param key  the key to retrieve the string.
     * @param args additional arguments.
     */
    void sendLocalizedChat(String key, Object... args);

    void cleanupChatMessages();

    /**
     * Notify about death of citizen.
     * @param damageSource the damage source.
     */
    void notifyDeath(DamageSource damageSource);
}
