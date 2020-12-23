package com.minecolonies.coremod.commands.colonycommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;
import static com.minecolonies.coremod.commands.CommandArgumentNames.RAID_TYPE_ARG;

public class CommandRaidNow implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getWorld().getDimensionKey());
        if (colony == null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
            return 0;
        }

        colony.getRaiderManager().raiderEvent();
        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.raidnow.success", colony.getName()), true);
        return 0;
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

        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getWorld().getDimensionKey());
        if (colony == null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
            return 0;
        }

        colony.getRaiderManager().raiderEvent(StringArgumentType.getString(context, RAID_TYPE_ARG));
        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.raidnow.success", colony.getName()), true);
        return 0;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "raid-now";
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
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(RAID_TYPE_ARG, StringArgumentType.string())
                         .suggests((ctx, builder) -> ISuggestionProvider.suggest(s, builder))
                         .executes(this::runSpecificRaid))
                         .executes(this::checkPreConditionAndExecute));
    }
}
