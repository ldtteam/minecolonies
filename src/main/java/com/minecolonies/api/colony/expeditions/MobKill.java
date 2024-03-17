package com.minecolonies.api.colony.expeditions;

import net.minecraft.world.entity.EntityType;

/**
 * Record for recording a kill during an expedition stage.
 *
 * @param entity the entity that was killed.
 * @param count  how many of said entity were killed.
 */
public record MobKill(EntityType<?> entity, int count)
{
}
