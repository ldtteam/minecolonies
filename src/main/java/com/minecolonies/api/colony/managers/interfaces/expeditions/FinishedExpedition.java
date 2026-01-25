package com.minecolonies.api.colony.managers.interfaces.expeditions;

import com.minecolonies.api.colony.expeditions.ExpeditionFinishedStatus;

/**
 * Container class for finished expedition instances.
 *
 * @param expedition the completed expedition object.
 * @param status     the status which the expedition was finished with.
 */
public record FinishedExpedition(ColonyExpedition expedition, ExpeditionFinishedStatus status)
{
}
