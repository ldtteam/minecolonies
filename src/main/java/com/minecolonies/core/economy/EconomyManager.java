package com.minecolonies.core.economy;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side facade for the economy. It owns persistence, day advancement,
 * warehouse exports, and player treasury operations; UI code only asks it for
 * quotes and records completed trades.
 */
public final class EconomyManager
{
    public static final long TICKS_PER_DAY = 24_000L;
    public static final int MARKET_LEVEL = 1;

    private EconomyManager()
    {
    }

    public static EconomySavedData data(final MinecraftServer server)
    {
        return server.overworld().getDataStorage().computeIfAbsent(EconomySavedData.TYPE);
    }

    public static String key(final IColony colony)
    {
        return colony.getDimension().identifier() + "#" + colony.getID();
    }

    public static EconomyLedger ledger(final MinecraftServer server, final IColony colony)
    {
        final EconomyLedger ledger = data(server).getOrCreate(key(colony));
        ensureCatalog(ledger);
        return ledger;
    }

    public static void markDirty(final MinecraftServer server)
    {
        data(server).setDirty();
    }

    public static void enable(final MinecraftServer server, final IColony colony)
    {
        final EconomyLedger ledger = ledger(server, colony);
        if (!ledger.enabled())
        {
            ledger.setEnabled(true);
            ledger.setLastDay(server.overworld().getGameTime() / TICKS_PER_DAY);
            data(server).setDirty();
        }
    }

    public static void disable(final MinecraftServer server, final IColony colony)
    {
        final EconomyLedger ledger = ledger(server, colony);
        if (ledger.enabled())
        {
            ledger.setEnabled(false);
            data(server).setDirty();
        }
    }

    /**
     * Called once per server tick. Daily work is bounded to avoid a large
     * catch-up spike after a server has been offline for a long time.
     */
    public static void tick(final MinecraftServer server)
    {
        if (server.getTickCount() % 20 != 0)
        {
            return;
        }

        final long day = server.overworld().getGameTime() / TICKS_PER_DAY;
        final EconomySavedData savedData = data(server);
        boolean changed = false;
        for (final IColony colony : IColonyManager.getInstance().getAllColonies())
        {
            if (!(colony.getWorld() instanceof ServerLevel) || !ledger(server, colony).enabled())
            {
                continue;
            }

            final EconomyLedger ledger = ledger(server, colony);
            if (ledger.lastDay() < 0)
            {
                seedCatalog(ledger);
                ledger.setLastDay(day);
                changed = true;
                continue;
            }

            final long daysElapsed = day - ledger.lastDay();
            if (daysElapsed <= 0)
            {
                continue;
            }

            final long restockDays = Math.min(daysElapsed, 30L);
            for (long i = 0; i < restockDays; i++)
            {
                for (final EconomyCommodity commodity : EconomyCatalog.commodities())
                {
                    ledger.restock(commodity);
                }
                ledger.decayDemand();
            }
            ledger.setLastDay(day);
            exportSurplus(colony, ledger);
            changed = true;
        }

        if (changed)
        {
            savedData.setDirty();
        }
    }

    public static EconomyQuote quote(final EconomyLedger ledger, final EconomyCommodity commodity)
    {
        return new EconomyQuote(
            commodity,
            PriceModel.sellPrice(commodity, ledger.supply(commodity.itemId()), ledger.demand(commodity.itemId())),
            PriceModel.buyPrice(commodity, ledger.supply(commodity.itemId()), ledger.demand(commodity.itemId())),
            ledger.supply(commodity.itemId()),
            ledger.demand(commodity.itemId()));
    }

    public static Item resolveItem(final EconomyCommodity commodity)
    {
        return BuiltInRegistries.ITEM.getValue(Identifier.parse(commodity.itemId()));
    }

    public static EconomyCommodity commodityForItemId(final String itemId)
    {
        return EconomyCatalog.byItemId(itemId);
    }

