package com.minecolonies.core.commands.generalcommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.commands.CommandBaseRaid;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CommandRaidAll extends CommandBaseRaid
{
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return buildCommandsInSerial(createSettingArguments().toList());
    }

    @Override
    public String getName()
    {
        return "raid-all";
    }

    @Override
    protected int startRaidNow(final CommandContext<CommandSourceStack> context, final IRaiderManager.RaidSettings raidSettings)
    {
        context.getSource().sendSuccess(() -> Component.translatableEscape(CommandTranslationConstants.COMMAND_RAID_ALL_NOW_SUCCESS), true);

        final List<IColony> colonies = IColonyManager.getInstance().getAllColonies();
        for (final IColony colony : colonies)
        {
            final IRaiderManager.RaidSpawnResult result = colony.getRaiderManager().raiderEvent(raidSettings);

            if (result == IRaiderManager.RaidSpawnResult.SUCCESS)
            {
                final Component text = Component.literal(" - ").append(Component.translatableEscape(CommandTranslationConstants.COMMAND_RAID_ALL_NOW_ROW_SUCCESS, colony.getID()));
                context.getSource().sendSuccess(() -> text, true);
            }
            else
            {
                final Component text =
                    Component.literal(" - ").append(Component.translatableEscape(CommandTranslationConstants.COMMAND_RAID_ALL_NOW_ROW_FAILURE, colony.getID(), result));
                context.getSource().sendFailure(text);
            }
        }
        return 1;
    }

    @Override
    protected int startRaidTonight(final CommandContext<CommandSourceStack> context, final IRaiderManager.RaidSettings raidSettings)
    {
        context.getSource().sendSuccess(() -> Component.translatableEscape(CommandTranslationConstants.COMMAND_RAID_ALL_TONIGHT_SUCCESS), true);

        final List<IColony> colonies = IColonyManager.getInstance().getAllColonies();
        for (final IColony colony : colonies)
        {
            colony.getRaiderManager().setRaidNextNight(raidSettings);
        }
        return 1;
    }
}
