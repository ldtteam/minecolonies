package com.minecolonies.core.commands;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.minecolonies.core.commands.CommandArgumentNames.*;

public abstract class CommandBaseRaid implements IMCOPCommand
{
    protected final Stream<ArgumentBuilder<CommandSourceStack, ?>> createSettingArguments()
    {
        String[] opt = new String[2];
        opt[0] = RAID_NOW;
        opt[1] = RAID_TONIGHT;

        final List<String> raidTypes = new ArrayList<>();
        for (final ColonyEventTypeRegistryEntry type : IMinecoloniesAPI.getInstance().getColonyEventRegistry())
        {
            raidTypes.add(type.getRegistryName().toString());
        }

        final RequiredArgumentBuilder<CommandSourceStack, String> raidTimeArg = IMCCommand.newArgument(RAID_TIME_ARG, StringArgumentType.string())
            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(opt, builder));
        final RequiredArgumentBuilder<CommandSourceStack, String> raidTypeArg = IMCCommand.newArgument(RAID_TYPE_ARG, StringArgumentType.string())
            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(raidTypes, builder))
            .executes(this::checkPreConditionAndExecute);
        final RequiredArgumentBuilder<CommandSourceStack, Boolean> raidShipArg = IMCCommand.newArgument(SHIP_ARG, BoolArgumentType.bool())
            .executes(this::onExecuteWithType);
        final RequiredArgumentBuilder<CommandSourceStack, Integer> raidAmountArg = IMCCommand.newArgument(RAID_AMOUNT_ARG, IntegerArgumentType.integer(1))
            .executes(this::onExecuteWithAmount);
        final RequiredArgumentBuilder<CommandSourceStack, Coordinates> raidLocationArg = IMCCommand.newArgument(RAID_LOCATION_ARG, BlockPosArgument.blockPos())
            .executes(this::onExecuteWithLocation);
        return Stream.of(raidTimeArg, raidTypeArg, raidShipArg, raidAmountArg, raidLocationArg);
    }

    private int onExecuteWithType(final CommandContext<CommandSourceStack> ctx)
    {
        return checkPreConditionAndExecute(ctx, (context) -> {
            final String raidType = StringArgumentType.getString(context, RAID_TYPE_ARG);
            final boolean allowShips = BoolArgumentType.getBool(context, SHIP_ARG);
            return raidExecute(context, new IRaiderManager.RaidSettings(true, raidType, allowShips, null, null));
        });
    }

    private int onExecuteWithAmount(final CommandContext<CommandSourceStack> ctx)
    {
        return checkPreConditionAndExecute(ctx, (context) -> {
            final String raidType = StringArgumentType.getString(context, RAID_TYPE_ARG);
            final boolean allowShips = BoolArgumentType.getBool(context, SHIP_ARG);
            final int raidAmount = IntegerArgumentType.getInteger(context, RAID_AMOUNT_ARG);
            return raidExecute(context, new IRaiderManager.RaidSettings(true, raidType, allowShips, raidAmount, null));
        });
    }

    private int onExecuteWithLocation(final CommandContext<CommandSourceStack> ctx)
    {
        return checkPreConditionAndExecute(ctx, (context) -> {
            final String raidType = StringArgumentType.getString(context, RAID_TYPE_ARG);
            final boolean allowShips = BoolArgumentType.getBool(context, SHIP_ARG);
            final int raidAmount = IntegerArgumentType.getInteger(context, RAID_AMOUNT_ARG);
            final BlockPos raidLocation = BlockPosArgument.getBlockPos(context, RAID_LOCATION_ARG);
            return raidExecute(context, new IRaiderManager.RaidSettings(true, raidType, allowShips, raidAmount, raidLocation));
        });
    }

    @Override
    public final int onExecute(final CommandContext<CommandSourceStack> context)
    {
        return raidExecute(context, new IRaiderManager.RaidSettings(true, null, true, null, null));
    }

    /**
     * Actually find the colony and assign the raid event.
     *
     * @param context      command context from the user.
     * @param raidSettings type of raid, or "" if determining naturally.
     * @return zero if failed, one if successful.
     */
    private int raidExecute(final CommandContext<CommandSourceStack> context, final IRaiderManager.RaidSettings raidSettings)
    {
        final String raidTime = StringArgumentType.getString(context, RAID_TIME_ARG);

        return switch (raidTime)
        {
            case RAID_NOW -> startRaidNow(context, raidSettings);
            case RAID_TONIGHT -> startRaidTonight(context, raidSettings);
            default -> 0;
        };
    }

    protected abstract int startRaidNow(final CommandContext<CommandSourceStack> context, final IRaiderManager.RaidSettings raidSettings);

    protected abstract int startRaidTonight(final CommandContext<CommandSourceStack> context, final IRaiderManager.RaidSettings raidSettings);

    protected enum RaidTime
    {
        NOW,
        TONIGHT;

        @Override
        public String toString()
        {
            return super.toString();
        }
    }
}
