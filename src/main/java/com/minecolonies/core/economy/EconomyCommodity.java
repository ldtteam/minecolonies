package com.minecolonies.core.economy;

/**
 * A deliberately small, data-only description of a commodity traded by the
 * first economy prototype. Values are measured per trade bundle, not per item.
 */
public record EconomyCommodity(
    String itemId,
    int bundleSize,
    int baseSellPrice,
    int baseBuyPrice,
    int initialStock,
    int dailyRestock,
    int warehouseReserve)
{
    public EconomyCommodity
    {
        if (itemId == null || itemId.isBlank())
        {
            throw new IllegalArgumentException("itemId must not be blank");
        }
        if (bundleSize < 1 || baseSellPrice < 1 || baseBuyPrice < baseSellPrice + 1 || initialStock < 0 || dailyRestock < 0 || warehouseReserve < 0)
        {
            throw new IllegalArgumentException("Invalid commodity values for " + itemId);
        }
    }
}
