package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.commands.arguments.MultiColonyIdArgument;
import com.minecolonies.core.commands.arguments.MultipleOptionsArgument;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.core.commands.CommandArgumentNames.*;

public class CommandRaid implements IMCOPCommand
{
    private static final DynamicCommandExceptionType ERROR_INVALID_COLONY_EVENT_TYPE =
        new DynamicCommandExceptionType(entry -> Component.translatable("com.minecolonies.command.raid.colony_type.invalid", entry));

    /**
     * Run the command with all fields including the raid type and ship set.
     *
     * @param ctx the command context.
     * @return the command status.
     */
    private int onExecuteWithType(final CommandContext<CommandSourceStack> ctx)
    {
        return checkPreConditionAndExecute(ctx, (context) -> {
            final String raidType = getRaidType(context);
            final boolean allowShips = BoolArgumentType.getBool(context, SHIP_ARG);
            return raidExecute(context, new IRaiderManager.RaidSettings(true, raidType, allowShips, null, null));
        });
    }

    /**
     * Run the command with all fields including the raider amount set.
     *
     * @param ctx the command context.
     * @return the command status.
     */
    private int onExecuteWithAmount(final CommandContext<CommandSourceStack> ctx)
    {
        return checkPreConditionAndExecute(ctx, (context) -> {
            final String raidType = getRaidType(context);
            final boolean allowShips = BoolArgumentType.getBool(context, SHIP_ARG);
            final int raidAmount = IntegerArgumentType.getInteger(context, RAID_AMOUNT_ARG);
            return raidExecute(context, new IRaiderManager.RaidSettings(true, raidType, allowShips, raidAmount, null));
        });
    }

    /**
     * Run the command with all fields including the location set.
     *
     * @param ctx the command context.
     * @return the command status.
     */
    private int onExecuteWithLocation(final CommandContext<CommandSourceStack> ctx)
    {
        return checkPreConditionAndExecute(ctx, (context) -> {
            final String raidType = getRaidType(context);
            final boolean allowShips = BoolArgumentType.getBool(context, SHIP_ARG);
            final int raidAmount = IntegerArgumentType.getInteger(context, RAID_AMOUNT_ARG);
            final BlockPos raidLocation = BlockPosArgument.getBlockPos(context, RAID_LOCATION_ARG);
            return raidExecute(context, new IRaiderManager.RaidSettings(true, raidType, allowShips, raidAmount, raidLocation));
        });
    }

    /**
     * Internal method for processing the raid type.
     *
     * @param context the command context.
     * @return the raid type.
     * @throws CommandSyntaxException if something goes wrong with the command processing.
     */
    private String getRaidType(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final String raidTypeString = StringArgumentType.getString(context, RAID_TYPE_ARG);
        final ResourceLocation raidType = new ResourceLocation(Constants.MOD_ID, raidTypeString);
        final ColonyEventTypeRegistryEntry colonyEventTypeRegistryEntry = IMinecoloniesAPI.getInstance().getColonyEventRegistry().getValue(raidType);
        if (colonyEventTypeRegistryEntry != null && colonyEventTypeRegistryEntry.isRaidEvent())
        {
            return raidTypeString;
        }
        throw ERROR_INVALID_COLONY_EVENT_TYPE.create(raidTypeString);
    }

    @Override
    public final LiteralArgumentBuilder<CommandSourceStack> build()
    {
        final List<String> raidTimes = List.of(RAID_NOW, RAID_TONIGHT);
        final List<String> raidTypes = new ArrayList<>();
        for (final ColonyEventTypeRegistryEntry colonyEventType : IMinecoloniesAPI.getInstance().getColonyEventRegistry())
        {
            if (colonyEventType.isRaidEvent())
            {
                raidTypes.add(colonyEventType.getRegistryName().getPath());
            }
        }

        final RequiredArgumentBuilder<CommandSourceStack, Coordinates> raidLocationArg =
            IMCCommand.newArgument(RAID_LOCATION_ARG, BlockPosArgument.blockPos()).executes(this::onExecuteWithLocation);
        final RequiredArgumentBuilder<CommandSourceStack, Integer> raidAmountArg =
            IMCCommand.newArgument(RAID_AMOUNT_ARG, IntegerArgumentType.integer(1)).executes(this::onExecuteWithAmount).then(raidLocationArg);
        final RequiredArgumentBuilder<CommandSourceStack, Boolean> raidShipArg =
            IMCCommand.newArgument(SHIP_ARG, BoolArgumentType.bool()).executes(this::onExecuteWithType).then(raidAmountArg);
        final RequiredArgumentBuilder<CommandSourceStack, String> raidTypeArg =
            IMCCommand.newArgument(RAID_TYPE_ARG, StringArgumentType.string())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(raidTypes, builder))
                .then(raidShipArg);
        final RequiredArgumentBuilder<CommandSourceStack, String> raidTimeArg = IMCCommand.newArgument(RAID_TIME_ARG, StringArgumentType.string())
            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(raidTimes, builder))
            .executes(this::checkPreConditionAndExecute)
            .then(raidTypeArg);

        final RequiredArgumentBuilder<CommandSourceStack, MultipleOptionsArgument.OptionContainer<List<Integer>>> colonyIdArg =
            IMCCommand.newArgument(COLONYID_ARG, MultiColonyIdArgument.id()).then(raidTimeArg);
        final RequiredArgumentBuilder<CommandSourceStack, String> allColoniesArg = IMCCommand.newArgument(COLONYID_ARG, StringArgumentType.string()).then(raidTimeArg);

        return IMCCommand.newLiteral(getName()).then(colonyIdArg).then(allColoniesArg);
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

    /**
     * Handler for stating a raid right now.
     *
     * @param context      the command context.
     * @param raidSettings the raid settings.
     * @return the command status.
     */
    private int startRaidNow(final CommandContext<CommandSourceStack> context, final IRaiderManager.RaidSettings raidSettings)
    {
        final List<IColony> colonies = MultiColonyIdArgument.getColonies(context, COLONYID_ARG);
        for (final IColony colony : colonies)
        {
            final IRaiderManager.RaidSpawnResult result = colony.getRaiderManager().raiderEvent(raidSettings);

            if (result == IRaiderManager.RaidSpawnResult.SUCCESS)
            {
                context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_RAID_NOW_SUCCESS, colony.getName()), true);
            }
            else
            {
                context.getSource().sendFailure(Component.translatable(CommandTranslationConstants.COMMAND_RAID_NOW_FAILURE, colony.getName(), result));
            }
        }
        return 1;
    }

    /**
     * Handler for stating a raid right tonight.
     *
     * @param context      the command context.
     * @param raidSettings the raid settings.
     * @return the command status.
     */
    private int startRaidTonight(final CommandContext<CommandSourceStack> context, final IRaiderManager.RaidSettings raidSettings)
    {
        final List<IColony> colonies = MultiColonyIdArgument.getColonies(context, COLONYID_ARG);
        for (final IColony colony : colonies)
        {
            colony.getRaiderManager().setRaidNextNight(raidSettings);
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_RAID_TONIGHT_SUCCESS, colony.getName()), true);
        }
        return 1;
    }

    @Override
    public String getName()
    {
        return "raid";
    }
}
