package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.commands.CommandBaseRaid;
import com.minecolonies.core.commands.arguments.ColonyIdArgument;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.stream.Stream;

import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandRaid extends CommandBaseRaid
{
    @Override
    protected int startRaidNow(final CommandContext<CommandSourceStack> context, final IRaiderManager.RaidSettings raidSettings)
    {
        final IColony colony = ColonyIdArgument.getColony(context, COLONYID_ARG);
        final IRaiderManager.RaidSpawnResult result = colony.getRaiderManager().raiderEvent(raidSettings);

        if (result == IRaiderManager.RaidSpawnResult.SUCCESS)
        {
            context.getSource().sendSuccess(() -> Component.translatableEscape(CommandTranslationConstants.COMMAND_RAID_NOW_SUCCESS, colony.getName()), true);
            return 1;
        }
        context.getSource().sendFailure(Component.translatableEscape(CommandTranslationConstants.COMMAND_RAID_NOW_FAILURE, colony.getName(), result));
        return 0;
    }

    @Override
    protected int startRaidTonight(final CommandContext<CommandSourceStack> context, final IRaiderManager.RaidSettings raidSettings)
    {
        final IColony colony = ColonyIdArgument.getColony(context, COLONYID_ARG);
        colony.getRaiderManager().setRaidNextNight(raidSettings);
        context.getSource().sendSuccess(() -> Component.translatableEscape(CommandTranslationConstants.COMMAND_RAID_TONIGHT_SUCCESS, colony.getName()), true);
        return 0;
    }

    @Override
    public final LiteralArgumentBuilder<CommandSourceStack> build()
    {
        final RequiredArgumentBuilder<CommandSourceStack, ColonyIdArgument.Result> colonyIdArg = IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id());

        return buildCommandsInSerial(Stream.concat(Stream.of(colonyIdArg), createSettingArguments()).toList());
    }

    @Override
    public String getName()
    {
        return "raid";
    }
}
