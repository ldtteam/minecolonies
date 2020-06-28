package com.minecolonies.coremod.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Specific named damage source.
 */
public class NamedDamageSource extends EntityDamageSource
{
    /**
     * Create a specific named damage source.
     * @param damageTypeIn the string to print.
     * @param damageSourceEntityIn the inflicting entity.
     */
    public NamedDamageSource(final String damageTypeIn, @Nullable final Entity damageSourceEntityIn)
    {
        super(damageTypeIn, damageSourceEntityIn);
    }

    @NotNull
    @Override
    public ITextComponent getDeathMessage(LivingEntity entityLivingBaseIn)
    {
        return new StringTextComponent(this.damageType);
    }
}
