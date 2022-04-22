package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;

public interface ICitizenChatHandler
{
    /**
     * Notify about death of citizen.
     *
     * @param damageSource the damage source.
     */
    void notifyDeath(DamageSource damageSource);

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
    void sendLocalizedChat(ITextComponent component);
}