    public static int countPlayerItems(final Player player, final Item item)
    {
        int count = 0;
        final Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        {
            final ItemStack stack = inventory.getItem(slot);
            if (stack.is(item))
            {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int removePlayerItems(final Player player, final Item item, final int amount)
    {
        int remaining = Math.max(0, amount);
        final Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++)
        {
            final ItemStack stack = inventory.getItem(slot);
            if (stack.is(item))
            {
                final int removed = Math.min(remaining, stack.getCount());
                inventory.removeItem(slot, removed);
                remaining -= removed;
            }
        }
        return amount - remaining;
    }

    public static boolean withdrawTreasury(final EconomyLedger ledger, final Player player, final int amount)
    {
        if (amount < 1 || !ledger.debitTreasury(amount))
        {
            return false;
        }

        giveItem(player, new ItemStack(Items.EMERALD, amount));
        return true;
    }

    public static int depositEmeralds(final EconomyLedger ledger, final Player player, final int amount)
    {
        final int deposited = removePlayerItems(player, Items.EMERALD, amount);
        ledger.creditTreasury(deposited);
        return deposited;
    }

    public static void giveItem(final Player player, final ItemStack stack)
    {
        if (!player.getInventory().add(stack))
        {
            player.drop(stack, false);
        }
    }

    /**
     * Converts full commodity bundles above the warehouse safety stock into
     * colony treasury value. The operation is atomic per bundle and never
     * removes the reserve, so food and building materials remain available to
     * MineColonies' request system.
     */
    public static ExportResult exportSurplus(final IColony colony, final EconomyLedger ledger)
    {
        final List<IItemHandler> warehouses = new ArrayList<>();
        for (final IWareHouse warehouse : colony.getServerBuildingManager().getWareHouses())
        {
            final IItemHandler handler = warehouse.getTileEntity() == null ? null : warehouse.getTileEntity().getItemHandlerCap();
            if (handler != null)
            {
                warehouses.add(handler);
            }
        }

        int bundleCount = 0;
        long value = 0L;
        for (final EconomyCommodity commodity : EconomyCatalog.commodities())
        {
            final Item item = resolveItem(commodity);
            if (item == Items.AIR)
            {
                continue;
            }

            int total = 0;
            for (final IItemHandler handler : warehouses)
            {
                total += InventoryUtils.getItemCountInItemHandler(handler, item);
            }

            final int surplus = Math.max(0, total - commodity.warehouseReserve());
            final int bundles = surplus / commodity.bundleSize();
            final int target = bundles * commodity.bundleSize();
            final int removed = extractFromHandlers(warehouses, item, target);
            final int soldBundles = removed / commodity.bundleSize();
            if (soldBundles > 0)
            {
                final long commodityValue = (long) soldBundles * PriceModel.sellPrice(
                    commodity,
                    ledger.supply(commodity.itemId()),
                    ledger.demand(commodity.itemId()));
                ledger.recordExport(commodityValue);
                bundleCount += soldBundles;
                value += commodityValue;
            }
        }

        return new ExportResult(bundleCount, value);
    }

    private static int extractFromHandlers(final List<IItemHandler> handlers, final Item item, final int amount)
    {
        int remaining = amount;
        for (final IItemHandler handler : handlers)
        {
            for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++)
            {
                final ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.is(item))
                {
                    continue;
                }
                final ItemStack extracted = handler.extractItem(slot, Math.min(remaining, stack.getCount()), false);
                remaining -= extracted.getCount();
            }
        }
        return amount - remaining;
    }

    private static void ensureCatalog(final EconomyLedger ledger)
    {
        if (ledger.lastDay() >= 0)
        {
            return;
        }
        seedCatalog(ledger);
    }

    private static void seedCatalog(final EconomyLedger ledger)
    {
        for (final EconomyCommodity commodity : EconomyCatalog.commodities())
        {
            if (ledger.supply(commodity.itemId()) == 0)
            {
                ledger.addSupply(commodity.itemId(), commodity.initialStock());
            }
        }
    }

    public record EconomyQuote(EconomyCommodity commodity, int sellPrice, int buyPrice, int supply, int demand)
    {
    }

    public record ExportResult(int bundles, long value)
    {
    }
}
