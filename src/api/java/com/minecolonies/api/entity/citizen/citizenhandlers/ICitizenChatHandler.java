package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.util.DamageSource;

public interface ICitizenChatHandler
{
    /**
     * Notify about death of citizen.
     *
     * @param damageSource the damage source.
     */
    void notifyDeath(DamageSource damageSource);
}
