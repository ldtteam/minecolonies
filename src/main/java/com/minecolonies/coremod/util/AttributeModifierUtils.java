package com.minecolonies.coremod.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;

/**
 * Utility class for handling add/removal of attribute modifiers.
 */
public abstract class AttributeModifierUtils
{
    /**
     * Remove all healthmodifiers from a citizen
     *
     * @param entity the entity to remove the modifiers from
     */
    public static void removeAllHealthModifiers(final LivingEntity entity)
    {
        if (entity == null)
        {
            return;
        }

        for (final AttributeModifier mod : entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getModifiers())
        {
            entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(mod);
        }

        if (entity.getHealth() > entity.getMaxHealth())
        {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    /**
     * Removes healthmodifier by name. Reduces HP if needed
     *
     * @param entity       the entity to remove the modifier from
     * @param modifierName Name of the modifier to remove, see e.g. GUARD_HEALTH_MOD_LEVEL_NAME
     */
    public static void removeHealthModifier(final LivingEntity entity, final String modifierName)
    {
        if (entity == null)
        {
            return;
        }

        for (final AttributeModifier mod : entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH).getModifiers())
        {
            if (mod.getName().equals(modifierName))
            {
                entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(mod);
            }
        }
        if (entity.getHealth() > entity.getMaxHealth())
        {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    /**
     * Adds a health modifier, overwriting old modifier with the same name. Keeps health percentage.
     *
     * @param entity   entity to add a healthmodifier to
     * @param modifier the modifier to add.
     */
    public static void addHealthModifier(final LivingEntity entity, final AttributeModifier modifier)
    {
        if (entity == null)
        {
            return;
        }

        final float prevHealthPct = entity.getHealth() / entity.getMaxHealth();

        removeHealthModifier(entity, modifier.getName());
        entity.getAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(modifier);

        entity.setHealth(entity.getMaxHealth() * prevHealthPct);
    }
}
