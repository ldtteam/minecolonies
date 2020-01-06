package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

public interface ICitizenChatHandler
{
    /**
     * Notify about death of citizen.
     * @param damageSource the damage source.
     */
    void notifyDeath(DamageSource damageSource);

    /**
     * Sends a localized message from the citizen containing a language string
     * with a key and arguments.
     *
     * @param key  the key to retrieve the string.
     * @param args additional arguments.
     */
    void sendLocalizedChat(String key, Object... args);
}
