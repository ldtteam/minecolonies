package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;
import static com.minecolonies.coremod.commands.CommandArgumentNames.RAID_TYPE_ARG;

public class CommandRaidAllTonight implements IMCOPCommand
{
    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        for (final IColony colony : IColonyManager.getInstance().getAllColonies())
        {
            colony.getRaiderManager().setRaidNextNight(true);
        }

        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.raidtonight"), true);
        return 1;
    }

    /**
     * What happens when the command is executed with the optional raidtype argument.
     * @param context the context of the command execution.
     * @return
     */
    public int runSpecificRaid(final CommandContext<CommandSource> context)
    {
        if(!checkPreCondition(context))
        {
            return 0;
        }

        for (final IColony colony : IColonyManager.getInstance().getAllColonies())
        {
            colony.getRaiderManager().setRaidNextNight(true, StringArgumentType.getString(context, RAID_TYPE_ARG));
        }
        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.raidtonight"), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "raid-All-tonight";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        String[] s = new String[5];
        s[0] = "pirate";
        s[1] = "egyptian";
        s[2] = "amazon";
        s[3] = "norsemen";
        s[4] = "barbarian";

        return IMCCommand.newLiteral(getName())
                         .then(IMCCommand.newArgument(RAID_TYPE_ARG, StringArgumentType.string())
                                 .suggests((ctx, builder) -> ISuggestionProvider.suggest(s, builder))
                                 .executes(this::runSpecificRaid))
                         .executes(this::checkPreConditionAndExecute);
    }
}
