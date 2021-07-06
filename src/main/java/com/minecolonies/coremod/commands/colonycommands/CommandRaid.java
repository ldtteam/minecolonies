package com.minecolonies.coremod.commands.colonycommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.norsemenevent.NorsemenShipRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateGroundRaidEvent;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.coremod.commands.CommandArgumentNames.*;

public class CommandRaid implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        return raidExecute(context, "");
    }

    public int onSpecificExecute(final CommandContext<CommandSource> context)
    {
        if(!checkPreCondition(context))
        {
            return 0;
        }
        return raidExecute(context, StringArgumentType.getString(context, RAID_TYPE_ARG));
    }

    /**
     * Actually find the colony and assign the raid event.
     * @param context       command context from the user.
     * @param raidType      type of raid, or "" if determining naturally.
     * @return              zero if failed, one if successful.
     */
    public int raidExecute(final CommandContext<CommandSource> context, final String raidType)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
            return 0;
        }

        if(StringArgumentType.getString(context, RAID_TIME_ARG).equals(RAID_NOW))
        {
            colony.getRaiderManager().raiderEvent(raidType);
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.raidnow.success", colony.getName()), true);
        }
        else if(StringArgumentType.getString(context, RAID_TIME_ARG).equals(RAID_TONIGHT))
        {
            colony.getRaiderManager().setRaidNextNight(true, raidType);
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.raidtonight.success", colony.getName()), true);
        }
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "raid";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        final List<String> raidTypes = new ArrayList<>();
        for(final ColonyEventTypeRegistryEntry type : IMinecoloniesAPI.getInstance().getColonyEventRegistry().getValues())
        {
            if(!type.getRegistryName().getPath().equals(PirateGroundRaidEvent.PIRATE_GROUND_RAID_EVENT_TYPE_ID.getPath())
                 && !type.getRegistryName().getPath().equals(NorsemenShipRaidEvent.NORSEMEN_RAID_EVENT_TYPE_ID.getPath()))
            {
                raidTypes.add(type.getRegistryName().getPath());
            }
        }

        String[] opt = new String[2];
        opt[0] = RAID_NOW;
        opt[1] = RAID_TONIGHT;

        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(RAID_TIME_ARG, StringArgumentType.string())
                         .suggests((ctx, builder) -> ISuggestionProvider.suggest(opt, builder))
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(RAID_TYPE_ARG, StringArgumentType.string())
                                 .suggests((ctx, builder) -> ISuggestionProvider.suggest(raidTypes, builder))
                                 .executes(this::onSpecificExecute))
                         .executes(this::checkPreConditionAndExecute)));
    }
}
