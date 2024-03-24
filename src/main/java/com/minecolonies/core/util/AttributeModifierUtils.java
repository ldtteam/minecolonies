package com.minecolonies.core.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

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

        entity.getAttribute(Attributes.MAX_HEALTH).removeModifiers();

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
    public static void removeHealthModifier(final LivingEntity entity, final UUID modifierName)
    {
        if (entity == null)
        {
            return;
        }

        entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(modifierName);
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

        removeHealthModifier(entity, modifier.getId());
        entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);

        entity.setHealth(entity.getMaxHealth() * prevHealthPct);
    }

    /**
     * Remove a specific modifier from an entity.
     * @param entity the entity.
     * @param modifierName the name of the modifier.
     * @param attribute the type of attribute.
     */
    public static void removeModifier(final LivingEntity entity, final UUID modifierName, final Attribute attribute)
    {
        if (entity == null)
        {
            return;
        }

        entity.getAttribute(attribute).removeModifier(modifierName);
    }

    /**
     * Add a specific new modifier.
     * @param entity the entity to add it to.
     * @param modifier the modifier to add.
     * @param attribute the type of the attribute.
     */
    public static void addModifier(final LivingEntity entity, final AttributeModifier modifier, final Attribute attribute)
    {
        if (entity == null)
        {
            return;
        }

        removeModifier(entity, modifier.getId(), attribute);
        entity.getAttribute(attribute).addTransientModifier(modifier);
    }
}
