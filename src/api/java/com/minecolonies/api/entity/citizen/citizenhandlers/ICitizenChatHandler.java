package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;

public interface ICitizenChatHandler
{
    /**
     * Notify about death of citizen.
     *
     * @param damageSource the damage source.
     * @param mourn if citizens will mourn.
     * @param graveSpawned if grave spawned to collect.
     */
    void notifyDeath(DamageSource damageSource, final boolean mourn, final boolean graveSpawned);

    /**
     * Sends a localized message from the citizen containing a language string with a key and arguments.
     *
     * @param key  the key to retrieve the string.
     * @param args additional arguments.
     */
    void sendLocalizedChat(String key, Object... args);

    /**
     * Sends a localized message from the citizen containing the completed text component
     *
     * @param component the text component to send to show on the client
     */
    void sendLocalizedChat(Component component);
}
