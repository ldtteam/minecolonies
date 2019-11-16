package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

public interface ICitizenChatHandler
{
    /**
     * Sends a localized message from the citizen containing a language string
     * with a key and arguments.
     *
     * @param key  the key to retrieve the string.
     * @param blocking if it is blocking or if it is just a general message.
     * @param worldTick the world tick that is set up.
     * @param args additional arguments.
     */
    void sendLocalizedChat(final String key, final boolean blocking, final int worldTick, final Object... args);

    /**
     * Solve a certain notification and remove it from the buffer.
     * @param component the component.
     */
    void solve(ITextComponent component);

    /**
     * Remind the player later again about the issue.
     * @param component the component.
     * @param worldTick the world tick.
     */
    void remindMeLater(ITextComponent component, int worldTick);

    /**
     * Notify about death of citizen.
     * @param damageSource the damage source.
     */
    void notifyDeath(DamageSource damageSource);
}
