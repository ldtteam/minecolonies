package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.commands.arguments.ColonyIdArgument;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.economy.EconomyCatalog;
import com.minecolonies.core.economy.EconomyCommodity;
import com.minecolonies.core.economy.EconomyLedger;
import com.minecolonies.core.economy.EconomyManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Player-facing entry point for the server-only economy. The command opens the
 * vanilla merchant menu and provides the small amount of administration needed
 * to opt a colony in and export warehouse surplus.
 */
public class CommandEconomy implements IMCCommand
{
    private static final String ITEM_ARG = "item";
    private static final String AMOUNT_ARG = "amount";

    @Override
    public String getName()
    {
        return "economy";
    }

    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        context.getSource().sendFailure(Component.literal(
            "Usage: /economy <status|quote|market|enable|disable|export|deposit|withdraw> ..."));
        return 0;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        final LiteralArgumentBuilder<CommandSourceStack> root = IMCCommand.newLiteral(getName())
            .executes(this::checkPreConditionAndExecute);
        root.then(IMCCommand.newLiteral("status")
            .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id()).executes(this::status)));
        root.then(IMCCommand.newLiteral("quote")
            .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id())
                .then(IMCCommand.newArgument(ITEM_ARG, StringArgumentType.word()).executes(this::quote))));
        root.then(IMCCommand.newLiteral("market")
            .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id()).executes(this::market)));
        root.then(IMCCommand.newLiteral("enable")
            .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id()).executes(this::enable)));
        root.then(IMCCommand.newLiteral("disable")
            .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id()).executes(this::disable)));
        root.then(IMCCommand.newLiteral("export")
            .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id()).executes(this::export)));
        root.then(IMCCommand.newLiteral("deposit")
            .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id())
                .then(IMCCommand.newArgument(AMOUNT_ARG, IntegerArgumentType.integer(1, 1_000_000)).executes(this::deposit))));
        root.then(IMCCommand.newLiteral("withdraw")
            .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id())
                .then(IMCCommand.newArgument(AMOUNT_ARG, IntegerArgumentType.integer(1, 1_000_000)).executes(this::withdraw))));
        return root;
    }

    private int status(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = colony(context);
        if (colony == null)
        {
            return 0;
        }
        final EconomyLedger ledger = EconomyManager.ledger(context.getSource().getServer(), colony);
        context.getSource().sendSuccess(() -> Component.literal(
            "Economy " + colony.getName() + " [" + (ledger.enabled() ? "active" : "inactive") + "] "
                + "GDP=" + ledger.gdp() + " emeralds, treasury=" + ledger.treasury()
                + ", exports=" + ledger.exports() + ", imports=" + ledger.imports()
                + ", trade volume=" + ledger.tradeVolume()), false);
        return 1;
    }

    private int quote(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = colony(context);
        if (colony == null)
        {
            return 0;
        }
        final EconomyCommodity commodity = EconomyManager.commodityForItemId(StringArgumentType.getString(context, ITEM_ARG));
        if (commodity == null)
        {
            context.getSource().sendFailure(Component.literal("Unknown economy item. Use one of: "
                + String.join(", ", EconomyCatalog.commodities().stream().map(EconomyCommodity::itemId).toList())));
            return 0;
        }
        final EconomyLedger ledger = EconomyManager.ledger(context.getSource().getServer(), colony);
        final EconomyManager.EconomyQuote quote = EconomyManager.quote(ledger, commodity);
        context.getSource().sendSuccess(() -> Component.literal(
            commodity.itemId() + ": sell " + quote.sellPrice() + " emeralds / " + commodity.bundleSize()
                + ", buy " + quote.buyPrice() + " emeralds / " + commodity.bundleSize()
                + ", stock " + quote.supply() + " bundles, demand " + quote.demand()), false);
        return 1;
    }

    private int market(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = colony(context);
        final Player player = context.getSource().getEntity() instanceof Player p ? p : null;
        if (colony == null || !(player instanceof ServerPlayer serverPlayer))
        {
            context.getSource().sendFailure(Component.literal("The market must be opened by a player near the colony."));
            return 0;
        }
        if (!EconomyManager.ledger(context.getSource().getServer(), colony).enabled())
        {
            context.getSource().sendFailure(Component.literal("This colony's economy is inactive. An officer must run economy enable first."));
            return 0;
        }
        if (serverPlayer.distanceToSqr(colony.getCenter().getX() + 0.5D, colony.getCenter().getY() + 0.5D, colony.getCenter().getZ() + 0.5D) > 64.0D * 64.0D)
        {
            context.getSource().sendFailure(Component.literal("You must be within 64 blocks of the town hall."));
            return 0;
        }
        new com.minecolonies.core.economy.MarketMerchant(
            context.getSource().getServer(), colony,
            EconomyManager.ledger(context.getSource().getServer(), colony)).openFor(serverPlayer);
        return 1;
    }

    private int enable(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = colony(context);
        if (colony == null || !officer(context, colony))
        {
            return 0;
        }
        EconomyManager.enable(context.getSource().getServer(), colony);
        context.getSource().sendSuccess(() -> Component.literal("Economy enabled for " + colony.getName() + "."), true);
        return 1;
    }

    private int disable(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = colony(context);
        if (colony == null || !officer(context, colony))
        {
            return 0;
        }
        EconomyManager.disable(context.getSource().getServer(), colony);
        context.getSource().sendSuccess(() -> Component.literal("Economy disabled for " + colony.getName() + "."), true);
        return 1;
    }

    private int export(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = colony(context);
        if (colony == null || !officer(context, colony))
        {
            return 0;
        }
        final EconomyLedger ledger = EconomyManager.ledger(context.getSource().getServer(), colony);
        if (!ledger.enabled())
        {
            context.getSource().sendFailure(Component.literal("Enable the colony economy before exporting."));
            return 0;
        }
        final EconomyManager.ExportResult result = EconomyManager.exportSurplus(colony, ledger);
        EconomyManager.markDirty(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal(
            "Exported " + result.bundles() + " warehouse bundles for " + result.value() + " emeralds."
                + " Treasury now " + ledger.treasury() + "."), true);
        return result.bundles() > 0 ? 1 : 0;
    }

    private int deposit(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = colony(context);
        final Player player = context.getSource().getEntity() instanceof Player p ? p : null;
        if (colony == null || player == null || !officer(context, colony))
        {
            return 0;
        }
        final int amount = IntegerArgumentType.getInteger(context, AMOUNT_ARG);
        final EconomyLedger ledger = EconomyManager.ledger(context.getSource().getServer(), colony);
        final int deposited = EconomyManager.depositEmeralds(ledger, player, amount);
        EconomyManager.markDirty(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal(
            "Deposited " + deposited + " emeralds. Treasury now " + ledger.treasury() + "."), true);
        return deposited > 0 ? 1 : 0;
    }

    private int withdraw(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = colony(context);
        final Player player = context.getSource().getEntity() instanceof Player p ? p : null;
        if (colony == null || player == null || !officer(context, colony))
        {
            return 0;
        }
        final int amount = IntegerArgumentType.getInteger(context, AMOUNT_ARG);
        final EconomyLedger ledger = EconomyManager.ledger(context.getSource().getServer(), colony);
        if (!EconomyManager.withdrawTreasury(ledger, player, amount))
        {
            context.getSource().sendFailure(Component.literal("The colony treasury cannot cover that withdrawal."));
            return 0;
        }
        EconomyManager.markDirty(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal(
            "Withdrew " + amount + " emeralds. Treasury now " + ledger.treasury() + "."), true);
        return 1;
    }

    @Nullable
    private IColony colony(final CommandContext<CommandSourceStack> context)
    {
        try
        {
            return ColonyIdArgument.getColony(context, COLONYID_ARG);
        }
        catch (RuntimeException ignored)
        {
            return null;
        }
    }

    private boolean officer(final CommandContext<CommandSourceStack> context, final IColony colony)
    {
        if (context.getSource().permissions().hasPermission(Permissions.COMMANDS_OWNER))
        {
            return true;
        }
        if (!(context.getSource().getEntity() instanceof Player player))
        {
            context.getSource().sendFailure(Component.literal("Only a colony officer or server owner may do that."));
            return false;
        }
        if (!IMCCommand.isPlayerOped(player) && !colony.getPermissions().getRank(player).isColonyManager())
        {
            context.getSource().sendFailure(Component.literal("Only a colony officer or server owner may do that."));
            return false;
        }
        return true;
    }
}
