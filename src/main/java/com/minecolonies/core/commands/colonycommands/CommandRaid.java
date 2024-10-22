package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.colony.events.raid.norsemenevent.NorsemenShipRaidEvent;
import com.minecolonies.core.colony.events.raid.pirateEvent.PirateGroundRaidEvent;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.core.commands.CommandArgumentNames.*;

public class CommandRaid implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        return raidExecute(context, "");
    }

    public int onSpecificExecute(final CommandContext<CommandSourceStack> context)
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
    public int raidExecute(final CommandContext<CommandSourceStack> context, final String raidType)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        final boolean allowShips = BoolArgumentType.getBool(context, SHIP_ARG);
        if(StringArgumentType.getString(context, RAID_TIME_ARG).equals(RAID_NOW))
        {
            final IRaiderManager.RaidSpawnResult result = colony.getRaiderManager().raiderEvent(raidType, true, allowShips);
            if (result == IRaiderManager.RaidSpawnResult.SUCCESS)
            {
                context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_RAID_NOW_SUCCESS, colony.getName()), true);
                return 1;
            }
            context.getSource().sendFailure(Component.translatable(CommandTranslationConstants.COMMAND_RAID_NOW_FAILURE, colony.getName(), result));
        }
        else if(StringArgumentType.getString(context, RAID_TIME_ARG).equals(RAID_TONIGHT))
        {
            if (!colony.getRaiderManager().canRaid())
            {
                context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_RAID_NOW_FAILURE, colony.getName(), IRaiderManager.RaidSpawnResult.CANNOT_RAID), true);
                return 1;
            }
            colony.getRaiderManager().setRaidNextNight(true, raidType, allowShips);
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_RAID_TONIGHT_SUCCESS, colony.getName()), true);
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
    public LiteralArgumentBuilder<CommandSourceStack> build()
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
                         .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(opt, builder))
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(RAID_TYPE_ARG, StringArgumentType.string())
                                 .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(raidTypes, builder))
                                 .then(IMCCommand.newArgument(SHIP_ARG, BoolArgumentType.bool())
                                 .executes(this::onSpecificExecute)))
                         .executes(this::checkPreConditionAndExecute)));
    }
}
