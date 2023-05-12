package com.minecolonies.coremod.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Specific named damage source.
 */
public class NamedDamageSource extends EntityDamageSource
{
    /**
     * Create a specific named damage source.
     *
     * @param damageTypeIn         the string to print.
     * @param damageSourceEntityIn the inflicting entity.
     */
    public NamedDamageSource(final String damageTypeIn, @Nullable final Entity damageSourceEntityIn)
    {
        super(damageTypeIn, damageSourceEntityIn);
    }

    @NotNull
    @Override
    public Component getLocalizedDeathMessage(LivingEntity entityLivingBaseIn)
    {
        return Component.translatable(this.msgId, entityLivingBaseIn.getName());
    }

    /**
     * World difficulty scaling of damage against players, disabled as we already do take world difficulty into account.
     *
     * @return false
     */
    public boolean scalesWithDifficulty()
    {
        return false;
    }
}
