package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

import static com.minecolonies.coremod.commands.CommandArgumentNames.*;

public class CommandRaidAll implements IMCOPCommand
{
    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        return raidsExecute(context, "");
    }

    /**
     * What happens when the command is executed with the optional raidtype argument.
     * @param context the context of the command execution.
     * @return
     */
    public int onSpecificExecute(final CommandContext<CommandSource> context)
    {
        if(!checkPreCondition(context))
        {
            return 0;
        }
        return raidsExecute(context, StringArgumentType.getString(context, RAID_TYPE_ARG));
    }

    public int raidsExecute(final CommandContext<CommandSource> context, final String raidType)
    {
        if(StringArgumentType.getString(context, RAID_TIME_ARG).equals(RAID_NOW))
        {
            for (final IColony colony : IColonyManager.getInstance().getAllColonies())
            {
                colony.getRaiderManager().raiderEvent(raidType);
            }
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.raidtonight"), true);
            return 1;
        }
        else if(StringArgumentType.getString(context, RAID_TIME_ARG).equals(RAID_TONIGHT))
        {
            for (final IColony colony : IColonyManager.getInstance().getAllColonies())
            {
                colony.getRaiderManager().setRaidNextNight(true, raidType);
            }
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.raidtonight"), true);
            return 1;
        }
        return 0;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "raid-All";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        String[] raidTypes = new String[IMinecoloniesAPI.getInstance().getColonyEventRegistry().getKeys().size()];
        int i = 0;
        for(final ColonyEventTypeRegistryEntry type : IMinecoloniesAPI.getInstance().getColonyEventRegistry().getValues())
        {
            raidTypes[i] = type.getRegistryName().getPath();
            i++;
        }

        String[] opt = new String[2];
        opt[0] = RAID_NOW;
        opt[1] = RAID_TONIGHT;

        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(RAID_TIME_ARG, StringArgumentType.string())
                         .suggests((ctx, builder) -> ISuggestionProvider.suggest(opt, builder))
                 .then(IMCCommand.newArgument(RAID_TYPE_ARG, StringArgumentType.string())
                         .suggests((ctx, builder) -> ISuggestionProvider.suggest(raidTypes, builder))
                         .executes(this::onSpecificExecute))
                 .executes(this::checkPreConditionAndExecute));
    }
}
