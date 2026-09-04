package com.minecolonies.core.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Persistent account for one colony. This class has no Minecraft references
 * so the balance and pricing rules can be tested without starting a game.
 */
public final class EconomyLedger
{
    public static final Codec<EconomyLedger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.optionalFieldOf("treasury", 0L).forGetter(EconomyLedger::treasury),
        Codec.LONG.optionalFieldOf("gdp", 0L).forGetter(EconomyLedger::gdp),
        Codec.LONG.optionalFieldOf("exports", 0L).forGetter(EconomyLedger::exports),
        Codec.LONG.optionalFieldOf("imports", 0L).forGetter(EconomyLedger::imports),
        Codec.LONG.optionalFieldOf("tradeVolume", 0L).forGetter(EconomyLedger::tradeVolume),
        Codec.LONG.optionalFieldOf("lastDay", -1L).forGetter(EconomyLedger::lastDay),
        Codec.BOOL.optionalFieldOf("enabled", false).forGetter(EconomyLedger::enabled),
        Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("supply", Map.of()).forGetter(EconomyLedger::supply),
        Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("demand", Map.of()).forGetter(EconomyLedger::demand),
        Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("marketSales", Map.of()).forGetter(EconomyLedger::marketSales),
        Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("marketPurchases", Map.of()).forGetter(EconomyLedger::marketPurchases),
        Codec.LONG.optionalFieldOf("marketDay", -1L).forGetter(EconomyLedger::marketDay)
    ).apply(instance, EconomyLedger::new));

    private long treasury;
    private long gdp;
    private long exports;
    private long imports;
    private long tradeVolume;
    private long lastDay;
    private boolean enabled;
    private final Map<String, Integer> supply;
    private final Map<String, Integer> demand;
    private final Map<String, Integer> marketSales;
    private final Map<String, Integer> marketPurchases;
    private long marketDay;

    public EconomyLedger()
    {
        this(0L, 0L, 0L, 0L, 0L, -1L, false, Map.of(), Map.of(), Map.of(), Map.of(), -1L);
    }

    public EconomyLedger(
        final long treasury,
        final long gdp,
        final long exports,
        final long imports,
        final long tradeVolume,
        final long lastDay,
        final boolean enabled,
        final Map<String, Integer> supply,
        final Map<String, Integer> demand,
        final Map<String, Integer> marketSales,
        final Map<String, Integer> marketPurchases,
        final long marketDay)
    {
        this.treasury = Math.max(0L, treasury);
        this.gdp = Math.max(0L, gdp);
        this.exports = Math.max(0L, exports);
        this.imports = Math.max(0L, imports);
        this.tradeVolume = Math.max(0L, tradeVolume);
        this.lastDay = lastDay;
        this.enabled = enabled;
        this.supply = new HashMap<>();
        this.demand = new HashMap<>();
        supply.forEach((key, value) -> this.supply.put(key, Math.max(0, value)));
        demand.forEach((key, value) -> this.demand.put(key, Math.max(0, value)));
        this.marketSales = new HashMap<>();
        this.marketPurchases = new HashMap<>();
        marketSales.forEach((key, value) -> this.marketSales.put(key, Math.max(0, value)));
        marketPurchases.forEach((key, value) -> this.marketPurchases.put(key, Math.max(0, value)));
        this.marketDay = marketDay;
    }

    public long treasury()
    {
        return treasury;
    }

    public long gdp()
    {
        return gdp;
    }

    public long exports()
    {
        return exports;
    }

    public long imports()
    {
        return imports;
    }

    public long tradeVolume()
    {
        return tradeVolume;
    }

    public long lastDay()
    {
        return lastDay;
    }

    public boolean enabled()
    {
        return enabled;
    }

    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }

    public Map<String, Integer> supply()
    {
        return Map.copyOf(supply);
    }

    public Map<String, Integer> demand()
    {
        return Map.copyOf(demand);
    }

    public Map<String, Integer> marketSales()
    {
        return Map.copyOf(marketSales);
    }

    public Map<String, Integer> marketPurchases()
    {
        return Map.copyOf(marketPurchases);
    }

    public long marketDay()
    {
        return marketDay;
    }

    public void beginMarketDay(final long day)
    {
        if (marketDay != day)
        {
            marketSales.clear();
            marketPurchases.clear();
            marketDay = day;
        }
    }

    public int marketSales(final String itemId)
    {
        return marketSales.getOrDefault(itemId, 0);
    }

    public int marketPurchases(final String itemId)
    {
        return marketPurchases.getOrDefault(itemId, 0);
    }

    public int supply(final String itemId)
    {
        return supply.getOrDefault(itemId, 0);
    }

    public int demand(final String itemId)
    {
        return demand.getOrDefault(itemId, 0);
    }

    public void setLastDay(final long day)
    {
        lastDay = day;
    }

    public void addSupply(final String itemId, final int bundles)
    {
        if (bundles > 0)
        {
            supply.merge(itemId, bundles, (oldValue, value) -> Math.max(0, oldValue + value));
        }
    }

    public boolean removeSupply(final String itemId, final int bundles)
    {
        if (bundles < 1 || supply(itemId) < bundles)
        {
            return false;
        }
        final int remaining = supply(itemId) - bundles;
        if (remaining == 0)
        {
            supply.remove(itemId);
        }
        else
        {
            supply.put(itemId, remaining);
        }
        return true;
    }

    public void addDemand(final String itemId, final int bundles)
    {
        if (bundles > 0)
        {
            demand.merge(itemId, bundles, (oldValue, value) -> Math.max(0, oldValue + value));
        }
    }

    public void decayDemand()
    {
        demand.replaceAll((key, value) -> value / 2);
        demand.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    public void restock(final EconomyCommodity commodity)
    {
        addSupply(commodity.itemId(), commodity.dailyRestock());
    }

    public void creditTreasury(final long amount)
    {
        if (amount > 0)
        {
            treasury = Math.addExact(treasury, amount);
        }
    }

    public boolean debitTreasury(final long amount)
    {
        if (amount < 0 || treasury < amount)
        {
            return false;
        }
        treasury -= amount;
        return true;
    }

    public void recordExport(final long value)
    {
        if (value > 0)
        {
            exports = Math.addExact(exports, value);
            gdp = Math.addExact(gdp, value);
            tradeVolume = Math.addExact(tradeVolume, value);
            creditTreasury(value);
        }
    }

    public void recordMarketSale(final String itemId, final long value)
    {
        if (value > 0)
        {
            exports = Math.addExact(exports, value);
            gdp = Math.addExact(gdp, value);
            tradeVolume = Math.addExact(tradeVolume, value);
        }
        addSupply(itemId, 1);
        marketSales.merge(itemId, 1, Integer::sum);
        if (demand(itemId) > 0)
        {
            demand.put(itemId, demand(itemId) - 1);
        }
    }

    public void recordMarketPurchase(final String itemId, final long value)
    {
        if (value > 0)
        {
            imports = Math.addExact(imports, value);
            tradeVolume = Math.addExact(tradeVolume, value);
        }
        addDemand(itemId, 1);
        marketPurchases.merge(itemId, 1, Integer::sum);
    }
}
