package com.minecolonies.core.economy;

/**
 * Deterministic, bounded pricing used by both commands and the vanilla market
 * menu. Demand is measured in completed trade bundles and supply in available
 * bundles. The spread prevents a player from buying and immediately selling a
 * commodity for a profit.
 */
public final class PriceModel
{
    private static final double MAX_SCARCITY_BONUS = 1.0D;
    private static final double MAX_SCARCITY_DISCOUNT = -0.35D;
    private static final double PRICE_SENSITIVITY = 0.35D;
    private static final double BUY_SPREAD = 1.20D;

    private PriceModel()
    {
    }

    public static int sellPrice(final EconomyCommodity commodity, final int supply, final int demand)
    {
        return scaledPrice(commodity.baseSellPrice(), scarcityMultiplier(supply, demand));
    }

    public static int buyPrice(final EconomyCommodity commodity, final int supply, final int demand)
    {
        final int sell = sellPrice(commodity, supply, demand);
        final int calculated = scaledPrice(commodity.baseBuyPrice(), scarcityMultiplier(supply, demand) * BUY_SPREAD);
        return Math.max(sell + 1, calculated);
    }

    public static double scarcityMultiplier(final int supply, final int demand)
    {
        final int safeSupply = Math.max(0, supply);
        final int safeDemand = Math.max(0, demand);
        final double pressure = (double) (safeDemand - safeSupply) / Math.max(8.0D, safeSupply + safeDemand + 8.0D);
        final double adjustment = Math.max(MAX_SCARCITY_DISCOUNT, Math.min(MAX_SCARCITY_BONUS, pressure * PRICE_SENSITIVITY));
        return 1.0D + adjustment;
    }

    private static int scaledPrice(final int base, final double multiplier)
    {
        return Math.max(1, (int) Math.round(base * Math.max(0.5D, multiplier)));
    }
}
