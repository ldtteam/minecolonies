package com.minecolonies.core.economy;

import com.minecolonies.api.colony.IColony;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A server-backed virtual merchant. The client receives the ordinary vanilla
 * MerchantMenu/MerchantScreen packet, so no economy-specific client code is
 * needed.
 */
public final class MarketMerchant implements Merchant
{
    private static final int DAILY_SELL_LIMIT = 32;
    private static final int DAILY_BUY_LIMIT = 64;

    private final MinecraftServer server;
    private final IColony colony;
    private final EconomyLedger ledger;
    private final MerchantOffers offers = new MerchantOffers();
    private final Map<MerchantOffer, OfferKind> offerKinds = new IdentityHashMap<>();
    private @Nullable Player tradingPlayer;
    private int xp;

    public MarketMerchant(final MinecraftServer server, final IColony colony, final EconomyLedger ledger)
    {
        this.server = server;
        this.colony = colony;
        this.ledger = ledger;
        rebuildOffers();
    }

    public void openFor(final ServerPlayer player)
    {
        setTradingPlayer(player);
        openTradingScreen(player, net.minecraft.network.chat.Component.literal(colony.getName() + " Market"), EconomyManager.MARKET_LEVEL);
    }

    private void rebuildOffers()
    {
        ledger.beginMarketDay(server.overworld().getGameTime() / EconomyManager.TICKS_PER_DAY);
        for (final EconomyCommodity commodity : EconomyCatalog.commodities())
        {
            final net.minecraft.world.item.Item item = EconomyManager.resolveItem(commodity);
            if (item == Items.AIR)
            {
                continue;
            }

            final EconomyManager.EconomyQuote quote = EconomyManager.quote(ledger, commodity);
            final int sellUses = DAILY_SELL_LIMIT - ledger.marketSales(commodity.itemId());
            if (sellUses > 0)
            {
                final MerchantOffer sell = new EconomyOffer(
                    new ItemCost(item, commodity.bundleSize()),
                    new ItemStack(Items.EMERALD, quote.sellPrice()),
                    sellUses,
                    1,
                    0.0F,
                    ledger,
                    commodity.itemId(),
                    TradeDirection.SELL_TO_MARKET);
                offers.add(sell);
                offerKinds.put(sell, new OfferKind(TradeDirection.SELL_TO_MARKET, commodity, quote.sellPrice()));
            }

            final int buyUses = Math.min(DAILY_BUY_LIMIT - ledger.marketPurchases(commodity.itemId()), quote.supply());
            if (buyUses > 0)
            {
                final MerchantOffer buy = new EconomyOffer(
                    new ItemCost(Items.EMERALD, quote.buyPrice()),
                    new ItemStack(item, commodity.bundleSize()),
                    buyUses,
                    1,
                    0.0F,
                    ledger,
                    commodity.itemId(),
                    TradeDirection.BUY_FROM_MARKET);
                offers.add(buy);
                offerKinds.put(buy, new OfferKind(TradeDirection.BUY_FROM_MARKET, commodity, quote.buyPrice()));
            }
        }
    }

    @Override
    public void setTradingPlayer(@Nullable final Player player)
    {
        tradingPlayer = player;
    }

    @Override
    public @Nullable Player getTradingPlayer()
    {
        return tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers()
    {
        return offers;
    }

    @Override
    public void overrideOffers(final MerchantOffers offers)
    {
        this.offers.clear();
        this.offers.addAll(offers);
    }

    @Override
    public void notifyTrade(final MerchantOffer offer)
    {
        offer.increaseUses();
        final OfferKind kind = offerKinds.get(offer);
        if (kind == null)
        {
            return;
        }

        if (kind.direction() == TradeDirection.SELL_TO_MARKET)
        {
            ledger.recordMarketSale(kind.commodity().itemId(), kind.price());
            EconomyManager.markDirty(server);
        }
        else if (ledger.removeSupply(kind.commodity().itemId(), 1))
        {
            ledger.recordMarketPurchase(kind.commodity().itemId(), kind.price());
            EconomyManager.markDirty(server);
        }
    }

    @Override
    public void notifyTradeUpdated(final ItemStack itemStack)
    {
        // The vanilla menu calls this while the payment slots change. Prices
        // are fixed when the menu opens, preventing mid-trade price races.
    }

    @Override
    public int getVillagerXp()
    {
        return xp;
    }

    @Override
    public void overrideXp(final int xp)
    {
        this.xp = xp;
    }

    @Override
    public boolean showProgressBar()
    {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound()
    {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide()
    {
        return false;
    }

    @Override
    public boolean stillValid(final Player player)
    {
        if (!(colony.getWorld() instanceof net.minecraft.world.level.Level level) || player.level() != level)
        {
            return false;
        }
        return player.distanceToSqr(colony.getCenter().getX() + 0.5D, colony.getCenter().getY() + 0.5D, colony.getCenter().getZ() + 0.5D) <= 64.0D * 64.0D;
    }

    private enum TradeDirection
    {
        SELL_TO_MARKET,
        BUY_FROM_MARKET
    }

    /**
     * Merchant menus can remain open while another player trades. Checking the
     * shared ledger at the server-side payment step keeps the daily cap and
     * finite market stock authoritative even for stale menus.
     */
    private static final class EconomyOffer extends MerchantOffer
    {
        private final EconomyLedger ledger;
        private final String itemId;
        private final TradeDirection direction;

        private EconomyOffer(
            final ItemCost payment,
            final ItemStack result,
            final int maxUses,
            final int xpValue,
            final float priceMultiplier,
            final EconomyLedger ledger,
            final String itemId,
            final TradeDirection direction)
        {
            super(payment, result, maxUses, xpValue, priceMultiplier);
            this.ledger = ledger;
            this.itemId = itemId;
            this.direction = direction;
        }

        @Override
        public boolean take(final ItemStack paymentA, final ItemStack paymentB)
        {
            if (direction == TradeDirection.SELL_TO_MARKET
                && ledger.marketSales(itemId) >= DAILY_SELL_LIMIT)
            {
                return false;
            }
            if (direction == TradeDirection.BUY_FROM_MARKET
                && (ledger.marketPurchases(itemId) >= DAILY_BUY_LIMIT || ledger.supply(itemId) < 1))
            {
                return false;
            }
            return super.take(paymentA, paymentB);
        }
    }

    private record OfferKind(TradeDirection direction, EconomyCommodity commodity, int price)
    {
    }
}
