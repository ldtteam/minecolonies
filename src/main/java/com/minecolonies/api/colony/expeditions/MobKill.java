package com.minecolonies.api.colony.expeditions;

import net.minecraft.resources.ResourceLocation;

/**
 * Record for recording a kill during an expedition stage.
 *
 * @param encounterId the entity that was killed.
 * @param count       how many of said entity were killed.
 */
public record MobKill(ResourceLocation encounterId, int count)
{
}
