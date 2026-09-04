package com.minecolonies.core.entity.ai.combat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Applies damage on the authoritative server side of the MC 26 damage split.
 */
public final class ServerDamageHelper
{
    private ServerDamageHelper()
    {
    }

    public static boolean apply(final LivingEntity target, final DamageSource source, final float amount)
    {
        if (target.level() instanceof ServerLevel level)
        {
            return target.hurtServer(level, source, amount);
        }
        return false;
    }
}
