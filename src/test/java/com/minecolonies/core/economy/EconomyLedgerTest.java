package com.minecolonies.core.economy;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EconomyLedgerTest
{
    private static final EconomyCommodity WHEAT = new EconomyCommodity(
        "minecraft:wheat", 32, 1, 2, 12, 3, 64);

    @Test
    public void marketSpreadNeverCreatesAnInstantArbitrage()
    {
        for (int supply = 0; supply <= 200; supply += 10)
        {
            for (int demand = 0; demand <= 200; demand += 10)
            {
                final int sell = PriceModel.sellPrice(WHEAT, supply, demand);
                final int buy = PriceModel.buyPrice(WHEAT, supply, demand);
                assertTrue("buy price must exceed sell price", buy > sell);
                assertTrue("sell price must stay positive", sell > 0);
            }
        }
    }

    @Test
    public void marketSaleAddsGdpButMarketPurchaseOnlyAddsImports()
    {
        final EconomyLedger ledger = new EconomyLedger();
        ledger.addSupply(WHEAT.itemId(), 4);
        ledger.beginMarketDay(3);

        ledger.recordMarketSale(WHEAT.itemId(), 2);
        assertEquals(2L, ledger.gdp());
        assertEquals(2L, ledger.exports());
        assertEquals(2L, ledger.tradeVolume());
        assertEquals(5, ledger.supply(WHEAT.itemId()));

        assertTrue(ledger.removeSupply(WHEAT.itemId(), 1));
        ledger.recordMarketPurchase(WHEAT.itemId(), 3);
        assertEquals("imports are not GDP", 2L, ledger.gdp());
        assertEquals(3L, ledger.imports());
        assertEquals(5L, ledger.tradeVolume());
        assertEquals(1, ledger.demand(WHEAT.itemId()));
    }

    @Test
    public void exportCreditsTreasuryAndNeverAllowsNegativeBalances()
    {
        final EconomyLedger ledger = new EconomyLedger();
        ledger.recordExport(17);
        assertEquals(17L, ledger.treasury());
        assertEquals(17L, ledger.gdp());
        assertTrue(ledger.debitTreasury(7));
        assertEquals(10L, ledger.treasury());
        assertFalse(ledger.debitTreasury(11));
        assertEquals(10L, ledger.treasury());
    }

    @Test
    public void marketCountersResetWhenTheDayChanges()
    {
        final EconomyLedger ledger = new EconomyLedger();
        ledger.beginMarketDay(10);
        ledger.recordMarketSale(WHEAT.itemId(), 1);
        ledger.beginMarketDay(11);
        assertEquals(0, ledger.marketSales(WHEAT.itemId()));
        assertEquals(11L, ledger.marketDay());
    }

    @Test
    public void ledgerCodecRoundTripsThePersistentState()
    {
        final EconomyLedger original = new EconomyLedger(
            9L, 12L, 7L, 5L, 12L, 4L, true,
            Map.of(WHEAT.itemId(), 8), Map.of(WHEAT.itemId(), 2),
            Map.of(WHEAT.itemId(), 1), Map.of(WHEAT.itemId(), 3), 4L);
        final JsonElement encoded = EconomyLedger.CODEC.encodeStart(JsonOps.INSTANCE, original).result().orElseThrow();
        final EconomyLedger decoded = EconomyLedger.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();

        assertEquals(original.treasury(), decoded.treasury());
        assertEquals(original.gdp(), decoded.gdp());
        assertEquals(original.exports(), decoded.exports());
        assertEquals(original.imports(), decoded.imports());
        assertEquals(original.tradeVolume(), decoded.tradeVolume());
        assertEquals(original.lastDay(), decoded.lastDay());
        assertTrue(decoded.enabled());
        assertEquals(original.supply(), decoded.supply());
        assertEquals(original.demand(), decoded.demand());
        assertEquals(original.marketSales(), decoded.marketSales());
        assertEquals(original.marketPurchases(), decoded.marketPurchases());
        assertEquals(original.marketDay(), decoded.marketDay());
    }

    @Test
    public void commandBuilderExposesTheEconomyOperations()
    {
        final var economy = new com.minecolonies.core.commands.colonycommands.CommandEconomy().build().build();
        assertNotNull(economy.getChild("status"));
        assertNotNull(economy.getChild("quote"));
        assertNotNull(economy.getChild("market"));
        assertNotNull(economy.getChild("enable"));
        assertNotNull(economy.getChild("disable"));
        assertNotNull(economy.getChild("export"));
        assertNotNull(economy.getChild("deposit"));
        assertNotNull(economy.getChild("withdraw"));
    }
}
